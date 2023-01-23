package com.omegar.mvp.compiler.viewstate

import com.omegar.mvp.Moxy
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo
import com.omegar.mvp.compiler.pipeline.Validator
import javax.lang.model.util.Elements

/**
 * Created by Anton Knyazev on 14.12.2020.
 */
class ViewInterfaceInfoValidator(
    private val elements: Elements,
    private val currentReflectorPackage: String
) : Validator<ViewInterfaceInfo>() {

    override fun validate(input: ViewInterfaceInfo): Boolean {
        val viewStateFullName = input.getViewStateFullName(elements)
        val viewStateElement = elements.getTypeElement(viewStateFullName) ?: return true
        return viewStateElement.getAnnotationsByType(Moxy::class.java)
            .any { currentReflectorPackage == it.reflectorPackage }
    }

}