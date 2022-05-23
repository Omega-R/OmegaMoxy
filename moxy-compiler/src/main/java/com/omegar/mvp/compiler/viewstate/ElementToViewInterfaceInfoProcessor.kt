package com.omegar.mvp.compiler.viewstate

import com.omegar.mvp.MvpView
import com.omegar.mvp.compiler.Util
import com.omegar.mvp.compiler.entity.ViewCommandInfo
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo
import com.omegar.mvp.compiler.entity.ViewMethod
import com.omegar.mvp.compiler.entity.parser.UniversalViewMethodParser
import com.omegar.mvp.compiler.pipeline.ElementProcessor
import com.omegar.mvp.compiler.pipeline.PipelineContext
import com.omegar.mvp.compiler.pipeline.Publisher
import com.omegar.mvp.viewstate.SerializeType
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.annotation.processing.Messager
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types


class ElementToViewInterfaceInfoProcessor(
        private val mElements: Elements,
        private val mTypes: Types,
        private val mUsedStrategiesPublisher: Publisher<TypeElement>
) : ElementProcessor<TypeElement?, ViewInterfaceInfo?>() {

    companion object {
        private val STATE_STRATEGY_TYPE_ANNOTATION = StateStrategyType::class.java.name
    }


    private val mViewMethodParser: UniversalViewMethodParser = UniversalViewMethodParser(mElements, mTypes)
    private val mMvpViewTypeMirror: TypeMirror = mElements.getTypeElement(MvpView::class.java.canonicalName).asType()

    private val addToEndSingleStrategyClass by lazy { mElements.getTypeElement(AddToEndSingleStrategy::class.java.canonicalName) }

    @KotlinPoetMetadataPreview
    override fun process(element: TypeElement?, context: PipelineContext<ViewInterfaceInfo?>) {
        generateInfo(element!!)?.let {
            context.next(it)
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
            if (interfaceElement != null && mTypes.isAssignable(interfaceElement.asType(), mMvpViewTypeMirror)) {
                return interfaceElement
            }
        }
        return null
    }

    @KotlinPoetMetadataPreview
    private fun TypeElement.getCommands(): List<ViewCommandInfo> {
        val methodsCounter: MutableMap<String, Int> = HashMap()

        return mViewMethodParser.parse(this)
                .map { method ->
                    val annotation = method.getAnnotation(STATE_STRATEGY_TYPE_ANNOTATION)
                    val strategyClass = annotation?.getStrategyClass()
                            ?: method.getStrategyClass()
                            ?: throw IllegalArgumentException("""
You are trying generate ViewState for ${simpleName}. But "${method.name}" method don't provide Strategy type. 
Please annotate your method with Strategy.

For example:
@StateStrategyType(ADD_TO_END_SINGLE)
fun ${method.name}()

""")
                    // publish strategy
                    mUsedStrategiesPublisher.next(strategyClass)
                    val methodTag = annotation.getMethodTag(method.name)
                    val singleInstance = annotation?.getValueAsBoolean("singleInstance") ?: false
                    val serializeType = annotation?.getValueAsEnum("serializeType") ?: SerializeType.NONE
                    // Allow methods be with same names
                    val uniqueSuffix = getUniqueSuffix(methodsCounter, method.name)
                    ViewCommandInfo(method, uniqueSuffix, strategyClass, methodTag, singleInstance, serializeType)
                }
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
                getValueAsTypeElement("custom", mElements)
            }
            else -> mElements.getTypeElement(type.strategyClass.canonicalName)
        }
    }

    private fun ViewMethod.getStrategyClass(): TypeElement? {
        return when (type) {
            is ViewMethod.Type.Property -> addToEndSingleStrategyClass
            else -> null
        }
    }

    private fun ViewMethod.AnnotationData?.getMethodTag(defaultTag: String): String {
        // get tag from annotation
        return this?.getValue("tag") ?: defaultTag
    }

    override fun finish(nextContext: PipelineContext<ViewInterfaceInfo?>?) {
        mUsedStrategiesPublisher.finish()
        super.finish(nextContext)
    }

}