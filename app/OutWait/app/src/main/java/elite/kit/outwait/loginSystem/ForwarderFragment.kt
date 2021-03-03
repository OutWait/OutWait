package elite.kit.outwait.loginSystem

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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

        userViewModel.loginResponse.observeOnce(viewLifecycleOwner, Observer { listOfUsers ->
            Log.i("currentDestionation", "${findNavController().currentDestination!!}")
            when {
                listOfUsers.isNotEmpty() && listOfUsers.component1() is ClientInfo -> {
                    Log.i("navigate", "remainingTime")
                        userViewModel.navigateToRemainingTimeFragment()

                }

                listOfUsers.isNotEmpty() && listOfUsers.component1() == true -> {
                    Log.i("navigate", "managementFragment")

                        userViewModel.navigateToManagementViewFragment()
                }

                listOfUsers.isNotEmpty() && listOfUsers.component1() == false -> {
                    Log.i("navigate", "loginFragment")

                        userViewModel.navigateToLoginFragment()


                    //findNavController().navigate(R.id.loginFragment)
                }

                listOfUsers.isEmpty() -> {
                    //findNavController().navigate(R.id.loginFragment)
                    Log.i("navigate", "loginFragment")
                        userViewModel.navigateToLoginFragment()


                }

            }
        })

    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
}
