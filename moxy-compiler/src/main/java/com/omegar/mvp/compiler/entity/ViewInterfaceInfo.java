package com.omegar.mvp.compiler.entity;

import com.omegar.mvp.MvpProcessor;
import com.omegar.mvp.compiler.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Date: 27-Jul-2017
 * Time: 13:04
 *
 * @author Evgeny Kursakov
 */
@SuppressWarnings("NewApi")
public class ViewInterfaceInfo implements TypeElementHolder {
    @Nullable
    private final TypeElement mSuperInterfaceType;
    private final TypeElement mElement;
    private final ClassName mName;
    private final List<TypeVariableName> mTypeVariables;
    private final List<ViewMethod> mMethods;

    public ViewInterfaceInfo(TypeElement element, List<ViewMethod> methods) {
        this(null, element, methods);
    }

    public ViewInterfaceInfo(@Nullable TypeElement superInterfaceType, TypeElement element, List<ViewMethod> methods) {
        mSuperInterfaceType = superInterfaceType;
        mElement = element;
        mName = ClassName.get(element);
        mMethods = methods;

        mTypeVariables = element.getTypeParameters().stream()
                .map(TypeVariableName::get)
                .collect(Collectors.toList());

    }

    @Nullable
    public TypeElement getSuperInterfaceType() {
        return mSuperInterfaceType;
    }

    public TypeElement getTypeElement() {
        return mElement;
    }

    public ClassName getName() {
        return mName;
    }

    public TypeName getNameWithTypeVariables() {
        if (mTypeVariables.isEmpty()) {
            return mName;
        } else {
            TypeVariableName[] names = new TypeVariableName[mTypeVariables.size()];
            mTypeVariables.toArray(names);

            return ParameterizedTypeName.get(mName, names);
        }
    }

    public List<TypeVariableName> getTypeVariables() {
        return mTypeVariables;
    }

    public List<ViewMethod> getMethods() {
        return mMethods;
    }

    public String getViewStateFullName(Elements elements) {
        return getViewStateFullName(elements, mElement);
    }

    public String getViewStateSimpleName(Elements elements) {
        return getViewStateSimpleName(elements, mElement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewInterfaceInfo that = (ViewInterfaceInfo) o;

        return mName != null ? mName.equals(that.mName) : that.mName == null;
    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ViewInterfaceInfo{" +
                "superInterfaceInfo=" + mSuperInterfaceType +
                ", element=" + mElement +
                '}';
    }

    public static String getViewStateFullName(Elements elements, TypeElement viewTypeElement) {
        return Util.getFullClassName(elements, viewTypeElement) + MvpProcessor.VIEW_STATE_SUFFIX;
    }

    public static String getViewStateSimpleName(Elements elements, TypeElement viewTypeElement) {
        return Util.getSimpleClassName(elements, viewTypeElement) + MvpProcessor.VIEW_STATE_SUFFIX;
    }


}
