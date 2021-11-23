package example.com.moxy_androidx_sample

import android.os.Bundle
import android.util.Log

import com.omegar.mvp.ktx.providePresenter
import example.com.moxy_androidx_sample.contract.Contract

class MainActivity : BaseActivity(R.layout.activity_main), Contract.MainView, Contract.MainPartView by MainViewModel() {

	override var value: String = ""


	private val presenter: MainPresenter by providePresenter()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun printLog(msg: Double?, log: String?) {
		Log.e(TAG, "printLog : msg : $msg activity hash code : ${hashCode()}, log: $log")
	}

	companion object {
		const val TAG = "MoxyDebug"
	}

}
