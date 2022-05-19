package example.com.moxy_androidx_sample

import android.util.Log
import example.com.moxy_androidx_sample.BasePresenter
import example.com.moxy_androidx_sample.MainActivity
import example.com.moxy_androidx_sample.contract.Contract
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds

class MainPresenter : BasePresenter<Contract.MainView<Double>>() {


	init {
		viewState.testFunction()
		viewState.printLog(10.0, "Kek")
		viewState.value = "test";
//		viewState.duration = 5.seconds
		thread {
			Thread.sleep(5000)
			viewState.random = 1000
		}
	}

	override fun onFirstViewAttach() {
		super.onFirstViewAttach()
		Log.e(MainActivity.TAG, "presenter hash code : ${hashCode()}, value = ${viewState.value}")
		viewState.printLog(2.0, viewState.toString())
	}
	
}