package com.omegar.mvp

import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType.ADD_TO_END_SINGLE
import kotlin.time.Duration

interface MoxyView: MvpView {


    var duration: Duration

    @MoxyViewCommand(ADD_TO_END_SINGLE)
    fun test()

}