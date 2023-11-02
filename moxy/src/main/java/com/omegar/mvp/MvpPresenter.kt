package com.omegar.mvp

import com.omegar.mvp.presenter.PresenterType
import com.omegar.mvp.viewstate.MvpViewState

/**
 * Date: 15.12.2015
 * Time: 19:31
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 * @author Konstantin Tckhovrebov
 */
@InjectViewState
abstract class MvpPresenter<View : MvpView> {

    /**
     * @return view state, casted to view interface for simplify
     */
    @Suppress("UNCHECKED_CAST")
    protected val viewState: View
        get() = mvpViewState as View

    private var firstLaunch = true
    internal lateinit var presenterTag: String
        @JvmName("getTag") get
        @JvmName("setTag") set
    internal lateinit var presenterType: PresenterType
        @JvmName("getPresenterType") get
        @JvmName("setPresenterType") set


    private var mvpViewState: MvpViewState<View> = MvpProcessor.createViewState(this::class)

    private val savedFields: MutableList<SavedField<*>> = mutableListOf()

    /**
     * @return views attached to view state, or attached to presenter(if view state not exists)
     */
    val attachedViews: Set<View>
        get() = mvpViewState.attachedViews

    internal fun attachView(mvpView: MvpView) {
        val view = mvpView as View
        attachView(view, firstLaunch)
        firstLaunch = false
    }

    /**
     *
     * Attach view to view state or to presenter(if view state not exists).
     *
     * If you use [MvpDelegate], you should not call this method directly.
     * It will be called on [MvpDelegate.onAttach], if view does not attached.
     *
     * @param view to attachment
     * @param isFirstAttach is first presenter init and view binding
     */
    protected open fun attachView(view: View, isFirstAttach: Boolean) {
        attachView(view)
        if (isFirstAttach) {
            onFirstViewAttach()
        }
    }

    /**
     *
     * Attach view to view state or to presenter(if view state not exists).
     *
     * If you use [MvpDelegate], you should not call this method directly.
     * It will be called on [MvpDelegate.onAttach], if view does not attached.
     *
     * @param view to attachment
     */
    protected open fun attachView(view: View) {
        mvpViewState.attachView(view)
    }

    /**
     *
     * Callback after first presenter init and view binding. If this
     * presenter instance will have to attach some view in future, this method
     * will not be called.
     *
     * There you can to interact with [.mViewState].
     */
    protected open fun onFirstViewAttach() {
        // nothing
    }

    internal fun detachView(view: MvpView) {
        detachView(view as View)
    }

    internal fun destroyView(view: MvpView) {
        destroyView(view as View)
    }

    /**
     *
     * Detach view from view state or from presenter(if view state not exists).
     *
     * If you use [MvpDelegate], you should not call this method directly.
     * It will be called on [MvpDelegate.onDetach].
     *
     * @param view view to detach
     */
    protected open fun detachView(view: View) {
        mvpViewState.detachView(view)
    }

    protected open fun destroyView(view: View) {
        mvpViewState.destroyView(view)
    }

    /**
     * Set view state to presenter
     *
     * @param viewState that implements type, setted as View generic param
     */
    protected fun setViewState(viewState: MvpViewState<View>) {
        mvpViewState = viewState
    }

    /**
     * Check if view is in restore state or not
     *
     * @param view view for check
     * @return true if view state restore state to incoming view. false otherwise.
     */
    protected fun isInRestoreState(view: View): Boolean = mvpViewState.isInRestoreState(view)

    /**
     *
     * Called before reference on this presenter will be cleared and instance of presenter
     * will be never used.
     */
    open fun onDestroy() {
        // nothing
    }

}