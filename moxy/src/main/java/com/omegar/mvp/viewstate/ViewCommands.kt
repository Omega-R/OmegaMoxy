package com.omegar.mvp.viewstate

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.omegar.mvp.MvpView
import com.omegar.mvp.SavedState
import java.util.ArrayList

/**
 * Date: 17.12.2015
 * Time: 11:09
 *
 * @author Yuri Shmakov
 */
class ViewCommands<View : MvpView> {

    private companion object {
        private const val KEY_STATE = "state"
    }

    val currentState: MutableList<ViewCommand<View>> = ArrayList()

    @Suppress("UNCHECKED_CAST")
    fun load(inBundle: Bundle) {
        val savedState = inBundle.getParcelable<SavedState>(KEY_STATE)
        currentState.addAll(savedState.list as List<ViewCommand<View>>)
    }

    fun save(outBundle: Bundle) {
        val savedState = SavedState(currentState)
        outBundle.putParcelable(KEY_STATE, savedState)
    }

    fun beforeApply(viewCommand: ViewCommand<View>) {
        viewCommand.stateStrategy.beforeApply(currentState, viewCommand)
    }

    fun afterApply(viewCommand: ViewCommand<View>) {
        viewCommand.stateStrategy.afterApply(currentState, viewCommand)
    }

    fun reapply(view: View, currentState: Set<ViewCommand<View>>) {
        if (this.currentState.isNotEmpty()) {
            val commands = ArrayList(this.currentState)
            for (command in commands) {
                if (currentState.contains(command)) {
                    continue
                }
                command.apply(view)
                afterApply(command)
            }
        }
    }

    override fun toString(): String {
        return "ViewCommands{" +
                "state=" + currentState +
                '}'
    }
}