package com.omegar.mvp.compiler.entity;

import com.omegar.mvp.compiler.Util;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

@SuppressWarnings("NewApi")
public class ViewMethod {

    private final ExecutableElement mMethodElement;
    private final String mName;
    private final List<ParameterSpec> mParameterSpecs;
    private final List<TypeName> mExceptions;
    private final List<TypeVariableName> mTypeVariables;
    private final String mArgumentsString;


    public ViewMethod(Types types, DeclaredType targetInterfaceElement, ExecutableElement methodElement) {
        mMethodElement = methodElement;
        mName = getMethodName(methodElement);

        mParameterSpecs = new ArrayList<>();

        ExecutableType executableType = (ExecutableType) types.asMemberOf(targetInterfaceElement, methodElement);
        List<? extends VariableElement> parameters = methodElement.getParameters();
        List<? extends TypeMirror> resolvedParameterTypes = executableType.getParameterTypes();

        for (int i = 0; i < parameters.size(); i++) {
            VariableElement element = parameters.get(i);
            TypeName type = TypeName.get(resolvedParameterTypes.get(i));
            String name = element.getSimpleName().toString();

            mParameterSpecs.add(ParameterSpec.builder(type, name)
                    .addModifiers(element.getModifiers())
                    .build()
            );
        }

        mExceptions = methodElement.getThrownTypes().stream()
                .map(TypeName::get)
                .collect(Collectors.toList());

        mTypeVariables = methodElement.getTypeParameters()
                .stream()
                .map(TypeVariableName::get)
                .collect(Collectors.toList());

        mArgumentsString = mParameterSpecs.stream()
                .map(parameterSpec -> parameterSpec.name)
                .collect(Collectors.joining(", "));
    }

    public ExecutableElement getElement() {
        return mMethodElement;
    }

    public String getName() {
        return mName;
    }

    public List<ParameterSpec> getParameterSpecs() {
        return mParameterSpecs;
    }

    public List<TypeName> getExceptions() {
        return mExceptions;
    }

    public List<TypeVariableName> getTypeVariables() {
        return mTypeVariables;
    }

    public String getArgumentsString() {
        return mArgumentsString;
    }

    public String getEnclosedClassName() {
        TypeElement typeElement = (TypeElement) mMethodElement.getEnclosingElement();
        return typeElement.getQualifiedName().toString();
    }

    private List<ParameterSpec> formatParameters(Types types, DeclaredType enclosingType, ExecutableElement element,
                                                 List<ParameterSpec> parameterSpecs) {
        List<ParameterSpec> list = new ArrayList<>();

        ExecutableType executableType = (ExecutableType) types.asMemberOf(enclosingType, element);
        List<? extends TypeMirror> resolvedParameterTypes = executableType.getParameterTypes();

        for (int i = 0; i < parameterSpecs.size(); i++) {
            ParameterSpec parameter = parameterSpecs.get(i);
            TypeName type = TypeName.get(resolvedParameterTypes.get(i));
            list.add(ParameterSpec.builder(type, parameter.name).build());
        }

        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandViewMethod that = (CommandViewMethod) o;

        if (mMethodElement != null ? !mMethodElement.equals(that.getElement()) : that.getElement() != null) return false;

        return mArgumentsString != null ? mArgumentsString.equals(that.getArgumentsString()) : that.getArgumentsString() == null;
    }

    @Override
    public int hashCode() {
        int result = mMethodElement != null ? mMethodElement.hashCode() : 0;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mArgumentsString != null ? mArgumentsString.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "ViewMethod{" +
                "name='" + mName + '\'' +
                '}';
    }

    public static String getMethodName(ExecutableElement methodElement) {
        return methodElement.getSimpleName().toString();
    }

}
