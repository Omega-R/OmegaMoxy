package com.omegar.mvp.compiler.viewstate


import com.omegar.mvp.Moxy
import com.omegar.mvp.MvpView
import com.omegar.mvp.compiler.entity.ViewCommandInfo
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo
import com.omegar.mvp.compiler.entity.ViewMethod.Type.Method
import com.omegar.mvp.compiler.entity.ViewMethod.Type.Property
import com.omegar.mvp.compiler.pipeline.KotlinFileProcessor
import com.omegar.mvp.compiler.pipeline.PipelineContext
import com.omegar.mvp.compiler.pipeline.Publisher
import com.omegar.mvp.viewstate.MoxyDefaultValue
import com.omegar.mvp.viewstate.MvpViewState
import com.omegar.mvp.viewstate.SerializeType
import com.omegar.mvp.viewstate.ViewCommand
import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.MUTABLE_SET
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * Date: 18.12.2015
 * Time: 13:24
 *
 * @author Yuri Shmakov
 */
class ViewInterfaceInfoToViewStateJavaFileProcessor(
        private val mElements: Elements,
        private val mTypes: Types,
        private val mCurrentMoxyReflectorPackage: String,
        private val mReflectorPackagesPublisher: Publisher<String>
) : KotlinFileProcessor<ViewInterfaceInfo?>() {


    companion object {
        private const val VIEW = "OMEGAVIEW"
        private val CLASS_NAME_MVP_VIEW = MvpView::class.java.canonicalName
        private val GENERIC_TYPE_VARIABLE_NAME: TypeVariableName = TypeVariableName(VIEW)
        private val MVP_VIEW_STATE_CLASS_NAME = MvpViewState::class.java.asClassName()
        private val VIEW_COMMAND_CLASS_NAME = ViewCommand::class.java.asClassName()
        private val ANNOTATION_PARCELIZE = ClassName("kotlinx.android.parcel", "Parcelize")
        private val CLASS_NAME_PARCELABLE = ClassName("android.os", "Parcelable")
        private val CLASS_NAME_SERIALIZABLE = ClassName("java.io", "Serializable")
        private val DURATION = ClassName("kotlin.time", "Duration")
        private val SECONDS = MemberName(ClassName("kotlin.time", "Duration", "Companion"), "seconds")

        private val DEFAULT_VALUE_TYPE_ANNOTATION = MoxyDefaultValue::class.java.name

        private val VIEW_COMMAND_TYPE_NAME = VIEW_COMMAND_CLASS_NAME.parameterizedBy(GENERIC_TYPE_VARIABLE_NAME)
        private val MVP_VIEW_STATE_TYPE_NAME = MVP_VIEW_STATE_CLASS_NAME.parameterizedBy(GENERIC_TYPE_VARIABLE_NAME)
    }


    @OptIn(KotlinPoetMetadataPreview::class)
    override fun process(viewInterfaceInfo: ViewInterfaceInfo?): FileSpec {
        val viewName = viewInterfaceInfo!!.name
        val nameWithTypeVariables = viewInterfaceInfo.nameWithTypeVariables
        val variableName = TypeVariableName(VIEW, nameWithTypeVariables)
        val variableNames = listOf(variableName) + viewInterfaceInfo.typeVariables


        val className = viewInterfaceInfo.getViewStateSimpleName(mElements)
        val classBuilder = TypeSpec.classBuilder(className)
                .addOriginatingElement(viewInterfaceInfo.typeElement)
                .addAnnotation(
                        AnnotationSpec.builder(Moxy::class.java)
                                .addMember("reflectorPackage=\"%L\"", mCurrentMoxyReflectorPackage)
                                .build()
                )
                .addModifiers(KModifier.OPEN)
                .addSuperinterfaces(setOf(nameWithTypeVariables))
                .addTypeVariables(variableNames)
        val superInfoElement = viewInterfaceInfo.superInterfaceType
        if (superInfoElement == null || superInfoElement.qualifiedName.toString() == CLASS_NAME_MVP_VIEW) {
            classBuilder.superclass(MVP_VIEW_STATE_TYPE_NAME)
        } else {
            val superViewState = ViewInterfaceInfo.getViewStateFullName(mElements, superInfoElement)
            val superClassName: ClassName = ClassName.bestGuess(superViewState)
            checkReflectorPackages(superViewState)
            classBuilder.superclass(
                    superClassName.parameterizedBy(*generateSuperClassTypeVariables(viewInterfaceInfo, variableName))
            )
        }

        viewInterfaceInfo.commands.forEach { command ->
            val commandClass = generateCommandClass(viewInterfaceInfo.typeElement, command, variableNames)
            classBuilder.addType(commandClass)

            when (command.method.type) {
                is Method -> {
                    classBuilder.addFunction(
                            generateSetterBuilder(command, commandClass, command.method.type.setterSpec.toBuilder(), variableNames)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .build()
                    )
                    if (command.method.type.getterSpec != null) {
                        classBuilder.addFunction(
                                generateGetterBuilder(command, command.method.type.getterSpec.toBuilder(), variableNames)
                                        .addModifiers(KModifier.OVERRIDE)
                                        .build()
                        )
                    }
                }
                is Property -> {
                    val setter = FunSpec.setterBuilder()
                            .addParameter(command.method.parameterSpecs.first())
                    val getter = FunSpec.getterBuilder()
                    val property = command.method.type.spec.toBuilder()
                            .addModifiers(KModifier.OVERRIDE)
                            .setter(generateSetterBuilder(command, commandClass, setter, variableNames).build())
                            .getter(generateGetterBuilder(command, getter, variableNames).build())
                            .initializer(null)
                            .also {
                                it.annotations.clear()
                            }
                            .build()
                    classBuilder.addProperty(property)
                }
            }

        }

        return FileSpec.builder(viewName.packageName, className)
                .addType(classBuilder.build())
                .indent("\t")
                .build()
    }

    private fun checkReflectorPackages(superViewState: CharSequence) {
        val typeElement = mElements.getTypeElement(superViewState)
        if (typeElement != null) {
            val moxies = typeElement.getAnnotationsByType(Moxy::class.java)
            if (moxies != null) {
                for (moxy in moxies) {
                    mReflectorPackagesPublisher.next(moxy.reflectorPackage)
                }
            }
        }
    }

    private fun generateSuperClassTypeVariables(viewInterfaceInfo: ViewInterfaceInfo, variableName: TypeVariableName): Array<TypeVariableName> {
        return (listOf(variableName) + viewInterfaceInfo.parentTypeVariables).toTypedArray()
    }

    private fun generateCommandClass(typeElement: TypeElement, command: ViewCommandInfo, variableNames: List<TypeVariableName>):
            TypeSpec {
        val mvpView = "mvpView"
        var updateMethodBuilder: FunSpec.Builder? = null
        if (command.singleInstance) {
            updateMethodBuilder = FunSpec.builder("update")
                    .addModifiers(KModifier.INTERNAL)
                    .addParameters(command.method.parameterSpecs)
            for (typeVariable in command.method.parameterSpecs) {
                updateMethodBuilder.addStatement("this.%1L = %1L", typeVariable.name)
            }
        }

        val applyMethodBuilder = FunSpec.builder("apply")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(mvpView, GENERIC_TYPE_VARIABLE_NAME)
        when (command.method.type) {
            is Property -> {
                applyMethodBuilder.addStatement("$mvpView.%L = %L", command.method.name, command.method.argumentsStringWithStar)
            }
            is Method -> {
                applyMethodBuilder.addStatement("$mvpView.%L(%L)", command.method.name, command.method.argumentsStringWithStar)
            }
        }

        val parameterSpecs = command.method.parameterSpecs.map {
            if (KModifier.VARARG in it.modifiers) {
                it.toBuilder(type = ARRAY.parameterizedBy(WildcardTypeName.producerOf(it.type)))
                        .apply { modifiers.remove(KModifier.VARARG) }
                        .build()
            } else it
        }

        val classBuilder = TypeSpec.classBuilder(command.name)
                .addOriginatingElement(typeElement)
                .addModifiers(KModifier.PRIVATE)
                .addTypeVariables(variableNames)
                .primaryConstructor(
                        FunSpec.constructorBuilder()
                                .addParameters(parameterSpecs)
                                .build()
                )
                .superclass(VIEW_COMMAND_TYPE_NAME)
                .addSuperclassConstructorParameter(CodeBlock.of("%S, %T", command.tag, command.strategy.asClassName()))
                .addFunction(applyMethodBuilder.build())

        if (updateMethodBuilder != null) {
            classBuilder.addFunction(updateMethodBuilder.build())
        }
        if (command.method.parameterSpecs.isNotEmpty()) {
            classBuilder.addFunction(generateToStringMethodSpec(command))
        }
        when (command.serializeType) {
            SerializeType.PARCELABLE -> {
                classBuilder.addSuperinterface(CLASS_NAME_PARCELABLE)
                        .addAnnotation(ANNOTATION_PARCELIZE)
            }
            SerializeType.SERIALIZABLE -> classBuilder.addSuperinterface(CLASS_NAME_SERIALIZABLE)
            null, SerializeType.NONE -> {
                // nothing
            }
        }
        for (parameter in parameterSpecs) {
            classBuilder.addProperty(
                    PropertySpec.builder(parameter.name, parameter.type)
                            .mutable(command.singleInstance)
                            .initializer("%1N", parameter)
                            .build()
            )
        }
        return classBuilder.build()
    }

    private fun generateSetterBuilder(command: ViewCommandInfo, commandClass: TypeSpec, funSpec: FunSpec.Builder, variableNames: List<TypeVariableName>): FunSpec.Builder {
        val commandClassName = command.name
        val builder = funSpec.clearBody()
        builder.modifiers.remove(KModifier.ABSTRACT)
        val parameters = builder.parameters.map {
            it.toBuilder()
                    .defaultValue(null)
                    .build()
        }
        builder.parameters.clear()
        builder.parameters.addAll(parameters)
        builder.annotations.clear()
        if (command.existsDefaultImpl) {
            when (command.method.type) {
                is Property -> {
                    builder.addStatement("super.${command.method.name} = ${command.method.argumentsString}")
                }
                is Method -> {
                    builder.addStatement("super.${command.method.name}(${command.method.argumentsStringWithStar})")
                }
            }
        }
        if (command.singleInstance) {
            builder.addCode(
                    "apply(findCommand<%1L<%4L>>()?.also { it.update(%2L) } ?: %1L(%3L))",
                    commandClassName,
                    command.method.argumentsStringWithStar,
                    command.method.argumentsString,
                    variableNames.joinToString(separator = ",") { it.name }
            )

        } else {
            builder.addStatement("apply(%1N(%2L))", commandClass, command.method.argumentsString)
        }
        return builder
    }

    private fun generateGetterBuilder(command: ViewCommandInfo, funSpec: FunSpec.Builder, variableNames: List<TypeVariableName>): FunSpec.Builder {
        val returnType = command.method.parameterSpecs.firstOrNull()?.type
        val defaultValue = getDefaultValue(command, returnType)
        val commandClassName = command.name
        val builder = funSpec.clearBody()
        builder.modifiers.remove(KModifier.ABSTRACT)
        builder.annotations.clear()
        val generics = variableNames.joinToString(separator = ",") { it.name }
        if ("null" == defaultValue.toString() || returnType?.isNullable == true) {
            builder.addStatement("return findCommand<%1L<%3L>>()?.%2L", commandClassName, command.method.argumentsStringWithStar, generics)
        } else {
            builder.addStatement("return findCommand<%1L<%4L>>()?.%2L ?: %3L", commandClassName, command.method
                    .argumentsStringWithStar, defaultValue, generics)
        }
        return builder
    }

    private fun getDefaultValue(command: ViewCommandInfo, returnType: TypeName?): CodeBlock {
        return command.method.getAnnotation(DEFAULT_VALUE_TYPE_ANNOTATION)
                ?.getValue("value")?.let {
                    CodeBlock.of(it)
                }
                ?: command.getSuperDefaultValue()
                ?: returnType.determineDefaultValue()
    }

    private fun ViewCommandInfo.getSuperDefaultValue(): CodeBlock? {
        return if (existsDefaultImpl) CodeBlock.of("super.${method.name}") else null
    }

    private fun TypeName?.determineDefaultValue(): CodeBlock {
        return when ((this as? ParameterizedTypeName)?.rawType ?: this) {
            BOOLEAN -> CodeBlock.of("false")
            FLOAT -> CodeBlock.of("0f")
            CHAR -> CodeBlock.of("\\u0000")
            DOUBLE -> CodeBlock.of("0.0")
            BYTE, SHORT, INT, LONG -> CodeBlock.of("0")
            STRING -> CodeBlock.of("\"\"")
            LIST -> CodeBlock.of("emptyList()")
            MAP -> CodeBlock.of("emptyMap()")
            SET -> CodeBlock.of("emptySet()")
            ARRAY -> CodeBlock.of("emptyArray()")
            MUTABLE_MAP -> CodeBlock.of("mutableMapOf()")
            MUTABLE_LIST -> CodeBlock.of("mutableListOf()")
            MUTABLE_SET -> CodeBlock.of("mutableSetOf()")
            DURATION -> CodeBlock.of("0.%M", SECONDS)
            else -> CodeBlock.of("null")
        }
    }

    private fun generateToStringMethodSpec(command: ViewCommandInfo): FunSpec {
        val statement = command.method.parameterSpecs.joinToString(
                prefix = "return buildString(\"${command.name}\",",
                separator = ",",
                postfix = ")") { parameter ->
            "\"${parameter.name}\",${parameter.name}"
        }

        return FunSpec.builder("toString")
                .addModifiers(KModifier.OVERRIDE)
                .addStatement(statement)
                .build()
    }


    override fun finish(nextContext: PipelineContext<FileSpec>) {
        mReflectorPackagesPublisher.finish()
        super.finish(nextContext)
    }

}