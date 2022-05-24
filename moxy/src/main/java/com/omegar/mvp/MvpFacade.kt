package com.omegar.mvp

/**
 * Date: 17-Dec-15
 * Time: 19:00
 *
 * @author Alexander Blinov
 * @author Yuri Shmakov
 */
object MvpFacade {
    var presenterStore = PresenterStore()
    var mvpProcessor = MvpProcessor()
    var presentersCounter = PresentersCounter()

    @JvmStatic
    val instance
        get() = this
}