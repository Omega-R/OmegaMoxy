package com.omegar.mvp.viewstate

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.omegar.mvp.MvpView
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
        val loadedState = inBundle.getParcelableArrayList<Parcelable>(KEY_STATE)
        currentState.addAll(loadedState as List<ViewCommand<View>>)
        Log.e("MoxyDebug", "Loaded state = $loadedState")
    }

    fun save(outBundle: Bundle) {
        val savedState = currentState.filterIsInstanceTo(ArrayList(currentState.size), Parcelable::class.java)
        outBundle.putParcelableArrayList(KEY_STATE, savedState)
    }

    fun beforeApply(viewCommand: ViewCommand<View>) {
        val stateStrategy = viewCommand.stateStrategy
        stateStrategy.beforeApply(currentState, viewCommand)
    }

    fun afterApply(viewCommand: ViewCommand<View>) {
        val stateStrategy = viewCommand.stateStrategy
        stateStrategy.afterApply(currentState, viewCommand)
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