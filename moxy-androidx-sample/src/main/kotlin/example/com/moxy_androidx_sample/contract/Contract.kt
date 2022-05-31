package example.com.moxy_androidx_sample.contract

import com.omegar.mvp.viewstate.MoxyDefaultValue
import com.omegar.mvp.viewstate.SerializeType
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType.*
import example.com.moxy_androidx_sample.BaseView
import kotlin.time.Duration

interface Contract {

    interface MainView<D : Number> : BaseView {

        var value: String?

        @set:MoxyViewCommand(ADD_TO_END_SINGLE)
        var duration: Duration

        var boolean: Boolean?
            get() { return true }
            set(value) {}


        var list: List<Int>

        var array: Array<Int>

        var set: Set<Int>

        var map: Map<Int, Int>

        var mutablelist: MutableList<Int>

        var mutableSet: MutableSet<Int>

        var mutableMap: MutableMap<Int, Int>

        @set:MoxyViewCommand(ADD_TO_END_SINGLE, serializeType = SerializeType.PARCELABLE)
        var random: Int


        @MoxyViewCommand(ADD_TO_END_SINGLE_TAG, singleInstance = false, serializeType = SerializeType.PARCELABLE)
        fun printLog(msg: D? = null, vararg log: String?) {

        }

        @MoxyViewCommand(ADD_TO_END_SINGLE)
        fun test(duration: Duration)

        @MoxyViewCommand(ADD_TO_END_SINGLE)
        override fun testFunction()

    }


}

