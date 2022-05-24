package com.omegar.mvp.viewstate.strategy

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand
import java.io.Serializable

/**
 * Cautions:
 *
 *  * Don't rearrange current state
 *  * Don't insert commands inside existing current state - only put to end of it
 *  * Be careful if remove commands by another type. If you make it, be sure that inside your view method you fully override view changes
 *
 *
 * Date: 17.12.2015
 * Time: 11:21
 *
 * @author Yuri Shmakov
 */
interface StateStrategy : Serializable {
    /**
     * Called immediately after
     * [MvpViewState] receive some
     * command. Will not be called before re-apply to some other
     * [MvpView]
     *
     * @param currentState    current state of
     * [MvpViewState]. Each [ViewCommand]
     * contains self parameters.
     * @param incomingCommand command for apply to [MvpView] This
     * [ViewCommand] contains params of this command.
     * @param <View>          type of incoming view
    </View> */
    fun <View : MvpView> beforeApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>)

    /**
     * Called immediately after command applied to [MvpView]. Also called
     * after re-apply to other views.
     *
     * @param currentState    current state of
     * [MvpViewState]. Each [ViewCommand]
     * contains self parameters.
     * @param incomingCommand applied command to [MvpView] This
     * [ViewCommand] contains params of this command.
     * @param <View>          type of incoming view
    </View> */
    fun <View : MvpView> afterApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>)
}