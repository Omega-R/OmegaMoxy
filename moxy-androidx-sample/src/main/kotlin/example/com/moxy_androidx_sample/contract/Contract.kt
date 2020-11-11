package example.com.moxy_androidx_sample.contract

import com.omegar.mvp.viewstate.strategy.StrategyType.*
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import example.com.moxy_androidx_sample.BaseView
import example.com.moxy_androidx_sample.third.ThirdView

interface Contract {

    interface MainView : BaseView {

        @StateStrategyType(ADD_TO_END_SINGLE)
        fun printLog(msg: Double?, log: String?)

    }

}