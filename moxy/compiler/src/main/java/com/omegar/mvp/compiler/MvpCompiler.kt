package com.omegar.mvp.compiler

import com.omegar.mvp.RegisterMoxyReflectorPackages
import com.omegar.mvp.compiler.entity.*
import com.omegar.mvp.compiler.entity.AnnotationInfo.Companion.create
import com.omegar.mvp.compiler.entity.AnnotationTarget
import com.omegar.mvp.compiler.pipeline.*
import com.omegar.mvp.compiler.pipeline.TriplePublisher.Companion.collectTriple
import com.omegar.mvp.compiler.presenterbinder.ElementToTargetClassInfoProcessor
import com.omegar.mvp.compiler.presenterbinder.TargetClassInfoToPresenterBinderJavaFileProcessor
import com.omegar.mvp.compiler.presenterbinder.VariableToContainerElementProcessor
import com.omegar.mvp.compiler.reflector.MoxyReflectorProcessor
import com.omegar.mvp.compiler.viewstate.ElementToViewInterfaceInfoProcessor
import com.omegar.mvp.compiler.viewstate.ViewInterfaceInfoToViewStateKotlinFileProcessor
import com.omegar.mvp.compiler.viewstate.ViewInterfaceInfoValidator
import com.omegar.mvp.compiler.viewstateprovider.ElementToPresenterInfoProcessor
import com.omegar.mvp.compiler.viewstateprovider.NormalPresenterValidator
import com.omegar.mvp.compiler.viewstateprovider.PresenterInfoToViewStateProviderKotlinFileProcessor
import com.omegar.mvp.presenter.InjectPresenter
import com.squareup.kotlinpoet.FileSpec
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

class MvpCompiler : AbstractProcessor() {

    companion object {
        const val DEFAULT_MOXY_REFLECTOR_PACKAGE = "com.omegar.mvp"
        private const val MOXY_ANNOTATION_INJECT_VIEW_STATE = "com.omegar.mvp.InjectViewState"
        private const val OPTION_MOXY_REFLECTOR_PACKAGE = "moxyReflectorPackage"
        private const val OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES = "moxyRegisterReflectorPackages"
        private const val OPTION_ENABLE_ISOLATING_PROCESSING = "moxyEnableIsolatingProcessing"
    }

