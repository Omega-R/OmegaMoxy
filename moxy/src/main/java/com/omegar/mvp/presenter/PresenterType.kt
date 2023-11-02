package com.omegar.mvp.presenter

/**
 * Available presenter types. Manually lifetime control are available over [PresenterStore], [PresentersCounter] and [MvpPresenter.onDestroy]
 *
 *
 * Date: 17-Dec-15
 * Time: 19:31
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
enum class PresenterType {
    /**
     * Local presenter are not available out of injectable object
     */
    LOCAL,

    /**
     * Weak presenters are available everywhere. Weak presenter will be destroyed when finished all views. Inject will create new presenter instance.
     */
    WEAK,

    /**
     * Global presenter will be destroyed only when process will be killed([MvpPresenter.onDestroy] won't be called)
     */
    GLOBAL
}