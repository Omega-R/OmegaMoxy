package com.omegar.mvp.viewstate.strategy

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand

/**
 * Command will be added to end of commands queue.
 *
 *
 * Date: 17.12.2015
 * Time: 11:29
 *
 * @author Yuri Shmakov
 */
object AddToEndStrategy : StateStrategy {
    override fun <View : MvpView> beforeApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        currentState.add(incomingCommand)
    }

    override fun <View : MvpView> afterApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        // pass
    }
}