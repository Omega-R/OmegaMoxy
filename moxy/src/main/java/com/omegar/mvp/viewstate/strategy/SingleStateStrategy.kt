package com.omegar.mvp.viewstate.strategy

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand

/**
 * This strategy will clear current commands queue and then incoming command will be put in.
 *
 * Caution! Be sure that you fully set view to initial state inside this command.
 *
 * Date: 19-Dec-15
 * Time: 14:34
 *
 * @author Alexander Blinov
 */
object SingleStateStrategy : StateStrategy {
    override fun <View : MvpView?> beforeApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        currentState.clear()
        currentState.add(incomingCommand)
    }

    override fun <View : MvpView?> afterApply(currentState: List<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        // pass
    }
}