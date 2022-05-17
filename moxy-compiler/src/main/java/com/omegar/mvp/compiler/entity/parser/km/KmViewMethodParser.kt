package com.omegar.mvp.compiler.entity.parser.km

import com.omegar.mvp.compiler.entity.ViewMethod
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.classinspectors.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.collections.HashMap

class KmViewMethodParser(elements: Elements, types: Types) : ViewMethod.Parser() {
    @KotlinPoetMetadataPreview
    private val classInspector = ElementsClassInspector.create(elements, types)

    private val map: MutableMap<TypeElement, TypeSpec> = HashMap()

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
                    val param = ParameterSpec.builder("value", property.type).build()

                    ViewMethod(
                            element = targetInterfaceElement,
                            name = property.name,
                            parameterSpecs = listOf(param),
                            exceptions = emptyList(),
                            typeVariables = emptyList(),
                            type = ViewMethod.Type.Property(property),
                            annotationData = emptyList()
                    )
                }

        val functions = typeSpec.funSpecs
                .asSequence()
                .filter { it.returnType == UNIT || it.returnType == null }
                .map { func ->
                    ViewMethod(
                            targetInterfaceElement,
                            func.name,
                            func.parameters,
                            emptyList(),
                            func.typeVariables,
                            ViewMethod.Type.Method(func),
                            func.annotations.mapNotNull { it.tag(AnnotationMirror::class) }.toAnnotationDataList()
                    )
                }
        return (properties + functions).toList()
    }

    @KotlinPoetMetadataPreview
    private val TypeElement.typeSpec: TypeSpec get() = map.getOrPut(this) { toTypeSpec(classInspector) }

}