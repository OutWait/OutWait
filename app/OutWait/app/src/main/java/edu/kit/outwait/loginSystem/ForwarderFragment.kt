package edu.kit.outwait.loginSystem

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
import edu.kit.outwait.R
import edu.kit.outwait.clientDatabase.ClientInfo

/**
 * Redirects depend of login status a client or an institution to its screen
 *
 */
@AndroidEntryPoint
class ForwarderFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()

    /**
     * Creates view of this fragment
     *
     * @param inflater Instantiates a layout XML file into its corresponding View objects.
     * @param container Includes all view of this fragment
     * @param savedInstanceState A mapping from String keys to various Parcelable values.
     * @return Instantiates a layout XML file into its corresponding View objects.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        userViewModel.loginResponse.observeOnce(viewLifecycleOwner, Observer { listOfUsers ->
            when {
                listOfUsers.isEmpty() -> {
                    Log.i("navigate", "loginFragment")
                    userViewModel.navigateToLoginFragment()
                }
                listOfUsers.isNotEmpty() && listOfUsers.component1() is ClientInfo -> {
                        Log.i("navigate", "remainingFragment ${findNavController().currentDestination?.id} ${R.id.remainingTimeFragment}")
                        userViewModel.navigateToRemainingTimeFragment()

                }
                listOfUsers.isNotEmpty() && listOfUsers.component1() == true -> {
                    userViewModel.navigateToManagementViewFragment()
                }
                listOfUsers.isNotEmpty() && listOfUsers.component1() == false -> {
                    userViewModel.navigateToLoginFragment()
                }
            }
        })

        return inflater.inflate(R.layout.fragment_forwarder, container, false)
    }

    /**
     * Secures to get only one time response of a livedata
     *
     * @param T Type of contained Item
     * @param lifecycleOwner The owner of this livedata with its lifecycle
     * @param observer A simple callback that can receive from LiveData.
     */
    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

}
