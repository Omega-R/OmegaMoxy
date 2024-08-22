package com.omegar.mvp.compiler.processors

import com.google.devtools.ksp.processing.KSPLogger
import com.omegar.mvp.MvpDelegate
import com.omegar.mvp.compiler.NamingRules.presenterStateName
import com.omegar.mvp.compiler.NamingRules.propertyFlowName
import com.omegar.mvp.compiler.entities.View
import com.omegar.mvp.compiler.extensions.asClassName
import com.omegar.mvp.compiler.extensions.safeParameterizedBy
import com.omegar.mvp.compiler.extensions.toFileSpecBuilder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KProperty

class PresenterStateGenerator(val logger: KSPLogger) : Processor<View, FileSpec> {

    companion object {
        private val MVP_PRESENTER_STATE_CLASS_NAME = ClassName("com.omegar.mvp.compose", "MvpPresenterState")
        private val BASE_VIEW = ClassName("com.omegar.mvp.compose.MvpPresenterState", "BaseStateMvpView")
        private val MUTABLE_SHARED_FLOW = ClassName("kotlinx.coroutines.flow", "MutableSharedFlow")
        private val FLOW = ClassName("kotlinx.coroutines.flow", "Flow")
        private val AS_SHARED_FLOW = ClassName("kotlinx.coroutines.flow", "asSharedFlow")
        private val COMPOSABLE = ClassName("androidx.compose.runtime", "Composable")
        private val REMEMBER_MVP = ClassName("com.omegar.mvp.compose", "rememberMvp")

        private const val FACTORY_PARAM_NAME = "factory"
        private const val VIEW_PROPERTY_NAME = "view"
        private const val MVP_DELEGATE_NAME = "mvpDelegate"
        private const val PRESENTER_PROPERTY_NAME = "presenter"
        private const val BLOCK_POSTFIX_PARAM = "Block"

    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun invoke(view: View): FileSpec {

        val presenterClassName = view.presenterClassParamsMap.entries.first().key

        val typeSpec = generatePresenterState(
            view,
            presenterClassName.safeParameterizedBy(view.presenterClassParamsMap.getValue(presenterClassName))
        )

        return typeSpec.toFileSpecBuilder(view.className.packageName)
            .addFunction(
                FunSpec.builder("providePresenterState")
                    .addAnnotation(COMPOSABLE)
                    .returns(ClassName(view.className.packageName, typeSpec.name!!).safeParameterizedBy(typeSpec.typeVariables))
                    .addParameters(typeSpec.primaryConstructor!!.parameters)
                    .addCode(CodeBlock.Builder()
                        .beginControlFlow("return %T", REMEMBER_MVP)
                        .add("${typeSpec.name!!}(${typeSpec.primaryConstructor!!.parameters.joinToString { it.name }})\n")
                        .endControlFlow()
                        .build()
                    )
                    .apply {
                        if (typeSpec.typeVariables.isNotEmpty()) {
                            addTypeVariables(typeSpec.typeVariables)
                        }
                    }
                    .build()
            )
            .build()
    }

    private fun generatePresenterState(view: View, presenterTypeName: TypeName): TypeSpec {
        val viewTypePresenterParams = view.viewTypePresenterParams.getValue(presenterTypeName.asClassName()!!)
        val blockParams =
            generateSequence(view) { it.parent }
                .flatMap { view ->
                    view.methods.map {
                        if (it.type is View.Method.Type.Function) {
                            it.copy(
                                type = View.Method.Type.Function(
                                    params = it.params.map {
                                        it.copy(typeName = it.typeName.resolveGeneric(view, viewTypePresenterParams))
                                    })
                            )
                        } else it
                    }
                }
                .filter { it.type is View.Method.Type.Function }
                .map {
                    val name = it.name + BLOCK_POSTFIX_PARAM
                    val type = LambdaTypeName.get(
                        returnType = UNIT,
                        parameters = it.params.map { ParameterSpec.unnamed(it.typeName) },
                    )

                    ParameterSpec.builder(name, type)
                        .build()
                }.toList()

        val presenterParams = view.presenterClassParamsMap[presenterTypeName.asClassName()!!]
        val viewInPresenterTypeName = view.className.safeParameterizedBy(viewTypePresenterParams)
        val internalViewTypeSpec = generateInternalViewTypeSpec(view, presenterTypeName, blockParams)
        val internalViewClassName = ClassName("", internalViewTypeSpec.name!!).safeParameterizedBy(presenterParams)
        val typeSpec = TypeSpec.classBuilder(view.presenterStateName)
            .addOriginating(view)
            .addTypeVariables(view.presenterClassParamsMap.getValue(presenterTypeName.asClassName()!!))
            .superclass(MVP_PRESENTER_STATE_CLASS_NAME.parameterizedBy(viewInPresenterTypeName))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(FACTORY_PARAM_NAME, LambdaTypeName.get(returnType = presenterTypeName)).build()
                    )
                    .addParameters(blockParams)
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder(VIEW_PROPERTY_NAME, internalViewClassName)
                    .initializer(blockParams.joinToString(prefix = "$internalViewClassName(factory, ", postfix = ")") { it.name })
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .addProperty(
                PropertySpec.builder(PRESENTER_PROPERTY_NAME, presenterTypeName)
                    .getter(FunSpec.getterBuilder().addCode("return $VIEW_PROPERTY_NAME.$PRESENTER_PROPERTY_NAME").build())
                    .build()
            )
            .addProperty(
                PropertySpec.builder(
                    MVP_DELEGATE_NAME,
                    MvpDelegate::class.asTypeName().parameterizedBy(viewInPresenterTypeName),
                    KModifier.OVERRIDE
                )
                    .getter(FunSpec.getterBuilder().addCode("return $VIEW_PROPERTY_NAME.$MVP_DELEGATE_NAME").build())
                    .build()
            )
            .apply {
                generateSequence(view) { it.parent }
                    .flatMap { view -> view.methods }
                    .filter { it.type is View.Method.Type.Property }
                    .forEach { method ->
                        addProperty(
                            PropertySpec.builder(
                                method.propertyFlowName,
                                FLOW.parameterizedBy(
                                    (method.type as View.Method.Type.Property).param.typeName.resolveGeneric(
                                        view,
                                        viewTypePresenterParams
                                    )
                                )
                            )
                                .initializer("$VIEW_PROPERTY_NAME.${method.propertyFlowName}.%T()", AS_SHARED_FLOW)
                                .build()
                        )
                    }
            }
            .addType(internalViewTypeSpec)
            .build()
        return typeSpec
    }

