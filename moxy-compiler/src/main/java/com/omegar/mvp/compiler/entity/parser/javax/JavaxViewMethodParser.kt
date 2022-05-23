package com.omegar.mvp.compiler.entity.parser.javax

import com.omegar.mvp.compiler.Util
import com.omegar.mvp.compiler.entity.ViewMethod
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Types
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

class JavaxViewMethodParser(private val types: Types) : ViewMethod.Parser() {

    companion object {
        private const val PREFIX_SETTER = "set"
        private const val PREFIX_GETTER = "get"
    }

    override fun extractTypeVariableNames(targetInterfaceElement: TypeElement): List<TypeVariableName> {
        return targetInterfaceElement.typeParameters.map { it.asTypeVariableName().javaToKotlinType() }
    }

    override fun parse(targetInterfaceElement: TypeElement): List<ViewMethod> {
        val nonVoidMethods = mutableMapOf<String, ExecutableElement>()
        val viewInterfaceType = targetInterfaceElement.asType() as DeclaredType

        targetInterfaceElement.enclosedElements
                .filterIsInstance(ExecutableElement::class.java)
                .filter { it.kind == ElementKind.METHOD && it.returnType.kind != TypeKind.VOID }
                .forEach {
                    nonVoidMethods[it.simpleName.toString()] = it
                }

        return targetInterfaceElement.enclosedElements
                .filterIsInstance(ExecutableElement::class.java)
                .filter { it.kind == ElementKind.METHOD && it.returnType.kind == TypeKind.VOID }
                .map { create(it, nonVoidMethods, viewInterfaceType) }
    }

    private fun create(
            methodElement: ExecutableElement,
            nonVoidMethods: Map<String, ExecutableElement>,
            viewInterfaceType: DeclaredType
    ): ViewMethod {
        val methodName = methodElement.simpleName.toString()
        val parameters = methodElement.parameters
        val parameterSpecs = parameters.mapParametersSpecs(viewInterfaceType, methodElement)
        val exceptions = methodElement.thrownTypes.map { it.asTypeName().javaToKotlinType() }
        val typeVariables = methodElement.typeParameters.map { it.asTypeVariableName().javaToKotlinType() }

        val setter = FunSpec.overriding(methodElement, viewInterfaceType, types)
                .javaToKotlinType()
                .build()

        var methodType = ViewMethod.Type.Method(setter)

        if (methodElement.parameters.size == 1 && methodName.startsWith(PREFIX_SETTER)) {
            val methodNameWithoutPrefix = methodName.removePrefix(PREFIX_SETTER)

            nonVoidMethods[PREFIX_GETTER + methodNameWithoutPrefix]?.let { getterElement ->

                val paramTypeMirror = parameters.first().asType()

                if ((paramTypeMirror is DeclaredType && paramTypeMirror.asElement() == Util.asElement(getterElement.returnType))
                        || parameters[0].asType() == getterElement.returnType) {
                    val getter = FunSpec.overriding(getterElement, viewInterfaceType, types)
                            .javaToKotlinType()
                            .build()

                    methodType = ViewMethod.Type.Method(setter, getter)
                }
            }

        }

        val annotationDataList = methodElement.annotationMirrors.toAnnotationDataList()

        return ViewMethod(methodName, parameterSpecs, exceptions, typeVariables, methodType, annotationDataList)
    }

    private fun <T : VariableElement> List<T>.mapParametersSpecs(
            targetInterfaceElement: DeclaredType,
            methodElement: ExecutableElement
    ): List<ParameterSpec> {
        val executableType = types.asMemberOf(targetInterfaceElement, methodElement) as ExecutableType
        val resolvedParameterTypes = executableType.parameterTypes

        return mapIndexed { index, element ->
            val type = resolvedParameterTypes[index]!!.asTypeName().javaToKotlinType()
            val name = element.simpleName.toString()
            ParameterSpec.builder(name, type)
                    .addModifiers(element.modifiers.map { modifier -> KModifier.valueOf(modifier.name) })
                    .build()

        }
    }

    private fun TypeName.javaToKotlinType(): TypeName {
        return if (this is ParameterizedTypeName) {
            (rawType.javaToKotlinType() as ClassName)
                    .parameterizedBy(*typeArguments.map { it.javaToKotlinType() }.toTypedArray())
        } else {
            val className =
                    JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))
                            ?.asSingleFqName()?.asString()

            return if (className == null) {
                this
            } else {
                ClassName.bestGuess(className)
            }
        }
    }

    private fun TypeVariableName.javaToKotlinType(): TypeVariableName{
        return copy(bounds = bounds.map { it.javaToKotlinType() })
    }

    private fun MutableList<ParameterSpec>.javaToKotlinType() {
        val params = map { it.toBuilder(type = it.type.javaToKotlinType()).build() }
        clear()
        addAll(params)
    }

    private fun List<ParameterSpec>.javaToKotlinType(): List<ParameterSpec> {
        return map { it.toBuilder(type = it.type.javaToKotlinType()).build() }
    }

    private fun FunSpec.Builder.javaToKotlinType() = apply {
        parameters.javaToKotlinType()
        build().returnType?.javaToKotlinType()?.let {
            returns(it)
        }
    }

}