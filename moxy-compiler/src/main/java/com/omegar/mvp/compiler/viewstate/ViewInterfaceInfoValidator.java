package com.omegar.mvp.compiler.viewstate;

import com.omegar.mvp.Moxy;
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo;
import com.omegar.mvp.compiler.pipeline.Validator;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by Anton Knyazev on 14.12.2020.
 */
public class ViewInterfaceInfoValidator extends Validator<ViewInterfaceInfo> {

    private final Elements mElements;
    private final String mCurrentReflectorPackage;

    public ViewInterfaceInfoValidator(Elements elements, String currentReflectorPackage) {
        mElements = elements;
        mCurrentReflectorPackage = currentReflectorPackage;
    }

    @Override
    public boolean validate(ViewInterfaceInfo input) {
        String viewStateFullName = input.getViewStateFullName(mElements);
        TypeElement viewStateElement = mElements.getTypeElement(viewStateFullName);
        if (viewStateElement == null) {
            return true;
        }
        Moxy[] moxies = viewStateElement.getAnnotationsByType(Moxy.class);
        for (Moxy moxy : moxies) {
            if (mCurrentReflectorPackage.equals(moxy.reflectorPackage())) {
                return true;
            }
        }
        return false;
    }

}
