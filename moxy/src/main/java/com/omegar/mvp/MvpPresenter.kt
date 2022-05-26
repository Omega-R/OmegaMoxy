package com.omegar.mvp

import android.os.Bundle
import android.os.Parcelable
import com.omegar.mvp.presenter.PresenterType
import com.omegar.mvp.viewstate.MvpViewState
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.*

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

    internal companion object {
        private const val KEY_FIELDS = "fields"
        var weakBundle: WeakReference<Bundle>? = null

        private var weakSavedState: SavedState<*>? = null
    }

    /**
     * @return view state, casted to view interface for simplify
     */
    @Suppress("UNCHECKED_CAST")
    protected val viewState: View
        get() = mvpViewState as View

    private var firstLaunch = true
    internal var tag: String? = null
        @JvmName("getTag") get
        @JvmName("setTag") set
    internal var presenterType: PresenterType? = null
        @JvmName("getPresenterType") get
        @JvmName("setPresenterType") set

    @Suppress("UNCHECKED_CAST")
    private var mvpViewState: MvpViewState<View> = MoxyReflector.getViewState(javaClass) as MvpViewState<View>

    private val savedFields: MutableList<SavedField<*>> = mutableListOf()

    /**
     * @return views attached to view state, or attached to presenter(if view state not exists)
     */
    val attachedViews: Set<View>
        get() = mvpViewState.attachedViews

    private val fieldsKey: String
        get() = KEY_FIELDS + "_" + javaClass.simpleName

    init {
        weakBundle?.get()?.let {
            weakSavedState = it.getParcelable(fieldsKey)

            onRestoreInstanceState(it)
        } ?: run {
            weakSavedState = null
        }
    }

    protected fun <T : Serializable> savedState(initValue: T): SavedField<T> = SavedField(initValue).applyField()

    protected fun <T : Serializable> savedNullState(initValue: T?): SavedField<T?> = SavedField(initValue, null).applyField()

    protected fun <T : Serializable> savedState() = savedNullState<T>(null)

    protected fun <T : Parcelable?> savedState(initValue: T): SavedField<T> = SavedField(initValue).applyField()

    protected fun <T : Parcelable> savedNullParcelableState(initValue: T? = null): SavedField<T?> {
        return SavedField(initValue, null).applyField()
    }

    protected fun <T> SavedField<T>.applyField() = apply {
        savedFields += this
        weakBundle?.get()?.let { bundle ->
            weakSavedState?.list?.getOrNull(savedFields.lastIndex)?.let {
                @Suppress("UNCHECKED_CAST")
                (it as? T)?.let { newValue ->
                    value = newValue
                }
            }
        }
    }

    private fun onRestoreInstanceState(bundle: Bundle) {
        mvpViewState.loadState(bundle)
    }

    open fun onSaveInstanceState(outState: Bundle) {
        mvpViewState.saveState(outState)
        val state = SavedState(savedFields.map { it.value })
        outState.putParcelable(fieldsKey, state)
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
    open fun attachView(view: View) {
        mvpViewState.attachView(view)
        attachView(view, firstLaunch)
        if (firstLaunch) {
            firstLaunch = false
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
     * @param isFirstAttach is first presenter init and view binding
     */
    protected open fun attachView(view: View, isFirstAttach: Boolean) {
        // nothing
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

    /**
     *
     * Detach view from view state or from presenter(if view state not exists).
     *
     * If you use [MvpDelegate], you should not call this method directly.
     * It will be called on [MvpDelegate.onDetach].
     *
     * @param view view to detach
     */
    open fun detachView(view: View) {
        mvpViewState.detachView(view)
    }

    open fun destroyView(view: View) {
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