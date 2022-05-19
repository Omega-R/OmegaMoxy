package example.com.moxy_androidx_sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.omegar.mvp.ktx.providePresenter
import example.com.moxy_androidx_sample.contract.Contract

class MainActivity : BaseActivity(R.layout.activity_main), Contract.MainView<Double>, SecondInterface {
//	override fun fourth(item: String?) {
//		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//	}
//
//	override fun firstLog(m: Item?) {
//		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//	}
//
//	override fun firstCopyMethod(item: MutableList<Location>?) {
//		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//	}
//
//	override fun thirdMethod() {
//		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//	}
//
//	override fun secondMethod() {
//		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//	}
//
//	override fun firstMethod(item: List<Item>) {
//		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//	}

    override var value: String? = ""

    override var random: Int = Color.BLACK
        set(value) {
            field = value
            Log.e(TAG, "random : $value")
        }

//    override var duration: Duration? = null
//        set(value) {
//            field = value
//            Log.e(TAG, "duration : $value")
//        }

    override fun setTest(test: Float) {

    }

    override fun getTest(): Float {
        return 0f
    }

    private val presenter: MainPresenter by providePresenter {
        MainPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate = ${savedInstanceState != null}")

//		setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")

    }

    override fun printLog(msg: Double?, log: String?) {
        Log.e(TAG, "printLog : msg : $msg activity hash code : ${hashCode()}, log: $log")
    }

    override fun second() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    companion object {
        const val TAG = "MoxyDebug"
    }

    override var boolean: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var list: List<Int>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var array: Array<Int>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var set: Set<Int>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var map: Map<Int, Int>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var mutablelist: MutableList<Int>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var mutableSet: MutableSet<Int>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var mutableMap: MutableMap<Int, Int>
        get() = TODO("Not yet implemented")
        set(value) {}

}
