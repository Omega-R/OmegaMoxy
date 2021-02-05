package com.omegar.mvp.compiler.viewstate;

import com.omegar.mvp.Moxy;
import com.omegar.mvp.MvpProcessor;
import com.omegar.mvp.MvpView;
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo;
import com.omegar.mvp.compiler.entity.ViewMethod;
import com.omegar.mvp.compiler.pipeline.JavaFileProcessor;
import com.omegar.mvp.compiler.MvpCompiler;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
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
            TypeSpec commandClass = generateCommandClass(method, new ArrayList<TypeVariableName>(viewInterfaceInfo.getTypeVariables()) {{
               add(0, variableName);
            }} );
            classBuilder.addType(commandClass);
            classBuilder.addMethod(generateMethod(viewInterfaceType, method, nameWithTypeVariables, commandClass));
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

    private TypeSpec generateCommandClass(ViewMethod method, List<TypeVariableName> variableNames) {
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

    private MethodSpec generateMethod(DeclaredType enclosingType, ViewMethod method,
                                      TypeName viewTypeName, TypeSpec commandClass) {
        // TODO: String commandFieldName = "$cmd";
        String commandFieldName = decapitalizeString(method.getCommandClassName());

        // Add salt if contains argument with same name
        Random random = new Random();
        while (method.getArgumentsString().contains(commandFieldName)) {
            commandFieldName += random.nextInt(10);
        }

        return MethodSpec.overriding(method.getElement(), enclosingType, mTypes)
                .addStatement("$1N $2L = new $1N($3L)", commandClass, commandFieldName, method.getArgumentsString())
                .addStatement("mViewCommands.beforeApply($L)", commandFieldName)
                .addCode("\n")
                .beginControlFlow("if (mViews == null || mViews.isEmpty())")
                .addStatement("return")
                .endControlFlow()
                .addCode("\n")
                .beginControlFlow("for ($L view$$ : mViews)", VIEW)
                .addStatement("view$$.$L($L)", method.getName(), method.getArgumentsString())
                .endControlFlow()
                .addCode("\n")
                .addStatement("mViewCommands.afterApply($L)", commandFieldName)
                .build();
    }

    private MethodSpec generateCommandConstructor(ViewMethod method) {
        List<ParameterSpec> parameters = method.getParameterSpecs();

        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addParameters(parameters)
                .addModifiers(Modifier.PRIVATE)
                .addStatement("super($S, $T.class)", method.getTag(), method.getStrategy());

        if (parameters.size() > 0) {
            builder.addCode("\n");
        }

        for (ParameterSpec parameter : parameters) {
            builder.addStatement("this.$1N = $1N", parameter);
        }

        return builder.build();
    }

    private MethodSpec generateToStringMethodSpec(ViewMethod method) {
        StringBuilder statement = new StringBuilder("return \"" + method.getName());

        boolean firstParams = true;
        for (ParameterSpec parameter : method.getParameterSpecs()) {
            if (firstParams) {
                firstParams = false;
                statement.append("{\" + \n\"");
            } else {
                statement.append(" + \n\", ");
            }

            statement.append(parameter.name)
                    .append("=\" + ")
                    .append(parameter.name)
                    .append(" ");
        }
        if (!firstParams) {
            statement.append("+\n'}'");
        } else {
            statement.append("\"");
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
