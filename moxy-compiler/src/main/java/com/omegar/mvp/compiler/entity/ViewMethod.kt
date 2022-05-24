package com.omegar.mvp.compiler.entity

import com.squareup.kotlinpoet.*
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

data class ViewMethod(val name: String,
                      val parameterSpecs: List<ParameterSpec>,
                      val exceptions: List<TypeName>,
                      val typeVariables: List<TypeVariableName>,
                      val type: Type,
                      val annotationData: List<AnnotationData>,
                      val isSynthetic: Boolean
) {
    val argumentsString = parameterSpecs.joinToString(", ", transform = ParameterSpec::name)

    val argumentsStringWithStar = parameterSpecs.joinToString(", ") {
        if (KModifier.VARARG in it.modifiers) "*" + it.name else it.name
    }

    private val annotationMap = annotationData.groupBy { it.name }

    override fun toString(): String {
        return "ViewMethod{" +
                "name='" + name + '\'' +
                '}'
    }

    fun getAnnotation(name: String): AnnotationData? {
        return annotationMap[name]?.firstOrNull()
    }

    abstract class Parser {

        abstract fun extractTypeVariableNames(targetInterfaceElement: TypeElement): List<TypeVariableName>

        abstract fun parse(targetInterfaceElement: TypeElement): List<ViewMethod>

        protected fun List<AnnotationMirror>.toAnnotationDataList(): List<AnnotationData> {
            return map { annotation ->
                val name = annotation.annotationType.asElement().toString()
                val params = annotation.elementValues
                        .asSequence()
                        .map { entry ->
                            val key = entry.key.simpleName.toString()
                            val value = entry.value.value.toString()
                            key to value
                        }
                        .toMap()
                AnnotationData(name, params)
            }
        }

    }

    sealed class Type {
        data class Method(val setterSpec: FunSpec, val getterSpec: FunSpec? = null) : Type()
        data class Property(val spec: PropertySpec) : Type()
    }

    data class AnnotationData(val name: String, val params: Map<String, String>) {

        fun getValue(key: String) = params[key]

        inline fun <reified E : Enum<E>> getValueAsEnum(key: String): E? {
            return try {
                getValue(key)?.let { enumValueOf<E>(it) }
            } catch (e: Exception) {
                null
            }
        }

        fun getValueAsTypeElement(key: String, elements: Elements): TypeElement? {
            return getValue(key)?.let { elements.getTypeElement(it) }
        }

        fun getValueAsBoolean(key: String): Boolean? {
            return getValue(key)?.toBooleanStrictOrNull()
        }

    }

}