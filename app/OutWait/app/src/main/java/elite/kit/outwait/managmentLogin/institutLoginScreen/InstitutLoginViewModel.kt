package elite.kit.outwait.managmentLogin.institutLoginScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.joda.time.DateTime

class InstitutLoginViewModel : ViewModel() {

    private lateinit var _successfullLoginTime: MutableLiveData<DateTime>
    val successfullLoginTime:LiveData<DateTime>
    get() {return _successfullLoginTime}
     var username:String
     var password: String

    init {
        username=""
        password=""
    }

    fun loginTried(){
        if(username.isNotEmpty() && password.isNotEmpty()){
        Log.i("login","$username + $password")}
    }

    fun passwordForgottenString(){
        //TODO Check not empty
        Log.i("forgot","forgot password navigate to other fragment")

    }
}
