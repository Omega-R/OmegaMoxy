package com.omegar.mvp.compiler.entity;

import com.omegar.mvp.MvpProcessor;
import com.omegar.mvp.compiler.Util;
import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.ClassNames;
import com.squareup.kotlinpoet.ParameterizedTypeName;
import com.squareup.kotlinpoet.TypeName;
import com.squareup.kotlinpoet.TypeVariableName;
import com.squareup.kotlinpoet.TypeVariableNames;

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
    private final List<TypeVariableName> mParentTypeVariables;

    private final List<ViewCommandInfo> mCommands;

    public ViewInterfaceInfo(@Nullable TypeElement superInterfaceType,
                             TypeElement element,
                             List<ViewCommandInfo> commands,
                             List<TypeVariableName> typeVariables,
                             List<TypeVariableName> parentTypeVariables
    ) {
        mSuperInterfaceType = superInterfaceType;
        mElement = element;
        mName = ClassNames.get(element);
        mCommands = commands;
        mTypeVariables = typeVariables;
        mParentTypeVariables = parentTypeVariables;
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

    public List<TypeVariableName> getParentTypeVariables() {
        return mParentTypeVariables;
    }

    public List<ViewCommandInfo> getCommands() {
        return mCommands;
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

    public static String getViewStateFullName(String fullViewName) {
        return fullViewName + MvpProcessor.VIEW_STATE_SUFFIX;
    }

    public static String getViewFullName(Elements elements, TypeElement viewTypeElement) {
        return Util.getFullClassName(elements, viewTypeElement);
    }

    public static String getViewStateSimpleName(Elements elements, TypeElement viewTypeElement) {
        return Util.getSimpleClassName(elements, viewTypeElement) + MvpProcessor.VIEW_STATE_SUFFIX;
    }


}
