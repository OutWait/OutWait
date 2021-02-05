package elite.kit.outwait.instituteRepository

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstituteRepository @Inject constructor() {
    fun doSomething(){
        Log.d("InstituteRepo", "method DoSomething is reached in FR2")
    }
}
