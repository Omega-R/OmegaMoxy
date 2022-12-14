package com.omegar.mvp.compiler.entity

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.TypeElement

data class TargetClassInfo(override val typeElement: TypeElement, val fields: List<TargetPresenterField>) : TypeElementHolder {
    val name: ClassName = typeElement.asClassName()
}