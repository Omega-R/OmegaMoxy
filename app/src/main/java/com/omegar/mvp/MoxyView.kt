package com.omegar.mvp

import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType.ONE_EXECUTION
import kotlin.time.Duration

interface MoxyView: BaseView {


    var duration: Duration

    @MoxyViewCommand(ONE_EXECUTION)
    fun test(count: Int)

    @MoxyViewCommand(ONE_EXECUTION)
    fun showToast(message: String)

}