    private fun TypeName.resolveGeneric(currentView: View, viewTypePresenterParams: List<TypeName>): TypeName {
        return when (this) {
            is TypeVariableName -> {
                val index = currentView.viewTypeParams.indexOf(this)
                if (index >= 0) {
                    viewTypePresenterParams.getOrNull(index) ?: this
                } else this
            }

            is ParameterizedTypeName -> {
                copy(typeArguments = typeArguments.map { it.resolveGeneric(currentView, viewTypePresenterParams) })
            }

            else -> this
        }

    }

    private val TypeName.simpleName: String
        get() {
            return when (this) {
                is ParameterizedTypeName -> rawType.simpleName
                is ClassName -> simpleName
                else -> toString()
            }
        }

    private fun generateInternalViewTypeSpec(view: View, presenterTypeName: TypeName, blocks: List<ParameterSpec>): TypeSpec {
        val viewTypePresenterParams = view.viewTypePresenterParams.getValue(presenterTypeName.asClassName()!!)
        val viewTypeName = view.className.safeParameterizedBy(viewTypePresenterParams)

        val typeSpec = TypeSpec.classBuilder("InternalView")
            .addModifiers(KModifier.PRIVATE)
            .addTypeVariables(view.presenterClassParamsMap.getValue(presenterTypeName.asClassName()!!))
            .superclass(BASE_VIEW.parameterizedBy(viewTypeName))
            .addSuperinterface(viewTypeName)
            .apply {
//                addProperty(
//                    PropertySpec.builder("test", STRING)
//                        .initializer(
//                            "\"viewTypeResolvedParams = " + view.viewTypeResolvedParams + ",\"+\n\"" +
//                                    "viewTypePresenterParams[] = " + viewTypePresenterParams + ",\"+\n\"" +
//                                    "presenterClassParamsMap[] = " + view.presenterClassParamsMap.getValue(presenterTypeName.asClassName()!!) + ",\"+\n\"" +
//                                    "viewTypeParams = " + view.viewTypeParams + "\""
//                        )
//                        .build()
//                )
                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(
                            ParameterSpec.builder(FACTORY_PARAM_NAME, LambdaTypeName.get(returnType = presenterTypeName)).build()
                        )
                        .apply {
                            blocks.forEach {
                                addProperty(
                                    PropertySpec.builder(it.name, it.type, KModifier.PRIVATE).initializer(it.name).build()
                                )
                                addParameter(it)
                            }
                        }
                        .build()
                )
            }
            .addProperty(
                PropertySpec.builder(PRESENTER_PROPERTY_NAME, presenterTypeName)
                    .delegate("provide${presenterTypeName.simpleName}(factoryBlock = $FACTORY_PARAM_NAME)")
                    .build()
            )
            .apply {
                generateSequence(view) { it.parent }
                    .flatMap { it.methods }
                    .forEach {
                        when (it.type) {
                            is View.Method.Type.Property -> {
                                val resolvedTypeName = it.type.param.typeName.resolveGeneric(view, viewTypePresenterParams)
                                addProperty(
                                    PropertySpec.builder(
                                        it.propertyFlowName,
                                        MUTABLE_SHARED_FLOW.parameterizedBy(resolvedTypeName)
                                    )
                                        .initializer("MutableSharedFlow()")
                                        .build()
                                )
                                addProperty(
                                    PropertySpec.builder(it.name, resolvedTypeName)
                                        .mutable(true)
                                        .addModifiers(KModifier.OVERRIDE)
                                        .delegate("FlowDelegate(" + it.propertyFlowName + ")")
                                        .build()
                                )
                            }

                            is View.Method.Type.Function -> {
                                addFunction(
                                    FunSpec.builder(it.name)
                                        .apply {
                                            it.params.forEach {
                                                addParameter(it.name, it.typeName.resolveGeneric(view, viewTypePresenterParams))
                                            }
                                        }
                                        .addModifiers(KModifier.OVERRIDE)
                                        .addCode(
                                            it.params.joinToString(
                                                prefix = it.name + BLOCK_POSTFIX_PARAM + "(",
                                                postfix = ")",
                                                transform = View.Method.Param::name
                                            )
                                        )
                                        .build()
                                )
                            }
                        }
                    }
            }
            .build()

        return typeSpec
    }
}