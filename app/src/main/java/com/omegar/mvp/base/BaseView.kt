package com.omegar.mvp.base

import com.omegar.mvp.Addon
import com.omegar.mvp.MvpView


/**
 * Created by Anton Knyazev on 09.06.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
interface BaseView<M>: MvpView {

    fun base(value: Addon<M>)

}