package edu.kit.outwait.managmentLogin.institutLoginScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.outwait.instituteRepository.InstituteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class InstitutLoginViewModel @Inject constructor(
    private val repo: InstituteRepository,
    private val coordinator: InstitutCoordinator,
) : ViewModel() {

    init {
        repo.isLoggedIn().observeForever {
            if (it){
                coordinator.navigateToManagementView()
            }
        }
    }

    private val _isinstitutLogged = MutableLiveData<Boolean>(false)

    val isinstitutLogged: LiveData<Boolean> get() = _isinstitutLogged
    val institutName = MutableLiveData<String>()
    val institutPassword = MutableLiveData<String>()

    fun loginTried(){
        loginTried("test", "test")
    }
    fun loginTried(username: String, password: String){
       /* CoroutineScope(Main).launch {
            _isinstitutLogged.value= repo.loginCo("bla", "bla")
            if(_isinstitutLogged.value!!) coordinator.navigateToManagementView()  else repo.doSomething()
        }*/
        repo.login(username, password)
        //coordinator.navigateToManagementView()

    }

    fun passwordForgottenString() {
        coordinator.navigateToPasswordForgot()
    }
}
