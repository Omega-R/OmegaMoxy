package example.com.moxy_androidx_sample

import android.util.Log
import example.com.moxy_androidx_sample.contract.Contract

class MainPresenter : BasePresenter<Contract.MainView>() {

	init {
		viewState.printLog(10.0, "Kek")
	}

	override fun onFirstViewAttach() {
		super.onFirstViewAttach()
		Log.e(MainActivity.TAG, "presenter hash code : ${hashCode()}, value = ${viewState.value}")
		viewState.printLog(2.0, viewState.toString())
	}
	
}