package com.omegar.mvp.viewstate

import com.omegar.mvp.MvpProcessor
import com.omegar.mvp.MvpView
import java.lang.StringBuilder
import java.util.*

/**
 * Date: 15.12.2015
 * Time: 19:58
 *
 * @author Yuri Shmakov
 */
abstract class MvpViewState<View : MvpView> {
    /**
     * @return views, attached to this view state instance
     */
    val views: Set<View>
        get() = mutableViews

    protected val viewCommands = ViewCommands<View>()
    protected val mutableViews = Collections.newSetFromMap(WeakHashMap<View, Boolean>())
    protected val inRestoreState = Collections.newSetFromMap(WeakHashMap<View, Boolean>())
    protected val viewStates = WeakHashMap<View, Set<ViewCommand<View>>>()

    protected fun apply(command: ViewCommand<View>) {
        viewCommands.beforeApply(command)
        if (views.isNotEmpty()) {
            views.forEach(command::apply)
            viewCommands.afterApply(command)
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <C : ViewCommand<View>> findCommand(clz: Class<*>): C? {
        return viewCommands.currentState.firstOrNull { it.javaClass == clz } as? C?
    }

    protected inline fun <reified C : ViewCommand<View>> findCommand(): C? = findCommand(C::class.java)

    /**
     * Apply saved state to attached view
     *
     * @param view mvp view to restore state
     * @param currentState commands that was applied already
     */
    protected fun restoreState(view: View, currentState: Set<ViewCommand<View>>?) {
        if (!viewCommands.isEmpty) {
            viewCommands.reapply(view, currentState)
        }
    }

    /**
     * Attach view to view state and apply saves state
     *
     * @param view attachment
     */
    fun attachView(view: View?) {
        requireNotNull(view) { "Mvp view must be not null" }
        val isViewAdded = mutableViews.add(view)
        if (!isViewAdded) {
            return
        }
        inRestoreState.add(view)
        var currentState = viewStates[view]
        currentState = currentState ?: emptySet()
        restoreState(view, currentState)
        viewStates.remove(view)
        inRestoreState.remove(view)
    }

    /**
     *
     * Detach view from view state. After this moment view state save
     * commands via
     * [StateStrategy.beforeApply].
     *
     * @param view target mvp view to detach
     */
    fun detachView(view: View) {
        mutableViews.remove(view)
        inRestoreState.remove(view)
        val currentState = Collections.newSetFromMap(WeakHashMap<ViewCommand<View>, Boolean>())
        currentState.addAll(viewCommands.currentState)
        viewStates[view] = currentState
    }

    fun destroyView(view: View) {
        viewStates.remove(view)
    }

    /**
     * Check if view is in restore state or not
     *
     * @param view view for check
     * @return true if view state restore state to incoming view. false otherwise.
     */
    fun isInRestoreState(view: View): Boolean = inRestoreState.contains(view)

    override fun toString(): String {
        return "MvpViewState{" +
                "viewCommands=" + viewCommands +
                '}'
    }

    protected fun buildString(name: String, vararg args: Any?): String {
        return (0 until args.size / 2)
                .joinToString(prefix = "$name{", separator = ", ", postfix = "}") { i ->
                    val key = args[i * 2]
                    val value = args[i * 2 + 1]
                    val mark = "'".takeIf { value is String } ?: ""
                    "$key=$mark$value$mark"
                }
    }

}