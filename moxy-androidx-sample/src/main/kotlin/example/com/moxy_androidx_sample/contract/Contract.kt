package example.com.moxy_androidx_sample.contract

import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType.*
import example.com.moxy_androidx_sample.BaseView

interface Contract {

    interface MainView<D: Number> : BaseView {

        var value: String

        @StateStrategyType(ADD_TO_END_SINGLE, singleInstance = true)
        fun printLog(msg: D?, log: String?)

    }

}