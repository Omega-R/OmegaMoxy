package com.omegar.mvp

import com.omegar.mvp.presenter.PresenterField
import com.omegar.mvp.presenter.PresenterType
import kotlin.reflect.KProperty

/**
 * Created by Anton Knyazev on 25.11.2020.
 */

class CustomPresenterFactory<P : MvpPresenter<*>>(tag: String, private val factoryBlock: () -> P) :
    PresenterField<Any?>(tag, PresenterType.LOCAL, null) {

    private var presenter: P? = null

    override fun bind(container: Any?, presenter: MvpPresenter<*>?) {
        @Suppress("UNCHECKED_CAST")
        this.presenter = presenter as P
    }

    override fun providePresenter(delegated: Any?): MvpPresenter<*> {
        return factoryBlock()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): P {
        return presenter!!
    }

}

inline fun <reified P : MvpPresenter<*>> MvpDelegateHolder.providePresenter(
    name: String = "presenter",
): CustomPresenterFactory<P> {
    val factoryBlock = { P::class.java.newInstance() }
    return CustomPresenterFactory(P::class.java.name + "." + name, factoryBlock).also {
        mvpDelegate.enableAutoCreate()
        mvpDelegate.addCustomPresenterFields(it)
    }
}

inline fun <reified P : MvpPresenter<*>> MvpDelegateHolder.providePresenter(
    name: String = "presenter",
    noinline factoryBlock: () -> P
): CustomPresenterFactory<P> {
    return CustomPresenterFactory(P::class.java.name + "." + name, factoryBlock).also {
        mvpDelegate.addCustomPresenterFields(it)
    }
}

inline fun <reified P : MvpPresenter<*>> MvpDelegateHolder.providePresenterWithoutAutoCreate(
    name: String = "presenter",
    noinline factoryBlock: () -> P = { P::class.java.newInstance() }
): CustomPresenterFactory<P> {
    return CustomPresenterFactory(P::class.java.name + "." + name, factoryBlock).also {
        mvpDelegate.disableAutoCreate()
        mvpDelegate.addCustomPresenterFields(it)
    }
}