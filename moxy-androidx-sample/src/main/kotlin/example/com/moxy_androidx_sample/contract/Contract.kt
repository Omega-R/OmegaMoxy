package example.com.moxy_androidx_sample.contract

import com.omegar.mvp.viewstate.SerializeType
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType.*
import example.com.moxy_androidx_sample.BaseView
import kotlin.time.Duration

interface Contract {

    interface MainView<D: Number> : BaseView {

        var value: String?

        var duration: Duration?

        var boolean: Boolean

        var list: List<Int>

        var array: Array<Int>

        var set: Set<Int>

        var map: Map<Int, Int>

        var mutablelist: MutableList<Int>

        var mutableSet: MutableSet<Int>

        var mutableMap: MutableMap<Int, Int>

        var random: Int

        @StateStrategyType(ADD_TO_END_SINGLE_TAG, singleInstance = false, serializeType = SerializeType.PARCELABLE)
        fun printLog(msg: D? = null, vararg log: String?)

        @StateStrategyType(ADD_TO_END_SINGLE)
        fun test(duration: Duration)

    }


}

