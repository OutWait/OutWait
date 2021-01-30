package elite.kit.outwait.managmentLogin.institutLoginScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InstitutLoginViewModel : ViewModel() {

    private lateinit var _successfullLoginTime: MutableLiveData<Long>
    val successfullLoginTime:LiveData<Long>
    get() {return _successfullLoginTime}
     var username:String
     var password: String

    init {
        username=""
        password=""
    }

    fun loginTried(){
        Log.i("login","$username + $password")
    }

    fun passwordForgottenString(){
        //TODO Check not empty
        Log.i("forgot","$username")

    }
}
