package com.omegar.mvp.several

import com.omegar.mvp.MvpAppCompatActivity

class TwoActivity: MvpAppCompatActivity(), TwoView {

    private val presenter: TwoPresenter by provideTwoPresenter { TwoPresenter() }

}