package com.omegar.mvp.viewstate

import android.os.Bundle
import com.omegar.mvp.MvpView
import com.omegar.mvp.SavedState
import java.util.ArrayList

/**
 * Date: 17.12.2015
 * Time: 11:09
 *
 * @author Yuri Shmakov
 */
@JvmInline
value class ViewCommands<View : MvpView>(val list: MutableList<ViewCommand<View>> = ArrayList()) {

    private companion object {
        private const val KEY_STATE = "state"
    }

    @Suppress("UNCHECKED_CAST")
    fun load(inBundle: Bundle) {
        val savedState = inBundle.getParcelable<SavedState>(KEY_STATE) ?: return
        list.addAll(savedState.list as List<ViewCommand<View>>)
    }

    fun save(outBundle: Bundle) {
        val savedState = SavedState(list)
        outBundle.putParcelable(KEY_STATE, savedState)
    }

    fun beforeApply(command: ViewCommand<View>) = command.beforeApply(list)

    fun afterApply(command: ViewCommand<View>) = command.afterApply(list)

    @Suppress("UNCHECKED_CAST")
    fun <C : ViewCommand<View>> findCommand(clz: Class<*>): C? = list.lastOrNull { it.javaClass === clz } as? C?

    fun reapply(view: View, currentState: Set<ViewCommand<View>>) {
        list.toList()
                .asSequence()
                .filter { it !in currentState }
                .forEach { command ->
                    command.apply(view)
                    afterApply(command)
                }
    }

    override fun toString(): String {
        return "ViewCommands{" +
                "state=" + list +
                '}'
    }

}