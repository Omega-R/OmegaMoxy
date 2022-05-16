package example.com.moxy_androidx_sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.omegar.mvp.ktx.providePresenter
import com.omegar.mvp.presenter.InjectPresenter
import example.com.moxy_androidx_sample.contract.Contract
import kotlin.time.Duration

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

    override var duration: Duration? = null
        set(value) {
            field = value
            Log.e(TAG, "duration : $value")
        }

    private val presenter: MainPresenter by providePresenter {
        MainPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//		setContentView(R.layout.activity_main)
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

}
