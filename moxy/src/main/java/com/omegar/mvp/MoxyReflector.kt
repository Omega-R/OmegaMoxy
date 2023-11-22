package com.omegar.mvp

import com.omegar.mvp.viewstate.MvpViewState
import kotlin.reflect.KClass


object MoxyReflector {
    private val sViewStateProviders: MutableMap<String?, () -> MvpViewState<*>> = mutableMapOf()

    operator fun set(presenter: KClass<out MvpPresenter<*>>, factory: () -> MvpViewState<*>) {
        sViewStateProviders[presenter.qualifiedName] = factory
    }

    fun createViewState(presenterClass: KClass<*>): Any? = sViewStateProviders[presenterClass.qualifiedName]?.invoke()

}