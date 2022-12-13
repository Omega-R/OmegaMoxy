package com.omegar.mvp.ktx

import com.omegar.mvp.MvpDelegateHolder
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.presenter.PresenterField
import com.omegar.mvp.presenter.PresenterType
import kotlin.reflect.KProperty

/**
 * Created by Anton Knyazev on 25.11.2020.
 */

class CustomPresenterFactory<P : MvpPresenter<*>, D>(tag: String, private val factoryBlock: () -> P) :
    PresenterField<D>(tag, PresenterType.LOCAL, null) {

    private var presenter: P? = null

    override fun bind(container: D, presenter: MvpPresenter<*>?) {
        @Suppress("UNCHECKED_CAST")
        this.presenter = presenter as P
    }

    override fun providePresenter(delegated: D): MvpPresenter<*> {
        return factoryBlock()
    }

    operator fun getValue(thisRef: D, property: KProperty<*>): P {
        return presenter!!
    }

}

inline fun <reified P : MvpPresenter<*>, D: Any> MvpDelegateHolder<D>.providePresenter(
    name: String = "presenter",
): CustomPresenterFactory<P, D> {
    val factoryBlock = { P::class.java.newInstance() }
    return CustomPresenterFactory<P, D>(P::class.java.name + "." + name, factoryBlock).also {
        mvpDelegate.enableAutoCreate()
        mvpDelegate.addCustomPresenterFields(it)
    }
}

inline fun <reified P : MvpPresenter<*>, D: Any> MvpDelegateHolder<D>.providePresenter(
    name: String = "presenter",
    noinline factoryBlock: () -> P
): CustomPresenterFactory<P, D> {
    return CustomPresenterFactory<P, D>(P::class.java.name + "." + name, factoryBlock).also {
        mvpDelegate.addCustomPresenterFields(it)
    }
}

inline fun <reified P : MvpPresenter<*>, D: Any> MvpDelegateHolder<D>.providePresenterWithoutAutoCreate(
    name: String = "presenter",
    noinline factoryBlock: () -> P = { P::class.java.newInstance() }
): CustomPresenterFactory<P, D> {
    return CustomPresenterFactory<P, D>(P::class.java.name + "." + name, factoryBlock).also {
        mvpDelegate.disableAutoCreate()
        mvpDelegate.addCustomPresenterFields(it)
    }
}