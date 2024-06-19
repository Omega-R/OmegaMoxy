package com.omegar.mvp.base

import com.omegar.mvp.MvpPresenter


/**
 * Created by Anton Knyazev on 09.06.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
open class BasePresenter<T: Number, M, VIEW: BaseView<M>>: MvpPresenter<VIEW>() {
}