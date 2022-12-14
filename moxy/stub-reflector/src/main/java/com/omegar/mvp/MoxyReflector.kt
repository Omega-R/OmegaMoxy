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
    private var sPresenterBinders: Map<String, List<Any>> = HashMap()

    fun getViewState(presenterClass: KClass<*>): Any? = sViewStateProviders[presenterClass.qualifiedName]

    fun getPresenterBinders(delegated: KClass<*>): List<Any>? = sPresenterBinders[delegated.qualifiedName]
}