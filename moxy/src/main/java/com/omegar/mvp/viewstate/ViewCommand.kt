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
        private val stateStrategy: StateStrategy = AddToEndSingleStrategy
) {

    fun beforeApply(commands: MutableList<ViewCommand<View>>) = stateStrategy.beforeApply(commands, this)

    abstract fun apply(mvpView: View)

    fun afterApply(commands: MutableList<ViewCommand<View>>) = stateStrategy.afterApply(commands, this)

    override fun toString(): String = javaClass.simpleName

    protected fun buildString(name: String, vararg args: Any?): String {
        return (0 until args.size / 2)
                .joinToString(prefix = "$name{", separator = ", ", postfix = "}") { i ->
                    val key = args[i * 2]
                    val value = args[i * 2 + 1]
                    val mark = "'".takeIf { value is String } ?: ""
                    "$key=$mark$value$mark"
                }
    }
}