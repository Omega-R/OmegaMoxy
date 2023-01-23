package com.omegar.mvp.compiler.entity

import com.omegar.mvp.compiler.extenions.orValueOf
import com.omegar.mvp.presenter.PresenterType
import javax.lang.model.type.DeclaredType

class PresenterProviderMethod(
    val clazz: DeclaredType,
    val name: String,
    val presenterType: PresenterType?,
    val tag: String?,
    val presenterId: String?
) {

    constructor(
        clazz: DeclaredType,
        name: String,
        type: String?,
        tag: String?,
        presenterId: String?
    ) : this(
        clazz,
        name,
        PresenterType.LOCAL.orValueOf(type),
        tag,
        presenterId
    )

}