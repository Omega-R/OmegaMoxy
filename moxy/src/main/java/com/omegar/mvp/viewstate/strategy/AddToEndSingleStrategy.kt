package com.omegar.mvp.viewstate.strategy

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand

/**
 * Command will be added to end of commands queue. If commands queue contains same type command, then existing command will be removed.
 *
 * Date: 17.12.2015
 * Time: 11:24
 *
 * @author Yuri Shmakov
 */
object AddToEndSingleStrategy : StateStrategy {
    override fun <View : MvpView> beforeApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        currentState.removeAll { it.javaClass === incomingCommand.javaClass }
        currentState.add(incomingCommand)
    }

    override fun <View : MvpView> afterApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        // pass
    }
}