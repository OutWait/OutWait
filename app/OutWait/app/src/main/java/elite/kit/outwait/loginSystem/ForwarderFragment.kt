package elite.kit.outwait.loginSystem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import elite.kit.outwait.R
import elite.kit.outwait.clientDatabase.ClientInfo

class ForwarderFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forwarder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.loginResponse.observe(viewLifecycleOwner) { listOfUsers ->
            when {
                listOfUsers.isEmpty() -> {
                    userViewModel.navigateToLoginFragment()
                }
                listOfUsers.component1() == false -> {
                    userViewModel.navigateToLoginFragment()
                }
                listOfUsers.component1() == true -> {
                    userViewModel.navigateToManagementViewFragment()
                }
                listOfUsers.component1() is ClientInfo -> {
                    userViewModel.navigateToRemainingTimeFragment()
                }
            }
        }

    }
}
