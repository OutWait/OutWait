package elite.kit.outwait.managmentLogin.passwordForgottenScreen

import android.util.Log
import androidx.lifecycle.ViewModel

class PasswordForgotViewModel : ViewModel() {
    var institutName:String

    init {
        institutName=""
    }

    fun resetPassword(){
        //TODO Call method of Repository with the para institutName
        Log.i("send","$institutName to reset")
    }
}
