//package ir.veezee.android.utils
//
//import android.content.Context
//import android.content.Intent
//import android.webkit.URLUtil
//import com.android.volley.Request
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.gson.Gson
//import ir.veezee.android.R
//import ir.veezee.android.google.GoogleSignInHelper
//import ir.veezee.android.api.utils.AppClient
//import ir.veezee.android.google.interfaces.GoogleSignOutListener
//import ir.veezee.android.api.utils.interfaces.HttpRequestListeners
//import ir.veezee.android.interfaces.UserLoginResponseListener
//import org.json.JSONObject
//
//class BaseUserLogin(val context: Context) {
//
//    private val TAG = "BaseUserLogin Console";
//
//    private var google: GoogleSignInHelper? = null;
//    private var googleSignIn: GoogleSignInHelper? = null
//        get() = getGoogleClient();
//
//    private var loginListener: UserLoginResponseListener? = null;
//    private val request = AppClient(context);
//
//    private fun mkUserFromJson(user: JSONObject): UserManager = Gson().fromJson(user.toString(), UserManager::class.java);
//
//    private fun removeSessions() {
//
//    }
//
//    fun logout() {
//        googleSignIn?.signOut(object : GoogleSignOutListener {
//            override fun onCompleted() {
//                UserManager.remove(context);
//                return;
//            }
//        });
//        UserManager.remove(context);
//    }
//
//    private fun getGoogleClient(): GoogleSignInHelper {
//        if (google == null)
//            google = GoogleSignInHelper(context);
//
//        return google!!;
//    }
//
//    fun showGoogleAccounts() {
//        googleSignIn?.signOut(null);
//        googleSignIn?.accounts();
//    }
//
//
//    fun googleSignIn(data: Intent) {
//        loginWithGoogle(googleSignIn?.signInAccountFromIntent(data));
//    }
//
//    private fun loginWithGoogle(account: GoogleSignInAccount?) {
//
//        if (account != null) {
//
//
//            val jsnObj = AppClient(context);
//            jsnObj.jsonObjRequest(url, Request.Method.POST, params, object : HttpRequestListeners.JsonObjectResponseListener {
//
//                override fun headers(json: JSONObject) {
//
//                }
//
//                override fun error(er: JSONObject) {
//                    loginListener?.failed(, er.getInt(AppClient.STATUS_CODE), GOOGLE);
//                }
//
//                override fun response(json: JSONObject) {
//
//
//                    loginListener?.success(GOOGLE);
//                }
//            });
//
//        } else {
//            loginListener?.failed(context.getString(R.string.google_error), -2, GOOGLE);
//        }
//    }
//
//    /**
//     * Cancels the request
//     */
//    fun cancel() {
//        request.cancelLastRequest();
//    }
//
//}