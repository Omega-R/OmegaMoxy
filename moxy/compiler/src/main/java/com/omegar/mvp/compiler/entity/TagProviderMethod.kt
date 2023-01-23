package com.omegar.mvp.compiler.entity

import com.omegar.mvp.compiler.extenions.orValueOf
import com.omegar.mvp.presenter.PresenterType
import javax.lang.model.type.TypeMirror

class TagProviderMethod(
    val presenterClass: TypeMirror?,
    val methodName: String,
    val type: PresenterType,
    val presenterId: String?
) {

    constructor(
        presenterClass: TypeMirror?,
        methodName: String,
        type: String?,
        presenterId: String?
    ) : this(
        presenterClass,
        methodName,
        PresenterType.LOCAL.orValueOf(type),
        presenterId
    )

}