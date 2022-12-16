package com.omegar.mvp

import com.omegar.mvp.MvpFacade.mvpProcessor
import com.omegar.mvp.MvpFacade.presenterStore
import com.omegar.mvp.MvpFacade.presentersCounter
import com.omegar.mvp.MvpProcessor.generateDelegateTag
import com.omegar.mvp.presenter.PresenterField
import com.omegar.mvp.presenter.PresenterType

/**
 * Date: 18-Dec-15
 * Time: 13:51
 *
 *
 * This class represents a delegate which you can use to extend Mvp's support to any class.
 *
 *
 * When using an [MvpDelegate], lifecycle methods which should be proxied to the delegate:
 *
 *  * [.onCreate]
 *  * [.onAttach]: inside onStart() of Activity or Fragment
 *  * [.onSaveInstanceState]
 *  * [.onDetach]: inside onDestroy() for Activity or onDestroyView() for Fragment
 *  * [.onDestroy]
 *
 *
 *
 * Every [Object] can only be linked with one [MvpDelegate] instance,
 * so the instance returned from [.MvpDelegate]} should be kept
 * until the Object is destroyed.
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 * @author Konstantin Tckhovrebov
 */
open class MvpDelegate<Delegated : Any>(private val delegated: Delegated) {

    companion object {
        private const val KEY_TAG = "MvpDelegate.KEY_TAG"
    }

    private val mCustomPresenterFields: MutableList<PresenterField<Delegated, MvpPresenter<*>>> = ArrayList()
    private lateinit var delegateTag: String
    private var isAttached = false
    private lateinit var presenters: List<MvpPresenter<*>>

    @Suppress("LeakingThis")
    var uniqueKey = System.identityHashCode(this)

    /**
     *
     * Get(or create if not exists) presenters for delegated object and bind
     * them to this object fields
     *
     * @param saveStore with saved state
     */
    fun onCreate(saveStore: MvpSaveStore<*>?) {
        isAttached = false

        //get base tag for presenters
        delegateTag = (saveStore?.getString(KEY_TAG) ?: generateDelegateTag(delegated::class, this::class, uniqueKey))
            .also { delegateTag ->
                //bind presenters to view
                presenters = mvpProcessor.getMvpPresenters(delegated, delegateTag, mCustomPresenterFields)
            }
    }

    /**
     *
     * Attach delegated object as view to presenter fields of this object.
     * If delegate did not enter at [.onCreate](or
 * [.onCreate]) before this method, then view will not be attached to
     * presenters
     */
    fun onAttach() {
        for (presenter in presenters) {
            if (isAttached && presenter.attachedViews.contains(delegated as MvpView)) {
                continue
            }
            presenter.attachView(delegated as MvpView)
        }
        isAttached = true
    }

    /**
     *
     * Detach delegated object from their presenters.
     */
    fun onDetach() {
        for (presenter in presenters) {
            if (!isAttached && !presenter.attachedViews.contains(delegated as MvpView)) {
                continue
            }
            presenter.detachView(delegated as MvpView)
        }
        isAttached = false
    }

    /**
     *
     * View was being destroyed, but logical unit still alive
     */
    fun onDestroyView() {
        presenters.forEach { presenter ->
            presenter.destroyView(delegated as MvpView)
        }
    }

    /**
     *
     * Destroy presenters.
     */
    fun onDestroy() {
        presentersCounter.getAll(delegateTag)
            .forEach { presenter ->
                val isRejected = presentersCounter.rejectPresenter(presenter, delegateTag)
                if (isRejected && presenter.presenterType !== PresenterType.GLOBAL) {
                    presenterStore.remove(presenter.presenterTag)
                    presenter.onDestroy()
                }
            }
    }

    /**
     * Save presenters tag prefix to save state for restore presenters at future after delegate recreate
     *
     * @param outState out state from Android component
     */
    fun onSaveInstanceState(outState: MvpSaveStore<*>) {
        outState.putString(KEY_TAG, delegateTag)
    }

    fun <P : MvpPresenter<*>> addCustomPresenterFields(customPresenterField: PresenterField<Delegated, P>) {
        @Suppress("UNCHECKED_CAST")
        mCustomPresenterFields.add(customPresenterField as PresenterField<Delegated, MvpPresenter<*>>)
    }

}