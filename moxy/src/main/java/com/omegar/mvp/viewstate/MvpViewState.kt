package com.omegar.mvp.viewstate

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.strategy.StateStrategy
import java.util.Collections
import java.util.WeakHashMap

/**
 * Date: 15.12.2015
 * Time: 19:58
 *
 * @author Yuri Shmakov
 */
abstract class MvpViewState<View : MvpView> {

    protected val views = mutableWeakSet<View>()
    protected val commands = ViewCommands<View>()
    private val restoreViews = mutableWeakSet<View>()
    private val restoreMap = WeakHashMap<View, Set<ViewCommand<View>>>()

    /**
     * @return views, attached to this view state instance
     */
    val attachedViews: Set<View>
        get() = views

    protected fun apply(command: ViewCommand<View>) {
        commands.beforeApply(command)
        if (views.isNotEmpty()) {
            views.forEach(command::apply)
            commands.afterApply(command)
        }
    }

    protected inline fun <reified C : ViewCommand<View>> findCommand(): C? = commands.findCommand(C::class.java)

    /**
     * Apply saved state to attached view
     *
     * */
    private fun View.restoreState() {
        restoreViews += this
        commands.reapply(this, currentState = restoreMap[this].orEmpty())
        restoreMap -= this
        restoreViews -= this
    }

    /**
     * Attach view to view state and apply saves state
     *
     * @param view attachment
     */
    fun attachView(view: View) {
        val isViewAdded = views.add(view)
        if (isViewAdded) {
            view.restoreState()
        }
    }

    /**
     * Detach view from view state. After this moment view state save
     * commands via
     * [StateStrategy.beforeApply].
     *
     * @param view target mvp view to detach
     */
    fun detachView(view: View) {
        views -= view
        restoreViews -= view
        restoreMap[view] = mutableWeakSet(commands.list)
    }

    fun destroyView(view: View) {
        restoreMap -= view
    }

    /**
     * Check if view is in restore state or not
     *
     * @param view view for check
     * @return true if view state restore state to incoming view. false otherwise.
     */
    fun isInRestoreState(view: View) = restoreViews.contains(view)

    override fun toString(): String {
        return "MvpViewState{" +
                "viewCommands=" + commands +
                '}'
    }

}

private fun <E> mutableWeakSet(): MutableSet<E> = Collections.newSetFromMap(WeakHashMap())

private fun <E> mutableWeakSet(collection: Collection<E>): MutableSet<E> {
    return Collections.newSetFromMap<E>(WeakHashMap(collection.size)).apply {
        addAll(collection)
    }
}