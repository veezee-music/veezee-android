package cloud.veezee.android.google

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

import cloud.veezee.android.google.interfaces.GoogleSignOutListener

class GoogleSignInHelper(val context: Context) {

    companion object {
        const val GOOGLE_REQUEST_CODE: Int = 10;

        fun isGooglePlayServiceAvailable(context: Context): Boolean {
            val googleAvailability = GoogleApiAvailability.getInstance();
            val status = googleAvailability.isGooglePlayServicesAvailable(context);
            return if(status != ConnectionResult.SUCCESS) {
                if(googleAvailability.isUserResolvableError(status)) {
                    googleAvailability.getErrorDialog((context as Activity), status, 1).show();
                }
                false;
            } else {
                true;
            }
        }
    }

    private var mGoogleClient: GoogleSignInClient? = null;

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode("awd")
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleClient = GoogleSignIn.getClient(context, gso);
    }

    fun accounts() {
        (context as Activity).startActivityForResult(mGoogleClient?.signInIntent, GOOGLE_REQUEST_CODE);
    }

    fun signOut(signOutListener: GoogleSignOutListener?) {
            mGoogleClient?.revokeAccess()?.addOnCompleteListener {
                mGoogleClient?.signOut()?.addOnCompleteListener {
                    signOutListener?.onCompleted();
                };
            };
    }

    fun signInAccountFromIntent(data: Intent?): GoogleSignInAccount? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return handleSignInResult(task);
    }

    fun getAccount(): GoogleSignInAccount? {
        return if (isSignIn()) {
            GoogleSignIn.getLastSignedInAccount(context);
        } else {
            null;
        }
    }

    fun isSignIn(): Boolean {
        val signIn: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context);
        return signIn != null;
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        return try {
            completedTask.getResult(ApiException::class.java);
        } catch (e: ApiException) {
            null;
        }
    }
}