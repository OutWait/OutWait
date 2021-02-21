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

        val navController = findNavController()

        /*userViewModel.users.observe(viewLifecycleOwner) { listOfUsers ->
            if (listOfUsers.isEmpty()) {
//                navController.navigate(R.id.loginFragment)
            } else if (listOfUsers.component1() == false) {
                //                navController.navigate(R.id.loginFragment)
            } else if (listOfUsers.component1() == true) {
                navController.navigate(R.id.managmentViewFragment)
            } else if (listOfUsers.component1() is ClientInfo) {
                navController.navigate(R.id.remainingTimeFragment)
            }
        }*/

    }
}
