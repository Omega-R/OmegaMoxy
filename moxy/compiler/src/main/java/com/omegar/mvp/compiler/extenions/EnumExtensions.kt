package com.omegar.mvp.compiler.extenions

inline fun <reified E : Enum<E>> E.orValueOf(name: String?): E = name?.let { enumValueOf<E>(name) } ?: this