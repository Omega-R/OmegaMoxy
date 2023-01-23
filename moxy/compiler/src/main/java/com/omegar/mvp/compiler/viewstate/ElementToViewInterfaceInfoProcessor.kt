package com.omegar.mvp.compiler.viewstate

import com.omegar.mvp.MvpView
import com.omegar.mvp.compiler.Util
import com.omegar.mvp.compiler.entity.ViewCommandInfo
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo
import com.omegar.mvp.compiler.entity.ViewMethod
import com.omegar.mvp.compiler.entity.parser.UniversalViewMethodParser
import com.omegar.mvp.compiler.pipeline.ElementProcessor
import com.omegar.mvp.compiler.pipeline.PipelineContext
import com.omegar.mvp.viewstate.SerializeType
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.annotation.processing.Messager
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

class ElementToViewInterfaceInfoProcessor(
        private val elements: Elements,
        private val messager: Messager,
        private val types: Types
) : ElementProcessor<TypeElement, ViewInterfaceInfo>() {

    companion object {
        private val MOXY_VIEW_STATE_TYPE_ANNOTATION = MoxyViewCommand::class.java.name
    }


    private val mViewMethodParser: UniversalViewMethodParser = UniversalViewMethodParser(elements, types)
    private val mMvpViewTypeMirror: TypeMirror = elements.getTypeElement(MvpView::class.java.canonicalName).asType()

    private val addToEndSingleStrategyClass by lazy { elements.getTypeElement(AddToEndSingleStrategy::class.java.canonicalName) }

    @KotlinPoetMetadataPreview
    override fun process(input: TypeElement, context: PipelineContext<ViewInterfaceInfo>?) {
        generateInfo(input)?.let {
            context?.next(it)
        }
    }

    @KotlinPoetMetadataPreview
    private fun generateInfo(element: TypeElement): ViewInterfaceInfo? {
        if (element.asClassName() == Util.MVP_VIEW_CLASS_NAME) return null

        // Get commands for input class
        val commands = element.getCommands()

        // get super interface
        val superInterfaceType = getSuperInterfaceTypeElement(element)
        return ViewInterfaceInfo(
                superInterfaceType,
                element,
                commands,
                mViewMethodParser.extractTypeVariableNames(element),
                superInterfaceType?.let { mViewMethodParser.extractTypeVariableNames(superInterfaceType) } ?: emptyList()
        )
    }

    private fun getSuperInterfaceTypeElement(element: TypeElement): TypeElement? {
        for (typeMirror in element.interfaces) {
            val interfaceElement = Util.asElement(typeMirror)
            if (interfaceElement != null && types.isAssignable(interfaceElement.asType(), mMvpViewTypeMirror)) {
                return interfaceElement
            }
        }
        return null
    }

    @KotlinPoetMetadataPreview
    private fun TypeElement.getCommands(): List<ViewCommandInfo> {
        val methodsCounter: MutableMap<String, Int> = HashMap()


        val defaultImplList = enclosedElements
                .filterIsInstance(TypeElement::class.java)
                .firstOrNull { it.simpleName.toString() == "DefaultImpls" }
                ?.enclosedElements
                ?.filterIsInstance(ExecutableElement::class.java)
                ?.map { it.simpleName.toStandardGetMethodName() }
                .orEmpty()

        return mViewMethodParser.parse(this)
                .map { method ->
                    val annotation = method.getAnnotation(MOXY_VIEW_STATE_TYPE_ANNOTATION)
                    val strategyClass = annotation?.getStrategyClass()
                            ?: method.getStrategyClass(messager, simpleName.toString())
                            ?: throw IllegalArgumentException("""
You are trying generate ViewState for ${simpleName}. But "${method.name}" method don't provide Strategy type. 
Please annotate your method with Strategy.

For example:
@MoxyViewCommand(ADD_TO_END_SINGLE)
fun ${method.name}()

""")
                    val methodTag = annotation.getMethodTag(method.name)
                    val singleInstance = annotation?.getValueAsBoolean("singleInstance") ?: false
                    val serializeType = annotation?.getValueAsEnum("serializeType") ?: SerializeType.NONE
                    // Allow methods be with same names
                    val uniqueSuffix = getUniqueSuffix(methodsCounter, method.name)
                    ViewCommandInfo(
                            method = method,
                            uniqueSuffix = uniqueSuffix,
                            strategy = strategyClass,
                            tag = methodTag,
                            singleInstance = singleInstance,
                            serializeType = serializeType,
                            existsDefaultImpl = method.name.toStandardGetMethodName() in defaultImplList
                    )
                }
    }

    private fun CharSequence.toStandardGetMethodName(): String {
        return toString().removePrefix("get").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun getUniqueSuffix(methodsCounter: MutableMap<String, Int>, methodName: String): String {
        var counter = methodsCounter[methodName]
        var uniqueSuffix = ""
        if (counter != null && counter > 0) {
            uniqueSuffix = counter.toString()
        } else {
            counter = 0
        }
        methodsCounter[methodName] = counter + 1
        return uniqueSuffix
    }


    private fun ViewMethod.AnnotationData.getStrategyClass(): TypeElement? {
        return when (val type = getValueAsEnum<StrategyType>("value")) {
            StrategyType.CUSTOM, null -> {
                getValueAsTypeElement("custom", elements)
            }
            else -> elements.getTypeElement(type.strategyClass!!.canonicalName)
        }
    }

    private fun ViewMethod.getStrategyClass(messager: Messager, interfaceName: String): TypeElement? {
        if (isSynthetic) {
            messager.printMessage(Diagnostic.Kind.WARNING, """
The '$name' method in $interfaceName is set to the ADD_TO_END_SINGLE strategy, since it is not possible to read annotations for synthetic methods                                         
            """.trimIndent())
            return addToEndSingleStrategyClass
        }
        return when (type) {
            is ViewMethod.Type.Property -> addToEndSingleStrategyClass
            else -> null
        }
    }

    private fun ViewMethod.AnnotationData?.getMethodTag(defaultTag: String): String {
        // get tag from annotation
        return this?.getValue("tag") ?: defaultTag
    }

}