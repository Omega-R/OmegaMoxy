package com.omegar.mvp.compiler.entity.parser.km

import com.omegar.mvp.compiler.entity.ViewMethod
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.classinspectors.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmProperty
import kotlinx.metadata.jvm.getterSignature
import kotlinx.metadata.jvm.setterSignature
import kotlinx.metadata.jvm.signature
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.collections.HashMap

class KmViewMethodParser(elements: Elements, private val types: Types) : ViewMethod.Parser() {

    companion object {

        private const val ANNOTATION_JVM_SYNTHETICS_FUNC = "@kotlin.jvm.JvmSynthetic"
        private const val ANNOTATION_JVM_SYNTHETICS_PROPERTY = "@set:kotlin.jvm.JvmSynthetic"

    }

    @KotlinPoetMetadataPreview
    private val classInspector = ElementsClassInspector.create(elements, types)

    private val map: MutableMap<TypeElement, TypeSpec> = HashMap()

    fun isPossible(targetInterfaceElement: TypeElement): Boolean {
        return targetInterfaceElement.getAnnotation(Metadata::class.java) != null
    }

    @KotlinPoetMetadataPreview
    override fun extractTypeVariableNames(targetInterfaceElement: TypeElement): List<TypeVariableName> {
        return targetInterfaceElement.typeSpec.typeVariables
    }

    @KotlinPoetMetadataPreview
    override fun parse(targetInterfaceElement: TypeElement): List<ViewMethod> {
        val typeSpec = targetInterfaceElement.typeSpec

        val annotationMap = targetInterfaceElement.enclosedElements
                .asSequence()
                .filterIsInstance(ExecutableElement::class.java)
                .associate { it.jvmMethodSignature(types) to it.annotationMirrors }

        val properties = typeSpec.propertySpecs
                .asSequence()
                .filter { it.mutable }
                .map { property ->
                    val param = ParameterSpec.builder("value", property.type).build()
                    ViewMethod(
                            name = property.name,
                            parameterSpecs = listOf(param),
                            exceptions = emptyList(),
                            typeVariables = emptyList(),
                            type = ViewMethod.Type.Property(property),
                            annotationData = property.toAnnotationDataList(annotationMap),
                            isSynthetic = property.annotations.indexOfFirst {
                                it.toString() == ANNOTATION_JVM_SYNTHETICS_PROPERTY
                            } != -1
                    )
                }

        val functions = typeSpec.funSpecs
                .asSequence()
                .filter { it.returnType == UNIT || it.returnType == null }
                .map { func ->
                    ViewMethod(
                            name = func.name,
                            parameterSpecs = func.parameters,
                            exceptions = emptyList(),
                            typeVariables = func.typeVariables,
                            type = ViewMethod.Type.Method(func),
                            annotationData = func.toAnnotationDataList(annotationMap),
                            isSynthetic = func.annotations.indexOfFirst { it.toString() == ANNOTATION_JVM_SYNTHETICS_FUNC } != -1
                    )
                }
        return (properties + functions).toList()
    }

    private fun PropertySpec.toAnnotationDataList(annotationMap: Map<String, List<AnnotationMirror>>): List<ViewMethod.AnnotationData> {
        val javaxAnnotations = tag(KmProperty::class.java)?.let { kmProperty ->
            val setterSignature = kmProperty.setterSignature?.asString()
            val getterSignature = kmProperty.getterSignature?.asString()
            annotationMap[getterSignature].orEmpty() + annotationMap[setterSignature].orEmpty()
        }.orEmpty()

        val kmAnnotations = annotations.mapNotNull { it.tag(AnnotationMirror::class) }

        return (javaxAnnotations + kmAnnotations).toAnnotationDataList()
    }

    private fun FunSpec.toAnnotationDataList(annotationMap: Map<String, List<AnnotationMirror>>): List<ViewMethod.AnnotationData> {
        val signature = tag(KmFunction::class.java)?.signature?.asString()
        return ((annotationMap[signature].orEmpty() + annotations.mapNotNull { it.tag(AnnotationMirror::class) })
                .toAnnotationDataList())
    }

    @KotlinPoetMetadataPreview
    private val TypeElement.typeSpec: TypeSpec
        get() = map.getOrPut(this) { toTypeSpec(classInspector) }

}