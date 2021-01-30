package elite.kit.outwait.managmentLogin.institutLoginScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InstitutLoginViewModel : ViewModel() {

    private lateinit var _successfullLoginTime: MutableLiveData<Long>
    val successfullLoginTime:LiveData<Long>
    get() {return _successfullLoginTime}

    fun loginTried(username:String, password:String){

    }

    fun passwordForgottenString(username:String){

    }
}
