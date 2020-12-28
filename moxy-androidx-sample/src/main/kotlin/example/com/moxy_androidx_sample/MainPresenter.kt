package example.com.moxy_androidx_sample

import android.util.Log
import example.com.moxy_androidx_sample.contract.Contract

class MainPresenter : BasePresenter<Contract.MainView<Double>>() {
	
	override fun onFirstViewAttach() {
		super.onFirstViewAttach()
		Log.e(MainActivity.TAG, "presenter hash code : ${hashCode()}")
		viewState.printLog(10.0, "Kek")
	}
	
}