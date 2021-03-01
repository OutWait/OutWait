package elite.kit.outwait.loginSystem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.clientDatabase.ClientInfo
@AndroidEntryPoint
class ForwarderFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_forwarder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.loginResponse.observe(viewLifecycleOwner) { listOfUsers ->
            when {
                listOfUsers.isEmpty() -> {
                    findNavController().navigate(R.id.loginFragment)
                }
                listOfUsers.component1() == false -> {
                    findNavController().navigate(R.id.loginFragment)
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
