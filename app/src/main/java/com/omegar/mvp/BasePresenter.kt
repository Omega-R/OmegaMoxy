package com.omegar.mvp


/**
 * Created by Anton Knyazev on 09.06.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
@InjectViewState
open class BasePresenter<VIEW: BaseView>: MvpPresenter<VIEW>() {
}