package com.omegar.mvp.compiler.extensions

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec


/**
 * Created by Anton Knyazev on 11.05.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */

fun TypeSpec.toFileSpec(packageName: String): FileSpec {
    return toFileSpecBuilder(packageName)
        .build()
}

fun TypeSpec.toFileSpecBuilder(packageName: String): FileSpec.Builder {
    return FileSpec.builder(packageName, name!!)
        .addType(this)
        .indent("\t")
}
