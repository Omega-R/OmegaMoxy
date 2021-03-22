package com.omegar.mvp.compiler.entity;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class ReturnViewMethod extends ViewMethod {

    private TypeMirror mReturnType;
    private final ExecutableElement mSetterElement;

    public ReturnViewMethod(Types types,
                            DeclaredType targetInterfaceElement,
                            ExecutableElement getterElement,
                            ExecutableElement setterElement) {
        super(types, targetInterfaceElement, getterElement);
        mReturnType = getterElement.getReturnType();
        mSetterElement = setterElement;
    }

    public TypeMirror getReturnType() {
        return mReturnType;
    }

    public ExecutableElement getSetterElement() {
        return mSetterElement;
    }
}
