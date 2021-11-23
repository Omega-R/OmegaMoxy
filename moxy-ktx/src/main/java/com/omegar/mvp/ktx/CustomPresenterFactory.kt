package com.omegar.mvp.ktx

import com.omegar.mvp.MvpDelegateHolder
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.presenter.PresenterField
import com.omegar.mvp.presenter.PresenterType
import kotlin.reflect.KProperty

/**
 * Created by Anton Knyazev on 25.11.2020.
 */

class CustomPresenterFactory<P : MvpPresenter<*>>(tag: String, clz: Class<P>, private val factoryBlock: () -> P) :
        PresenterField<Any?>(tag, PresenterType.LOCAL, null, clz) {

    private var presenter: P? = null

    @Suppress("UNCHECKED_CAST")
    override fun bind(container: Any?, presenter: MvpPresenter<*>) {
        this.presenter = presenter as P
    }

    override fun providePresenter(delegated: Any?): MvpPresenter<*> = factoryBlock()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): P = presenter!!

}

inline fun <reified P : MvpPresenter<*>> MvpDelegateHolder.providePresenter(
        name: String = "presenter",
        noinline factoryBlock: () -> P = { P::class.java.getDeclaredConstructor().newInstance() }
): CustomPresenterFactory<P> {
    return CustomPresenterFactory(P::class.java.name + "." + name, P::class.java, factoryBlock).also {
        mvpDelegate.addCustomPresenterFields(it)
    }
}
