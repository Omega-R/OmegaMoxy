package example.com.moxy_androidx_sample

import androidx.lifecycle.MutableLiveData
import example.com.moxy_androidx_sample.contract.Contract

class MainViewModel : Contract.MainPartView {

    private val nameLiveData: MutableLiveData<String> = MutableLiveData("")

    override fun setName(name: String) {
        nameLiveData.value = name
    }


}