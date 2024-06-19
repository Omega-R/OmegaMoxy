package com.omegar.mvp.several

import com.omegar.mvp.MvpAppCompatActivity

class ThreeActivity: MvpAppCompatActivity(), TwoView {

    private val presenter: ThreePresenter by provideThreePresenter { ThreePresenter() }

}