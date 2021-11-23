package example.com.moxy_androidx_sample.contract

import androidx.lifecycle.LiveData
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType.*
import example.com.moxy_androidx_sample.BaseView

interface Contract {

    interface MainView : BaseView {

        var value: String

        @StateStrategyType(ADD_TO_END_SINGLE)
        fun printLog(msg: Double?, log: String?)


    }

    interface MainPartView {

        @StateStrategyType(ADD_TO_END_SINGLE)
        fun setName(name: String)

    }

}