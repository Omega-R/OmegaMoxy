package com.omegar.mvp

import kotlin.reflect.KClass

/**
 * Date: 07.12.2016
 * Time: 16:39
 *
 * @author Yuri Shmakov
 */
object MoxyReflector {
    private var sViewStateProviders: Map<String, Any> = HashMap()

    fun createViewState(presenterClass: KClass<*>): Any? = sViewStateProviders[presenterClass.qualifiedName]

}