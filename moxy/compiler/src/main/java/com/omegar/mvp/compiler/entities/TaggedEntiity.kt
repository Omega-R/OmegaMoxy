package com.omegar.mvp.compiler.entities

import kotlin.reflect.KClass


/**
 * Created by Anton Knyazev on 27.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
open class Tagged {

    private val taggedMap = mutableMapOf<KClass<*>, Any?>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getTag(cls: KClass<T>): T? = taggedMap[cls] as? T?

    fun <T : Any> putTag(cls: KClass<T>, value: T?) {
        taggedMap[cls] = value
    }

}