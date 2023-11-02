package com.omegar.mvp.viewstate.strategy

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand

/**
 * Command will be saved in commands queue. And this command will be removed after first execution.
 *
 * Date: 24.11.2016
 * Time: 11:48
 *
 * @author Yuri Shmakov
 */
object OneExecutionStateStrategy : StateStrategy {
    override fun <View : MvpView> beforeApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        currentState.add(incomingCommand)
    }

    override fun <View : MvpView> afterApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        currentState.remove(incomingCommand)
    }
}