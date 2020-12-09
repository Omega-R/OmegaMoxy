package com.omegar.mvp.compiler.entity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;

/**
 * Date: 27-Jul-2017
 * Time: 13:04
 *
 * @author Evgeny Kursakov
 */
public class ViewInterfaceInfo implements TypeElementHolder {
    @Nullable
    private final ViewInterfaceInfo superInterfaceInfo;
    private final TypeElement element;
    private final ClassName name;
    private final List<TypeVariableName> typeVariables;
    private final List<ViewMethod> methods;

    public ViewInterfaceInfo(TypeElement element, List<ViewMethod> methods) {
        this(null, element, methods);
    }

    public ViewInterfaceInfo(@Nullable ViewInterfaceInfo superInterfaceInfo, TypeElement element, List<ViewMethod> methods) {
        this.superInterfaceInfo = superInterfaceInfo;
        this.element = element;
        this.name = ClassName.get(element);
        this.methods = methods;

        this.typeVariables = element.getTypeParameters().stream()
                .map(TypeVariableName::get)
                .collect(Collectors.toList());

    }

    @Nullable
    public ViewInterfaceInfo getSuperInterfaceInfo() {
        return superInterfaceInfo;
    }

    public TypeElement getTypeElement() {
        return element;
    }

    public ClassName getName() {
        return name;
    }

    public TypeName getNameWithTypeVariables() {
        if (typeVariables.isEmpty()) {
            return name;
        } else {
            TypeVariableName[] names = new TypeVariableName[typeVariables.size()];
            typeVariables.toArray(names);

            return ParameterizedTypeName.get(name, names);
        }
    }

    public List<TypeVariableName> getTypeVariables() {
        return typeVariables;
    }

    public List<ViewMethod> getMethods() {
        return methods;
    }

    public TypeElement getSuperClassElement() {
        return superInterfaceInfo == null ? null : superInterfaceInfo.getTypeElement();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewInterfaceInfo that = (ViewInterfaceInfo) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ViewInterfaceInfo{" +
                "superInterfaceInfo=" + superInterfaceInfo +
                ", element=" + element +
                '}';
    }
}
