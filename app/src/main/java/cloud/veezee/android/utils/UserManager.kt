package cloud.veezee.android.utils

import android.content.Context
import com.google.gson.Gson
import cloud.veezee.android.google.GoogleSignInHelper
import cloud.veezee.android.models.Access

class UserManager {

    enum class Account {
        GOOGLE,
        CUSTOM
    }

    var name: String = "";
    var token: String = "";
    private var expiresIn: Long = 0;
    var email: String = "";
    var loginWith: Account? = null;
    var access: Access? = null;
    private var userLogin: Boolean = false;

    var isAccessExpired: Boolean = false
        get() = accessExpire();

    var isTokenExpired: Boolean = false
        get() = tokenExpire();


    var isLoggedIn: Boolean = false
        get() = userLogin;

    companion object {
        const val NAME = "UserManager"

        fun get(context: Context): UserManager {

            return getUser(context);
        }

        private fun getUser(context: Context): UserManager {

            return if (SharedPreferencesHelper.exist(context, NAME)) {
                val serializeAccount = SharedPreferencesHelper.get(context, NAME);
                Gson().fromJson(serializeAccount, UserManager::class.java)
            } else {
                UserManager();
            }
        }

        fun remove(context: Context): Boolean {
            if (SharedPreferencesHelper.exist(context, NAME))
                return SharedPreferencesHelper.delete(context, NAME);
            return false;
        }

        fun syncAccountWithGoogle(context: Context) {
            val googleSignIn = GoogleSignInHelper(context);

            if (googleSignIn.isSignIn()) {
                val googleAccount = googleSignIn.getAccount();
                val account = UserManager();

                account.name = googleAccount?.displayName!!
                account.email = googleAccount.email!!;
                account.set(context);
            }
        }
    }

    private fun accessExpire(): Boolean = access?.expiresIn?.times(1000)!! < System.currentTimeMillis();

    private fun tokenExpire(): Boolean = expiresIn * 1000 < System.currentTimeMillis();

    fun set(context: Context): Boolean {

        userLogin = true;
        userLogin = SharedPreferencesHelper.save(context, NAME, accountToJson());

        return userLogin;
    };

    private fun accountToJson(): String = Gson().toJson(this);
}