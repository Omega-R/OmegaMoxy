package com.omegar.mvp.several

import com.omegar.mvp.MvpAppCompatActivity

class OneActivity: MvpAppCompatActivity(), OneView {


    private val presenter: OnePresenter by provideOnePresenter { OnePresenter() }

}