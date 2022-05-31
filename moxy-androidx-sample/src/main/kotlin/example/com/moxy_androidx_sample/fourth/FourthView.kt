package example.com.moxy_androidx_sample.fourth

import com.omegar.mvp.viewstate.strategy.StrategyType.ADD_TO_END_SINGLE
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import example.com.moxy_androidx_sample.BaseView

interface FourthView<R> : BaseView {

    @MoxyViewCommand(ADD_TO_END_SINGLE)
    fun fourth(item: R)

}