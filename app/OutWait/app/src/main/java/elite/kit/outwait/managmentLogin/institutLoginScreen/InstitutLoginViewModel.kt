package elite.kit.outwait.managmentLogin.institutLoginScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class InstitutLoginViewModel @Inject constructor(private val repo : InstituteRepository): ViewModel() {

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
        CoroutineScope(Main).launch {
            if (repo.loginCo("bla", "bla")){
                //switch to Recyclerview Fragment
                Log.d("loginTried::InstLoginVM", "switching Fragment in Thread ${Thread.currentThread().name}")
            } else{
                //show error warning
            }
        }
    }

    fun passwordForgottenString(){
        //TODO Check not empty
        Log.i("forgot","forgot password navigate to other fragment")

    }
}
