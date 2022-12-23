package com.omegar.mvp

import com.omegar.mvp.MvpFacade.presenterStore
import com.omegar.mvp.MvpFacade.presentersCounter
import com.omegar.mvp.presenter.PresenterField
import com.omegar.mvp.presenter.PresenterType
import kotlin.reflect.KClass

/**
 * Date: 18-Dec-15
 * Time: 13:16
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
object MvpProcessor {

    private var hasMoxyReflector: Boolean = try {
        MoxyReflector
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
    private fun <Delegated : Any, Presenter : MvpPresenter<*>> getMvpPresenter(
        target: Delegated,
        presenterField: PresenterField<Delegated, Presenter>,
        delegateTag: String
    ): Presenter {
        return getOrCreateMvpPresenter(
            delegateTag = delegateTag,
            presenterType = presenterField.presenterType,
            presenterClass = presenterField.presenterClass
        ) {
            presenterField.providePresenter(target)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <Presenter : MvpPresenter<*>> getOrCreateMvpPresenter(
        delegateTag: String,
        presenterType: PresenterType,
        presenterClass: KClass<*>,
        presenterFactory: () -> Presenter
    ): Presenter {
        val presenterTag = generatePresenterTag(presenterType, delegateTag, presenterClass)
        return presenterStore[presenterTag] as Presenter? ?: presenterFactory().also { presenter ->
            presenter.presenterType = presenterType
            presenter.presenterTag = presenterTag
        }
    }

    /**
     * Gets presenters [List] annotated with [InjectPresenter] for view.
     *
     * See full info about getting presenter instance in [.getMvpPresenter]
     *
     * @param delegated   class contains presenter
     * @param delegateTag unique tag
     * @param <Delegated> type of delegated
     * @return presenters list for specifies presenters container
    </Delegated> */
    fun <Delegated : Any, P : MvpPresenter<*>> getMvpPresenters(
        delegated: Delegated,
        delegateTag: String,
        customPresenterFields: List<PresenterField<Delegated, P>>?
    ): List<P> {
        return getPresenterBinders<Delegated, P>(delegated::class)
            .flatMap { it.presenterFields }
            .plus(customPresenterFields.orEmpty())
            .map { presenterField ->
                getMvpPresenter(delegated, presenterField, delegateTag).also { presenter ->
                    presentersCounter.injectPresenter(presenter, delegateTag)
                    presenterField.bind(delegated, presenter)
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Delegated : Any, P : MvpPresenter<*>> getPresenterBinders(delegatedClass: KClass<out Delegated>):
            List<PresenterBinder<Delegated, P>> {
        if (!hasMoxyReflector) return emptyList()

        val presenterBinders = MoxyReflector.getPresenterBinders(delegatedClass) as List<PresenterBinder<Delegated, P>>?

        return presenterBinders.orEmpty()
    }

    /**
     * @return generated tag in format: &lt;parent_delegate_tag&gt; &lt;delegated_class_full_name&gt;$MvpDelegate@&lt;hashCode&gt;
     *
     * example: SampleFragment$MvpDelegate@32649b0
     */
    fun generateDelegateTag(delegatedClass: KClass<*>, delegateClass: KClass<*>, uniqueKey: Int): String {
        return delegatedClass.simpleName + "$" + delegateClass.simpleName + "@" + Integer.toHexString(uniqueKey)
    }

    fun generatePresenterTag(type: PresenterType, delegateTag: String, presenterClass: KClass<*>): String {
        return when (type) {
            PresenterType.LOCAL -> delegateTag + presenterClass.qualifiedName
            else -> presenterClass.qualifiedName.orEmpty()
        }
    }

}