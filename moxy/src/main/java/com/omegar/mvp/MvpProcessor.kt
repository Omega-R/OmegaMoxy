package com.omegar.mvp

import com.omegar.mvp.MvpFacade.presenterStore
import com.omegar.mvp.MvpFacade.presentersCounter
import com.omegar.mvp.presenter.PresenterField
import com.omegar.mvp.presenter.PresenterType

/**
 * Date: 18-Dec-15
 * Time: 13:16
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
object MvpProcessor {

    const val PRESENTER_BINDER_SUFFIX = "$\$PresentersBinder"
    const val PRESENTER_BINDER_INNER_SUFFIX = "Binder"
    const val VIEW_STATE_SUFFIX = "$\$State"
    const val VIEW_STATE_PROVIDER_SUFFIX = "$\$ViewStateProvider"
    private var hasMoxyReflector: Boolean = try {
        MoxyReflector()
        true
    } catch (error: NoClassDefFoundError) {
        false
    }

    /**
     *
     * 1) Generates tag for identification MvpPresenter
     *
     * 2) Checks if presenter with tag is already exist in [PresenterStore], and returns it
     *
     * 3) If [PresenterStore] doesn't contain MvpPresenter with current tag, [PresenterField] will create it
     *
     * @param <Delegated>    type of delegated
     * @param target         object that want injection
     * @param presenterField info about presenter from [InjectPresenter]
     * @param delegateTag    unique tag @return MvpPresenter instance
    </Delegated> */
    private fun <Delegated> getMvpPresenter(
        target: Delegated,
        presenterField: PresenterField<Delegated>,
        delegateTag: String
    ): MvpPresenter<*>? {
        val type = presenterField.presenterType
        val tag = when (type) {
            PresenterType.LOCAL -> delegateTag + "$" + presenterField.getTag(target)
            else -> presenterField.getTag(target)
        }

        return presenterStore[tag] ?: let {
            presenterField.providePresenter(target)?.also { presenter ->
                presenter.presenterType = type
                presenter.tag = tag
                presenterStore[tag] = presenter
            }
        }
    }

    /**
     *
     * Gets presenters [List] annotated with [InjectPresenter] for view.
     *
     * See full info about getting presenter instance in [.getMvpPresenter]
     *
     * @param delegated   class contains presenter
     * @param delegateTag unique tag
     * @param <Delegated> type of delegated
     * @return presenters list for specifies presenters container
    </Delegated> */
    fun <Delegated : Any> getMvpPresenters(
        delegated: Delegated,
        delegateTag: String,
        customPresenterFields: List<PresenterField<Delegated>>?
    ): List<MvpPresenter<*>> {
        val presenters: MutableList<MvpPresenter<*>> = ArrayList()

        getPresenterBinders(delegated).forEach { presenterBinder ->
            presenterBinder.presenterFields.forEach { presenterField ->
                handlePresenterField(delegated, delegateTag, presenters, presenterField)
            }
        }

        // handle custom presenter fields
        customPresenterFields?.forEach { presenterField ->
            handlePresenterField(delegated, delegateTag, presenters, presenterField)
        }
        return presenters
    }

    private fun <Delegated : Any> getPresenterBinders(delegated: Delegated): List<PresenterBinder<Delegated>> {
        if (!hasMoxyReflector) return emptyList()

        var aClass: Class<*> = delegated::class.java
        var presenterBinders: List<Any>?
        do {
            presenterBinders = MoxyReflector.getPresenterBinders(aClass)
            aClass = aClass.superclass
        } while (aClass != Any::class.java && presenterBinders == null)

        return presenterBinders?.takeIf { it.isNotEmpty() }.orEmpty() as List<PresenterBinder<Delegated>>
    }

    private fun <Delegated> handlePresenterField(
        delegated: Delegated,
        delegateTag: String,
        presenters: MutableList<MvpPresenter<*>>,
        presenterField: PresenterField<Delegated>
    ) {
        getMvpPresenter(delegated, presenterField, delegateTag)?.let { presenter ->
            presentersCounter.injectPresenter(presenter, delegateTag)
            presenters.add(presenter)
            presenterField.bind(delegated, presenter)
        }
    }

}