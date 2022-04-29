package com.omegar.mvp.compiler.viewstate;

import com.omegar.mvp.Moxy;
import com.omegar.mvp.MvpView;
import com.omegar.mvp.compiler.entity.ReturnViewMethod;
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo;
import com.omegar.mvp.compiler.entity.CommandViewMethod;
import com.omegar.mvp.compiler.entity.ViewMethod;
import com.omegar.mvp.compiler.pipeline.JavaFileProcessor;
import com.omegar.mvp.compiler.Util;
import com.omegar.mvp.compiler.pipeline.PipelineContext;
import com.omegar.mvp.compiler.pipeline.Publisher;
import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.omegar.mvp.compiler.Util.decapitalizeString;

/**
 * Date: 18.12.2015
 * Time: 13:24
 *
 * @author Yuri Shmakov
 */
public final class ViewInterfaceInfoToViewStateJavaFileProcessor extends JavaFileProcessor<ViewInterfaceInfo> {


    private static final String VIEW = "Omega$$View";
    private static final String CLASS_NAME_MVP_VIEW = MvpView.class.getCanonicalName();
    private static final TypeVariableName GENERIC_TYPE_VARIABLE_NAME = TypeVariableName.get(VIEW);
    private static final ClassName MVP_VIEW_STATE_CLASS_NAME = ClassName.get(MvpViewState.class);
    private static final ClassName VIEW_COMMAND_CLASS_NAME = ClassName.get(ViewCommand.class);
    private static final ParameterizedTypeName VIEW_COMMAND_TYPE_NAME
            = ParameterizedTypeName.get(VIEW_COMMAND_CLASS_NAME, GENERIC_TYPE_VARIABLE_NAME);
    private static final ParameterizedTypeName MVP_VIEW_STATE_TYPE_NAME
            = ParameterizedTypeName.get(MVP_VIEW_STATE_CLASS_NAME, GENERIC_TYPE_VARIABLE_NAME);

    private final Elements mElements;
    private final Types mTypes;
    private final String mCurrentMoxyReflectorPackage;

    private final Publisher<String> mReflectorPackagesPublisher;

    public ViewInterfaceInfoToViewStateJavaFileProcessor(Elements elements,
                                                         Types types,
                                                         String currentMoxyReflectorPackage,
                                                         Publisher<String> reflectorPackagesPublisher) {
        mElements = elements;
        mTypes = types;
        mCurrentMoxyReflectorPackage = currentMoxyReflectorPackage;
        mReflectorPackagesPublisher = reflectorPackagesPublisher;
    }

    @Override
    public JavaFile process(ViewInterfaceInfo viewInterfaceInfo) {
        ClassName viewName = viewInterfaceInfo.getName();

        TypeName nameWithTypeVariables = viewInterfaceInfo.getNameWithTypeVariables();
        DeclaredType viewInterfaceType = (DeclaredType) viewInterfaceInfo.getTypeElement().asType();
        TypeVariableName variableName = TypeVariableName.get(VIEW, nameWithTypeVariables);

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(viewInterfaceInfo.getViewStateSimpleName(mElements))
                .addOriginatingElement(viewInterfaceInfo.getTypeElement())
                .addAnnotation(
                        AnnotationSpec.builder(Moxy.class)
                                .addMember("reflectorPackage", "\"" + mCurrentMoxyReflectorPackage + "\"")
                                .build()
                )
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(nameWithTypeVariables)
                .addTypeVariables(new ArrayList<TypeVariableName>(viewInterfaceInfo.getTypeVariables()) {{
                    add(0, variableName);
                }});

        TypeElement superInfoElement = viewInterfaceInfo.getSuperInterfaceType();

        if (superInfoElement == null || superInfoElement.getQualifiedName().toString().equals(CLASS_NAME_MVP_VIEW)) {
            classBuilder.superclass(MVP_VIEW_STATE_TYPE_NAME);
        } else {
            String superViewState = ViewInterfaceInfo.getViewStateFullName(mElements, superInfoElement);
            ClassName superClassName = ClassName.bestGuess(superViewState);
            checkReflectorPackages(superViewState);
            classBuilder.superclass(
                    ParameterizedTypeName.get(superClassName, generateSuperClassTypeVariables(viewInterfaceInfo, variableName))
            );
        }

        for (ViewMethod method : viewInterfaceInfo.getMethods()) {
            if (method instanceof CommandViewMethod) {
                CommandViewMethod commandViewMethod = (CommandViewMethod) method;
                TypeSpec commandClass = generateCommandClass(commandViewMethod, new ArrayList<TypeVariableName>(viewInterfaceInfo.getTypeVariables()) {{
                    add(0, variableName);
                }});

                classBuilder.addType(commandClass);
                classBuilder.addMethod(generateVoidMethod(viewInterfaceType, commandViewMethod, commandClass));
            } else if (method instanceof ReturnViewMethod) {
                ReturnViewMethod returnViewMethod = (ReturnViewMethod) method;
                ExecutableElement setterElement = returnViewMethod.getSetterElement();
                CommandViewMethod setterMethod = null;
                for (ViewMethod viewMethod : viewInterfaceInfo.getMethods()) {
                    if (viewMethod instanceof CommandViewMethod) {
                        if (setterElement == viewMethod.getElement()) {
                            setterMethod = (CommandViewMethod) viewMethod;
                            break;
                        }
                    }
                }

                classBuilder.addMethod(generateReturnMethod(viewInterfaceType, returnViewMethod, setterMethod));
            }
        }

        return JavaFile.builder(viewName.packageName(), classBuilder.build())
                .indent("\t")
                .build();
    }

