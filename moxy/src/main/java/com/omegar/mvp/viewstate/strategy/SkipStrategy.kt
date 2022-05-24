package com.omegar.mvp.viewstate.strategy

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand

/**
 * Command will not be put in commands queue
 *
 * Date: 21-Dec-15
 * Time: 17:43
 *
 * @author Alexander Blinov
 */
object SkipStrategy : StateStrategy {
    override fun <View : MvpView> beforeApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        //do nothing to skip
    }

    override fun <View : MvpView> afterApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        // pass
    }
}