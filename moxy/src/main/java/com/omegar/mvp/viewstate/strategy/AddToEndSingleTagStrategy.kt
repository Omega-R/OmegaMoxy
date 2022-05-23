package com.omegar.mvp.viewstate.strategy

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand

/**
 * Command will be added to the end of the commands queue. If the commands queue contains the same tag, then
 * an existing command will be removed.
 */
object AddToEndSingleTagStrategy : StateStrategy {
    override fun <View : MvpView?> beforeApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        currentState.removeAll { it.tag == incomingCommand.tag }
        currentState.add(incomingCommand)
    }

    override fun <View : MvpView?> afterApply(currentState: List<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        //Just do nothing
    }
}