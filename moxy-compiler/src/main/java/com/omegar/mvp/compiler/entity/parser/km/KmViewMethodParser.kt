package com.omegar.mvp.compiler.entity.parser.km

import com.omegar.mvp.compiler.entity.ViewMethod
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.classinspectors.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.sun.source.util.Trees
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.collections.HashMap

class KmViewMethodParser(private val elements: Elements, types: Types) : ViewMethod.Parser() {

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

        val properties = typeSpec.propertySpecs
                .asSequence()
                .filter { it.mutable }
                .map { property ->
//                    if (property.name == "boolean") {
//
//                        println(
//                                targetInterfaceElement.enclosedElements
//                                        .filterIsInstance(TypeElement::class.java)
//                                        .firstOrNull { it.simpleName.toString() == "DefaultImpls" }
//                                        ?.asType()?.toString()
//                        )
//                    }
                    val param = ParameterSpec.builder("value", property.type).build()
                    ViewMethod(
                            name = property.name,
                            parameterSpecs = listOf(param),
                            exceptions = emptyList(),
                            typeVariables = emptyList(),
                            type = ViewMethod.Type.Property(property),
                            annotationData = property.annotations.mapNotNull { it.tag(AnnotationMirror::class) }
                                    .toAnnotationDataList(),
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
                            annotationData = func.annotations.mapNotNull { it.tag(AnnotationMirror::class) }.toAnnotationDataList(),
                            isSynthetic = func.annotations.indexOfFirst { it.toString() == ANNOTATION_JVM_SYNTHETICS_FUNC } != -1
                    )
                }
        return (properties + functions).toList()
    }

    @KotlinPoetMetadataPreview
    private val TypeElement.typeSpec: TypeSpec
        get() = map.getOrPut(this) { toTypeSpec(classInspector) }


}