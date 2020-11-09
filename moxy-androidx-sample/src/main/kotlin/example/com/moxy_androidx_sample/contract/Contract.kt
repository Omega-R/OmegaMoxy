package example.com.moxy_androidx_sample.contract

import com.omegar.mvp.viewstate.strategy.BasicStrategyType.*
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import example.com.moxy_androidx_sample.BaseView
import example.com.moxy_androidx_sample.fifth.Contract
import example.com.moxy_androidx_sample.first.FirstView
import example.com.moxy_androidx_sample.packagee.Item
import example.com.moxy_androidx_sample.second.SecondView

interface Contract {

    interface MainView : BaseView {

        @StateStrategyType(ADD_TO_END_SINGLE)
        fun printLog(msg: Double?, log: String?)

    }

}