package com.omegar.mvp.compiler

import com.omegar.mvp.compiler.entities.View
import com.squareup.kotlinpoet.ClassName
import java.util.Locale


/**
 * Created by Anton Knyazev on 27.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
object NamingRules {

    val View.viewStateName
        get() = "$name\$\$State"

    val View.viewStateClassName
        get() = toClassName(viewStateName)

    val View.Method.commandName
        get() = "${name.replaceFirstChar { it.titlecase(Locale.ROOT) }}Command"

    const val moxyReflectorName = "MoxyReflector"

    const val moxyReflectorPackageName = "com.omegar.mvp"

    private fun View.toClassName(name: String) = ClassName(className.packageName, name)

}