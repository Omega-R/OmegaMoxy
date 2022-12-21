package com.omegar.mvp.compiler.entity

import com.omegar.mvp.MvpProcessor
import com.omegar.mvp.compiler.MoxyConst
import com.omegar.mvp.compiler.Util
import com.omegar.mvp.presenter.PresenterType
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.type.TypeMirror

class TargetPresenterField(
    val clazz: TypeMirror,
    val name: String,
    presenterType: String?,
) {
    val isParametrized: Boolean
    val typeName: TypeName
    val presenterType: PresenterType = if (presenterType == null) PresenterType.LOCAL else PresenterType.valueOf(presenterType)
    var presenterProviderMethodName: String? = null
    var presenterTagProviderMethodName: String? = null

    init {
        val rawTypeName = clazz.asTypeName()
        isParametrized = rawTypeName is ParameterizedTypeName
        typeName = if (isParametrized) (rawTypeName as ParameterizedTypeName).rawType else rawTypeName
    }

    val generatedClassName: String
        get() = Util.capitalizeString(name) + MoxyConst.PRESENTER_BINDER_INNER_SUFFIX
}