package com.omegar.mvp.compiler.viewstate


import com.omegar.mvp.compiler.pipeline.KotlinFileProcessor
import com.omegar.mvp.Moxy
import com.omegar.mvp.MvpView
import com.omegar.mvp.compiler.entity.*
import com.omegar.mvp.compiler.entity.ViewMethod.Type.*
import com.omegar.mvp.compiler.pipeline.PipelineContext
import com.omegar.mvp.compiler.pipeline.Publisher
import com.omegar.mvp.viewstate.MvpViewState
import com.omegar.mvp.viewstate.ViewCommand
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.lang.IllegalArgumentException
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * Date: 18.12.2015
 * Time: 13:24
 *
 * @author Yuri Shmakov
 */
class ViewInterfaceInfoToViewStateJavaFileProcessor(private val mElements: Elements,
                                                    private val mTypes: Types,
                                                    private val mCurrentMoxyReflectorPackage: String,
                                                    private val mReflectorPackagesPublisher: Publisher<String>) : KotlinFileProcessor<ViewInterfaceInfo?>() {


    companion object {
        private const val VIEW = "OMEGAVIEW"
        private val CLASS_NAME_MVP_VIEW = MvpView::class.java.canonicalName
        private val GENERIC_TYPE_VARIABLE_NAME: TypeVariableName = TypeVariableName(VIEW)
        private val MVP_VIEW_STATE_CLASS_NAME = MvpViewState::class.java.asClassName()
        private val VIEW_COMMAND_CLASS_NAME = ViewCommand::class.java.asClassName()
        private val VIEW_COMMAND_TYPE_NAME: ParameterizedTypeName = VIEW_COMMAND_CLASS_NAME.parameterizedBy(GENERIC_TYPE_VARIABLE_NAME)
        private val MVP_VIEW_STATE_TYPE_NAME: ParameterizedTypeName = MVP_VIEW_STATE_CLASS_NAME.parameterizedBy(GENERIC_TYPE_VARIABLE_NAME)
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
            val commandClass = generateCommandClass(command, variableNames)
            classBuilder.addType(commandClass)

            when (command.method.type) {
                is Method -> {
                    classBuilder.addFunction(
                            generateSetterBuilder(command, commandClass, command.method.type.setterSpec.toBuilder())
                                    .addModifiers(KModifier.OVERRIDE)
                                    .build()
                    )
                    if (command.method.type.getterSpec != null) {
                        classBuilder.addFunction(
                                generateGetterBuilder(command, command.method.type.getterSpec.toBuilder())
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
                            .setter(generateSetterBuilder(command, commandClass, setter).build())
                            .getter(generateGetterBuilder(command, getter).build())
                            .initializer(null)
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

    private fun generateCommandClass(command: ViewCommandInfo, variableNames: List<TypeVariableName>): TypeSpec {

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
                .returns(Unit::class)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("mvpView", GENERIC_TYPE_VARIABLE_NAME)
        when (command.method.type) {
            is Property -> {
                applyMethodBuilder.addStatement("mvpView.%L = %L", command.method.name, command.method.argumentsString)
            }
            is Method -> {
                applyMethodBuilder.addStatement("mvpView.%L(%L)", command.method.name, command.method.argumentsString)
            }
        }

        val classBuilder = TypeSpec.classBuilder(command.name)
                .addOriginatingElement(command.method.element)
                .addModifiers(KModifier.PRIVATE, KModifier.FINAL, KModifier.INNER)
                .primaryConstructor(
                        FunSpec.constructorBuilder()
                                .addParameters(command.method.parameterSpecs)
                                .build()
                )
                .superclass(VIEW_COMMAND_TYPE_NAME)
                .addSuperclassConstructorParameter(CodeBlock.of("%S, %T::class.java", command.tag, command.strategy))
                .addFunction(applyMethodBuilder.build())
        if (updateMethodBuilder != null) {
            classBuilder.addFunction(updateMethodBuilder.build())
        }
        classBuilder.addFunction(generateToStringMethodSpec(command))
        for (parameter in command.method.parameterSpecs) {
            classBuilder.addProperty(
                    PropertySpec.builder(parameter.name, parameter.type)
                            .addModifiers(KModifier.INTERNAL)
                            .mutable(command.singleInstance)
                            .initializer("%1N", parameter)
                            .build()
            )
        }
        return classBuilder.build()
    }


    private fun generateSetterBuilder(command: ViewCommandInfo, commandClass: TypeSpec, funSpec: FunSpec.Builder): FunSpec.Builder {
        val commandClassName = command.name
        val builder = funSpec.clearBody()
        builder.modifiers.remove(KModifier.ABSTRACT)
        builder.annotations.clear()
        if (command.singleInstance) {

            builder.addStatement("val command = findCommand<%1L>(%1L::class.java)", commandClassName)
                    .beginControlFlow("if (command == null)")
                    .addStatement("apply(%1N(%2L))", commandClass, command.method.argumentsString)
                    .nextControlFlow("else")
                    .addStatement("command.update(%L)", command.method.argumentsString)
                    .addStatement("apply(command)")
                    .endControlFlow()
        } else {
            builder.addStatement("apply(%1N(%2L))", commandClass, command.method.argumentsString)
        }
        return builder
    }

    private fun generateGetterBuilder(command: ViewCommandInfo, funSpec: FunSpec.Builder): FunSpec.Builder {
        val returnType = command.method.parameterSpecs.firstOrNull()?.type
        val defaultValue = when ((returnType as? ParameterizedTypeName)?.rawType ?: returnType) {
            BOOLEAN -> "false"
            FLOAT -> "0f"
            CHAR -> "\\u0000"
            DOUBLE -> "0.0"
            BYTE, SHORT, INT, LONG -> "0"
            STRING -> "\"\""
            LIST -> "emptyList()"
            MAP -> "emptyMap()"
            SET -> "emptySet()"
            ARRAY -> "emptyArray()"
            MUTABLE_MAP -> "mutableMapOf()"
            MUTABLE_LIST -> "mutableListOf()"
            MUTABLE_SET -> "mutableSetOf()"
            else -> "null"
        }
        val commandClassName = command.name
        val builder = funSpec.clearBody()
        builder.modifiers.remove(KModifier.ABSTRACT)
        builder.annotations.clear()
//        if (command.method.name == "list") {
//            throw IllegalArgumentException(returnType.toString())
//        }
        if ("null" == defaultValue || returnType?.isNullable == true) {
            builder.addStatement("return findCommand<%1L>(%1L::class.java)?.%2L", commandClassName, command.method.argumentsString)
        } else {
            builder.addStatement("return findCommand<%1L>(%1L::class.java)?.%2L ?: %3L", commandClassName, command.method.argumentsString, defaultValue)
        }
        return builder
    }

    private fun generateToStringMethodSpec(command: ViewCommandInfo): FunSpec {
        val statement = StringBuilder("return ")
        if (command.method.parameterSpecs.isEmpty()) {
            statement.append("\"")
                    .append(command.name)
                    .append("\"")
        } else {
            var firstParams = true
            statement.append("buildString(")
                    .append("\"")
                    .append(command.name)
                    .append("\",")
            for (parameter in command.method.parameterSpecs) {
                if (firstParams) {
                    firstParams = false
                } else {
                    statement.append(",")
                }
                statement.append("\"")
                        .append(parameter.name)
                        .append("\",")
                        .append(parameter.name)
            }
            statement.append(")")
        }
        return FunSpec.builder("toString")
                .addModifiers(KModifier.OVERRIDE)
                .addStatement(statement.toString())
                .build()
    }

    override fun finish(nextContext: PipelineContext<FileSpec>) {
        mReflectorPackagesPublisher.finish()
        super.finish(nextContext)
    }

}