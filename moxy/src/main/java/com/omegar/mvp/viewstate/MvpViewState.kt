package com.omegar.mvp.viewstate

import android.os.Bundle
import com.omegar.mvp.MvpView
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
    protected val mutableViews = mutableWeakSet<View>()
    private val inRestoreState = mutableWeakSet<View>()
    private val viewStates = WeakHashMap<View, Set<ViewCommand<View>>>()

    fun loadState(inBundle: Bundle) = viewCommands.load(inBundle)

    fun saveState(outBundle: Bundle) = viewCommands.save(outBundle)

    protected fun apply(command: ViewCommand<View>) {
        viewCommands.beforeApply(command)
        if (views.isNotEmpty()) {
            views.forEach(command::apply)
            viewCommands.afterApply(command)
        }
    }

    protected inline fun <reified C : ViewCommand<View>> findCommand(): C? = viewCommands.findCommand(C::class.java)

    /**
     * Apply saved state to attached view
     *
     * @param view mvp view to restore state
     * @param currentState commands that was applied already
     */
    private fun restoreState(view: View, currentState: Set<ViewCommand<View>>) = viewCommands.reapply(view, currentState)

    /**
     * Attach view to view state and apply saves state
     *
     * @param view attachment
     */
    fun attachView(view: View) {
        val isViewAdded = mutableViews.add(view)
        if (!isViewAdded) {
            return
        }
        inRestoreState += view
        restoreState(view, currentState = viewStates[view].orEmpty())
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
        mutableViews -= view
        inRestoreState -= view
        viewStates[view] = mutableWeakSet(viewCommands.commands)
    }

    fun destroyView(view: View) {
        viewStates -= view
    }

    /**
     * Check if view is in restore state or not
     *
     * @param view view for check
     * @return true if view state restore state to incoming view. false otherwise.
     */
    fun isInRestoreState(view: View) = inRestoreState.contains(view)

    override fun toString(): String {
        return "MvpViewState{" +
                "viewCommands=" + viewCommands +
                '}'
    }

}

private fun <E> mutableWeakSet(): MutableSet<E> = Collections.newSetFromMap(WeakHashMap())

private fun <E> mutableWeakSet(collection: Collection<E>): MutableSet<E> {
    return Collections.newSetFromMap<E>(WeakHashMap(collection.size)).apply {
        addAll(collection)
    }
}