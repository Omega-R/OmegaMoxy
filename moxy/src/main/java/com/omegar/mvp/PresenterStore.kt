package com.omegar.mvp

/**
 * Date: 17-Dec-15
 * Time: 16:05
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
object PresenterStore {
    private val presenters: MutableMap<String, MvpPresenter<*>> = HashMap()

    /**
     * Add presenter to storage
     *
     * @param tag      Tag of presenter. Local presenters contains also delegate's tag as prefix
     * @param instance Instance of MvpPresenter implementation to store
     * @param <T>      Type of presenter
    </T> */
    operator fun set(tag: String, instance: MvpPresenter<*>) = presenters.put(tag, instance)

    /**
     * Get presenter on existing params
     *
     * @param tag      Tag of presenter. Local presenters contains also delegate's tag as prefix
     * @return         Presenter if it's exists. Null otherwise (if it's no exists)
     */
    operator fun get(tag: String): MvpPresenter<*>? = presenters[tag]

    /**
     * Remove presenter from store.
     *
     * @param tag      Tag of presenter. Local presenters contains also delegate's tag as prefix
     * @return         Presenter which was removed
     */
    fun remove(tag: String): MvpPresenter<*>? = presenters.remove(tag)

    /**
     * Log all presenters from store.
     */
    fun logPresenters() {
        presenters.forEach { (key, value) ->
            println("PresenterStore: $key -> $value")
        }
    }


}