package example.com.moxy_androidx_sample

import android.graphics.Bitmap
import android.util.Log
import example.com.moxy_androidx_sample.contract.Contract
import kotlin.concurrent.thread

class MainPresenter : BasePresenter<Contract.MainView<Double>>() {

	private var test1: String? by savedState("test1")
	private var test2: String? by savedNullState("test2", "null")
	private var test3: String by savedState("test3", "null")
	private var test5: Int by savedState("test4", 56)
	private var test6: Bitmap by savedState("test5", Bitmap.createBitmap(10,10,Bitmap.Config.ARGB_8888))

	init {
		Log.w("Moxy", "test1 = $test1")
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
		test1 = "Anton"
		Log.e(MainActivity.TAG, "presenter hash code : ${hashCode()}, value = ${viewState.value}")
		viewState.printLog(2.0, viewState.toString())
	}
	
}