package elite.kit.outwait.loginSystem

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientRepository.ClientRepository
import elite.kit.outwait.instituteRepository.InstituteRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * It is a  userviewmodel which is depended (lifecycle) on the activity
 *
 * @property repoClient Instance of the repository from client
 * @property repoInstitute Instance of the repository from management
 * @property coordinator Instance to call navigation to other fragments
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repoClient: ClientRepository,
    private val repoInstitute: InstituteRepository,
    private val coordinator: LoginCoordinator
) : ViewModel() {


    /**
     * Saves entered input from client
     */
    val clientSlotCode = MutableLiveData<String>("")

    /**
     * Saves entered input from management
     */
    val instituteName = MutableLiveData<String>("")

    /**
     * Saves entered input from management
     */
    val institutePassword = MutableLiveData<String>("")

    /**
     * Observes whether client or management is logged
     */
    val loginResponse = MediatorLiveData<List<Any>>().apply {
        addSource(repoClient.getActiveSlots()) {
            value = it
        }

        addSource(repoInstitute.isLoggedIn()) {
            value = listOf(it)
        }
    }

    /**
     * Transmits login data from client to server
     *
     */
    fun enterSlotCode() {
        viewModelScope.launch {
            repoClient.newCodeEntered(clientSlotCode.value)
        }
    }

    /**
     * Transmits login data from management to server
     *
     */
    fun login() {
        repoInstitute.login(instituteName.value!!, institutePassword.value!!)
    }

    /**
     * Navigates to RemainingFragment
     *
     */
    fun navigateToRemainingTimeFragment() {
        coordinator.navigateToRemainingTimeFragment()
    }

    /**
     * Navigates to ManagementViewFragment
     *
     */
    fun navigateToManagementViewFragment() {
        coordinator.navigateToManagementViewFragment()
    }

    /**
     * Navigates to LoginFragment
     *
     */
    fun navigateToLoginFragment() {
        coordinator.navigateToLoginFragment()
    }

    /**
     * Navigates to PasswordForgottenFragment
     *
     */
    fun navigateToPasswordForgottenFragment() {
        coordinator.navigateToPasswordForgottenFragment()
    }


}
