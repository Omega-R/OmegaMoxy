package com.omegar.mvp.compiler.entity

import com.omegar.mvp.viewstate.SerializeType
import javax.lang.model.element.TypeElement

data class ViewCommandInfo(
        val method: ViewMethod,
        val uniqueSuffix: String,
        val strategy: TypeElement,
        val tag: String,
        val singleInstance: Boolean,
        val serializeType: SerializeType?
) {

    val name: String = method.name.replaceFirstChar { it.titlecase() } + uniqueSuffix + "Command"

}