    private lateinit var messager: Messager
    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var options: Map<String, String>
    private lateinit var injectViewStateAnnotation: AnnotationInfo<TypeElement>
    private lateinit var injectPresenterAnnotation: AnnotationInfo<VariableElement>

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
        types = processingEnv.typeUtils
        elements = processingEnv.elementUtils
        options = processingEnv.options
        injectViewStateAnnotation = create(elements, MOXY_ANNOTATION_INJECT_VIEW_STATE, AnnotationTarget.CLASS)
        injectPresenterAnnotation = create(elements, InjectPresenter::class.java, AnnotationTarget.FIELD)
    }

    override fun getSupportedOptions(): Set<String> {
        return setOf(
            OPTION_MOXY_REFLECTOR_PACKAGE,
            OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES,
            OPTION_ENABLE_ISOLATING_PROCESSING,
            currentIncrementalAnnotationProcessorType
        )
    }

    private val isEnabledIsolatingProcessing: Boolean
        get() = options[OPTION_ENABLE_ISOLATING_PROCESSING].toBoolean()

    private val currentIncrementalAnnotationProcessorType: String
        get() = "org.gradle.annotation.processing." + if (isEnabledIsolatingProcessing) "isolating" else "aggregating"

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
            InjectPresenter::class.java.canonicalName,
            MOXY_ANNOTATION_INJECT_VIEW_STATE,
            RegisterMoxyReflectorPackages::class.java.canonicalName
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        return if (annotations.isEmpty()) {
            false
        } else try {
            throwableProcess(annotations, roundEnv)
        } catch (e: RuntimeException) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Moxy compilation failed. Could you copy stack trace above and write us (or make issue on Github)?"
            )
            e.printStackTrace()
            true
        }
    }

    private fun throwableProcess(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val currentMoxyReflectorPackage = options[OPTION_MOXY_REFLECTOR_PACKAGE] ?: DEFAULT_MOXY_REFLECTOR_PACKAGE
        val presenterContainerElementPublisher = Publisher<TypeElement>()
        val presenterElementPublisher = Publisher<TypeElement>()
        val viewElementPublisher = Publisher<TypeElement>()
        val reflectorPackagesPublisher = Publisher(getAdditionalMoxyReflectorPackages(roundEnv))
        val kotlinFileWriter = KotlinFileWriter(processingEnv.filer)

        // moxyReflectorPipeline
        Pipeline.Builder<Triple<Set<TypeElement>, Set<TypeElement>, Set<String>>, Any>(
            collectTriple(
                presenterElementPublisher,
                presenterContainerElementPublisher,
                reflectorPackagesPublisher
            )
        )
            .addProcessor<KotlinFile, Void?>(MoxyReflectorProcessor(currentMoxyReflectorPackage))
            .buildPipeline(kotlinFileWriter)
            .start()


        // viewStatePipeline
        Pipeline.Builder<TypeElement, ViewInterfaceInfo>(viewElementPublisher)
            .addProcessor<ViewInterfaceInfo, ViewInterfaceInfo>(ElementToViewInterfaceInfoProcessor(elements, messager, types))
            .uniqueFilter()
            .addValidator<ViewInterfaceInfo>(ViewInterfaceInfoValidator(elements, currentMoxyReflectorPackage))
            .addProcessor<FileSpec, Void?>(
                ViewInterfaceInfoToViewStateKotlinFileProcessor(
                    elements,
                    types,
                    currentMoxyReflectorPackage,
                    reflectorPackagesPublisher
                )
            )
            .buildPipeline(kotlinFileWriter)
            .start()

        // viewStateProviderPipeline
        Pipeline.Builder<TypeElement, Any>(ElementsAnnotatedGenerator(roundEnv, messager, injectViewStateAnnotation))
            .addProcessor<PresenterInfo, Any>(ElementToPresenterInfoProcessor(elements, viewElementPublisher))
            .addValidator<Any>(NormalPresenterValidator())
            .copyTypeElementTo(presenterElementPublisher)
            .addProcessor<KotlinFile, Void?>(PresenterInfoToViewStateProviderKotlinFileProcessor().withCache())
            .buildPipeline(kotlinFileWriter)
            .start()

        if (injectPresenterAnnotation.contains(annotations)) {
            checkInjectors(
                roundEnv,
                PresenterInjectorRules(
                    elements,
                    messager,
                    injectPresenterAnnotation,
                    Modifier.PUBLIC,
                    Modifier.PROTECTED,
                    Modifier.DEFAULT
                )
            )

            // presenterBinderPipeline
            Pipeline.Builder<VariableElement, Any>(ElementsAnnotatedGenerator(roundEnv, messager, injectPresenterAnnotation))
                .addProcessor<TypeElement, Any>(VariableToContainerElementProcessor())
                .uniqueFilter()
                .copyPublishTo<Any>(presenterContainerElementPublisher)
                .addProcessor<TargetClassInfo, Any>(ElementToTargetClassInfoProcessor())
                .addProcessor<FileSpec, Any>(TargetClassInfoToPresenterBinderJavaFileProcessor())
                .buildPipeline(kotlinFileWriter)
                .start()
        } else {
            presenterContainerElementPublisher.finish()
        }
        return true
    }

    private fun getAdditionalMoxyReflectorPackages(roundEnv: RoundEnvironment): List<String> {
        val result: MutableList<String> = ArrayList()
        result.addAll(optionsAdditionalReflectorPackages)
        result.addAll(getRegisterAdditionalMoxyReflectorPackages(roundEnv))
        return result
    }

    private fun getRegisterAdditionalMoxyReflectorPackages(roundEnv: RoundEnvironment): List<String> {
        for (element in roundEnv.getElementsAnnotatedWith(RegisterMoxyReflectorPackages::class.java)) {
            if (element.kind != ElementKind.CLASS) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR, element.toString() + " must be " + ElementKind.CLASS.name
                            + ", or not mark it as @" + RegisterMoxyReflectorPackages::class.java.simpleName
                )
            }
            val packages = element.getAnnotation(RegisterMoxyReflectorPackages::class.java).value
            return packages.toList()
        }
        return emptyList()
    }

    private val optionsAdditionalReflectorPackages: List<String>
        get() = options[OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES]?.split(",").orEmpty()

    private fun checkInjectors(roundEnv: RoundEnvironment, annotationRule: AnnotationRule) {
        val annotationTypeElement = annotationRule.annotationInfo.typeElement
        for (annotatedElement in roundEnv.getElementsAnnotatedWith(annotationTypeElement)) {
            annotationRule.checkAnnotation(annotatedElement)
        }
        val errorStack = annotationRule.errorStack
        if (errorStack.isNotEmpty()) {
            messager.printMessage(Diagnostic.Kind.ERROR, errorStack)
        }
    }

}