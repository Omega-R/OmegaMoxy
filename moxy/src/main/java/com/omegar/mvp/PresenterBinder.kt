package com.omegar.mvp

import com.omegar.mvp.presenter.PresenterField

/**
 * Date: 18-Dec-15
 * Time: 18:42
 *
 * @author Alexander Blinov
 */
abstract class PresenterBinder<PresentersContainer> {
    abstract val presenterFields: List<PresenterField<PresentersContainer>>
}