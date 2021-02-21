package elite.kit.outwait

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.clientScreens.editCodeScreen.EditCodeViewModel
import elite.kit.outwait.navigation.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),androidx.lifecycle.ViewModelStoreOwner {

    @Inject
    lateinit var navigator: Navigator

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = (supportFragmentManager.findFragmentById(R.id.main_container) as? NavHostFragment)
            ?.findNavController()

        lifecycleScope.launchWhenCreated {
            navigator.navigationActions.collect {
                navController?.it()
            }
        }

        viewModel.instituteErrorNotifications().observe(this, Observer {
            val context = this
            CoroutineScope(Main).launch {
                Toast.makeText(context, getString(it.last().message), Toast.LENGTH_LONG).show()
            }
        })
        viewModel.clientErrorNotifications().observe(this, Observer {
                Toast.makeText(this, getString(it.last().message), Toast.LENGTH_LONG).show()
        })
    }




}
