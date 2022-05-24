package com.omegar.mvp.viewstate

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategy

/**
 * Date: 16-Dec-15
 * Time: 16:59
 *
 * @author Alexander Blinov
 */
abstract class ViewCommand<View : MvpView> protected constructor(
        val tag: String = "",
        val stateStrategy: StateStrategy = AddToEndSingleStrategy
) {

    abstract fun apply(view: View)

    fun beforeApply(commands: MutableList<ViewCommand<View>>) {
        stateStrategy.beforeApply(commands, this)
    }

    fun afterApply(commands: MutableList<ViewCommand<View>>) {
        stateStrategy.afterApply(commands, this)
    }

    override fun toString(): String {
        return javaClass.simpleName
    }
}