    private void checkReflectorPackages(CharSequence superViewState) {
        TypeElement typeElement = mElements.getTypeElement(superViewState);
        if (typeElement != null) {
            Moxy[] moxies = typeElement.getAnnotationsByType(Moxy.class);
            if (moxies != null) {
                for (Moxy moxy : moxies) {
                    mReflectorPackagesPublisher.next(moxy.reflectorPackage());
                }

            }
        }
    }

    private TypeVariableName[] generateSuperClassTypeVariables(ViewInterfaceInfo viewInterfaceInfo, TypeVariableName variableName) {
        List<TypeVariableName> parentClassTypeVariables = new ArrayList<>();
        parentClassTypeVariables.add(variableName);

        TypeMirror mirror = Util.firstOrNull(viewInterfaceInfo.getTypeElement().getInterfaces());
        if (mirror != null) {
            List<? extends TypeMirror> typeArguments = ((DeclaredType) mirror).getTypeArguments();
            for (TypeMirror typeMirror : typeArguments) {
                TypeName typeName = ClassName.get(typeMirror);
                TypeVariableName name = TypeVariableName.get(typeMirror.toString(), typeName);
                parentClassTypeVariables.add(name);
            }
        }
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return parentClassTypeVariables.toArray(new TypeVariableName[parentClassTypeVariables.size()]);
    }

    private TypeSpec generateCommandClass(CommandViewMethod method, List<TypeVariableName> variableNames) {
        MethodSpec applyMethod = MethodSpec.methodBuilder("apply")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(GENERIC_TYPE_VARIABLE_NAME, "mvpView")
                .addExceptions(method.getExceptions())
                .addStatement("mvpView.$L($L)", method.getName(), method.getArgumentsString())
                .build();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(method.getCommandClassName())
                .addOriginatingElement(method.getElement())
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addTypeVariables(new ArrayList<TypeVariableName>(method.getTypeVariables()) {{
                    addAll(0, variableNames);
                }})
                .superclass(VIEW_COMMAND_TYPE_NAME)
                .addMethod(generateCommandConstructor(method))
                .addMethod(applyMethod)
                .addMethod(generateToStringMethodSpec(method));

        for (ParameterSpec parameter : method.getParameterSpecs()) {
            classBuilder.addField(parameter.type, parameter.name, Modifier.PRIVATE, Modifier.FINAL);
        }

        return classBuilder.build();
    }

    private MethodSpec generateVoidMethod(DeclaredType enclosingType, CommandViewMethod method, TypeSpec commandClass) {
        return MethodSpec.overriding(method.getElement(), enclosingType, mTypes)
                .addStatement("apply(new $1N($2L))", commandClass, method.getArgumentsString())
                .build();
    }

    private MethodSpec generateReturnMethod(DeclaredType enclosingType, ReturnViewMethod method, CommandViewMethod commandViewMethod) {
        String defaultValue;
        switch (method.getReturnType().getKind()) {
            case BOOLEAN:
                defaultValue = "false";
                break;
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case CHAR:
            case DOUBLE:
                defaultValue = "0";
                break;
            default:
                defaultValue = "null";
                break;
        }
        String commandClassName = commandViewMethod.getCommandClassName();
        return MethodSpec.overriding(method.getElement(), enclosingType, mTypes)
                .addStatement("$1L command = findCommand($1L.class)", commandClassName)
                .addStatement("return command != null ? command.$L : $L", commandViewMethod.getArgumentsString(), defaultValue)
                .build();
    }

    private MethodSpec generateCommandConstructor(CommandViewMethod method) {
        List<ParameterSpec> parameters = method.getParameterSpecs();

        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addParameters(parameters)
                .addModifiers(Modifier.PRIVATE)
                .addStatement("super($S, $T.class)", method.getTag(), method.getStrategy());

        for (ParameterSpec parameter : parameters) {
            builder.addStatement("this.$1N = $1N", parameter);
        }

        return builder.build();
    }

    private MethodSpec generateToStringMethodSpec(CommandViewMethod method) {
        StringBuilder statement = new StringBuilder("return ");

        if (method.getParameterSpecs().isEmpty()) {
            statement.append("\"")
                    .append(method.getName())
                    .append("\"");
        } else {
            boolean firstParams = true;
            statement.append("buildString(")
                    .append("\"")
                    .append(method.getName())
                    .append("\",");
            for (ParameterSpec parameter : method.getParameterSpecs()) {
                if (firstParams) {
                    firstParams = false;
                } else {
                    statement.append(",");
                }
                statement.append("\"")
                        .append(parameter.name)
                        .append("\",")
                        .append(parameter.name);
            }
            statement.append(")");
        }

        return MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement(statement.toString())
                .build();
    }

    @Override
    protected void finish(PipelineContext<JavaFile> nextContext) {
        mReflectorPackagesPublisher.finish();
        super.finish(nextContext);
    }
}
