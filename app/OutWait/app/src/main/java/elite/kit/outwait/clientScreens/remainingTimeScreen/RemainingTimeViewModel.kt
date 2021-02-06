package elite.kit.outwait.clientScreens.remainingTimeScreen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import javax.inject.Inject

@HiltViewModel
class RemainingTimeViewModel  @Inject constructor(private val repo : InstituteRepository): ViewModel() {
    fun navigateBack() {
        TODO("Not yet implemented")
    }
    // TODO: Implement the ViewModel
}
