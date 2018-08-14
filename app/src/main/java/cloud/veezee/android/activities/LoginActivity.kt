package cloud.veezee.android.activities

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.*
import cloud.veezee.android.services.AudioService
import cloud.veezee.android.google.GoogleSignInHelper
import kotlinx.android.synthetic.main.activity_login_content.*
import org.json.JSONObject
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import cloud.veezee.android.Constants
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.gson.Gson
import cloud.veezee.android.api.*
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.application.App
import cloud.veezee.android.google.interfaces.GoogleSignOutListener
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.utils.*
import cloud.veezee.android.R
import cloud.veezee.android.api.googleLogin
import cloud.veezee.android.api.login
import cloud.veezee.android.api.register
import cloud.veezee.android.api.validateLogin


class LoginActivity : AppCompatActivity() {

    enum class Views {
        LOGIN,
        ACCOUNT
    }

    companion object {
        private const val TAG = "LoginActivity Console"
    }

    private var context: Context = this
    private var dialog: AlertDialog? = null;
    private var allowSubmit: Boolean = true;
    private var user: UserManager? = null;
    private var canShowOfflineButton: Boolean = false;

    private var google: GoogleSignInHelper? = null;
    private var googleSignInHelper: GoogleSignInHelper? = null
        get() {
            if (google == null)
                google = GoogleSignInHelper(context);

            return google!!;
        }

    private var mainLoading: ProgressBar? = null;
    private var loginLoading: ProgressBar? = null;
    private var logo: ImageView? = null;
    private var logoContainer: FrameLayout? = null;
    private var loginContainer: LinearLayout? = null;
    private var submit: Button? = null;
    private var loginButton: Button? = null;
    private var registerButton: Button? = null;
    private var googleButton: Button? = null;
    private var retryButton: Button? = null;
    private var offlineButton: Button? = null;
    private var root: ConstraintLayout? = null;

    private var loginContainerAnimationListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {

        }

        override fun onAnimationCancel(animation: Animator?) {

        }

        override fun onAnimationStart(animation: Animator?) {
            loginContainer?.visibility = View.VISIBLE;
        }
    }

    private var logoAnimationListener = object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
            if(!Constants.GUEST_MODE) {
                if (user!!.isLoggedIn) {
                    if (App.offlineMode) {
                        readyToLaunchOfflineMode();
                    } else {
                        validateLogin();
                    }
                } else {
                    entryAnimation(800);
                }
            } else {
                redirectToMainPage(800);
            }
        }

        override fun onAnimationStart(animation: Animation?) {

        }
    };


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_content);

        App.offlineMode = !isOnline(context);

        user = UserManager.get(context);
        initComponents();

        if (isMyServiceRunning(context, AudioService::class.java)) {
            redirectToMainPage(1);
            return;
        }

        blinkerAnimation(logo);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GoogleSignInHelper.GOOGLE_REQUEST_CODE) {
            loginWithGoogle(googleSignInHelper?.signInAccountFromIntent(data));
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AppClient.cancelLastRequest();
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

//    private fun fixStatusBarColor() {
//        StatusBarHelper.clearLightStatusBar(context, window.decorView, ContextCompat.getColor(context, R.color.black_primary_dark));
//    }

    private fun initComponents() {
        root = login_root;
        logo = login_logo;
        mainLoading = validate_loading;
        logoContainer = logo_container;
        loginButton = login_button;
        registerButton = register_button;
        googleButton = login_google_button;
        retryButton = retry_button;
        loginContainer = login_container;
        offlineButton = offline_button;
    }

    private fun readyToLaunchOfflineMode() {
        if (!user!!.isTokenExpired && App.setting!!.offlineAccess) {
            redirectToMainPage(800);
        } else {
            logout();
            entryAnimation(800);
            offlineButton?.visibility = View.INVISIBLE;
            retryButton?.visibility = View.INVISIBLE;
        }
    }

    private fun submit(email: String, password: String, name: String? = null) {
        if (allowSubmit) {
            allowSubmit = false;
            loginLoading?.visibility = View.VISIBLE;
            submit?.text = getString(R.string.loading);
            if (name == null)
                login(email, password);
            else
                register(email, password, name);
        }
    }

    private fun login(email: String, password: String) {
        API.Account.login(context, email, password, object : HttpRequestListeners.JsonObjectResponseListener {

            override fun headers(json: JSONObject) {
            }

            override fun error(er: JSONObject) {
                showToast(er.getString(AppClient.ERROR_MESSAGE));
                resetUIonRequestFailed();
            }

            override fun response(response: JSONObject) {
                val user = mkUserFromJson(response);
                user.loginWith = UserManager.Account.CUSTOM;
                user.set(context);

                App.autoLoginSessionExpireDate = now();
                redirectToMainPage(1000);
            }
        });
    }

    private fun register(email: String, password: String, name: String) {
        API.Account.register(context, email, password, name, object : HttpRequestListeners.JsonObjectResponseListener {
            override fun response(response: JSONObject) {
                login(email, password);
            }

            override fun headers(json: JSONObject) {

            }

            override fun error(er: JSONObject) {
                showToast(er.getString(AppClient.ERROR_MESSAGE));
                resetUIonRequestFailed();
            }
        });
    }

    private fun validateLogin() {
        val token = userToken(context);

        mainLoading?.visibility = View.VISIBLE;
        retryButton?.visibility = View.GONE;
        offlineButton?.visibility = View.GONE;

        API.Account.validateLogin(context, token, object : HttpRequestListeners.JsonObjectResponseListener {
            override fun response(response: JSONObject) {

                val user = mkUserFromJson(response);
                user.loginWith = UserManager.get(context).loginWith;
                user.set(context);

                App.autoLoginSessionExpireDate = now();
                redirectToMainPage(1000);
            }

            override fun headers(json: JSONObject) {

            }

            override fun error(er: JSONObject) {
                val serverStatusCode: Int = er.getInt(AppClient.STATUS_CODE);

                mainLoading?.visibility = View.INVISIBLE;
                if (serverStatusCode == 410 || serverStatusCode == 500) {
                    entryAnimation(2000);
                    logout();
                } else {
                    showFailedButtons();
                }
            }
        });
    }

    private fun googleAccounts() {
        googleSignInHelper?.accounts();
    }

    private fun loginWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {

            API.Account.googleLogin(context, account.serverAuthCode, object : HttpRequestListeners.JsonObjectResponseListener {
                override fun response(response: JSONObject) {

                    val user = mkUserFromJson(response);
                    user.loginWith = UserManager.Account.GOOGLE;
                    user.set(context);

                    App.autoLoginSessionExpireDate = now();
                    redirectToMainPage(1000);
                }

                override fun headers(json: JSONObject) {

                }

                override fun error(er: JSONObject) {
                    showToast(context.getString(R.string.google_error));
                }
            });
        } else {
            showToast(context.getString(R.string.google_error));
        }
    }

    private fun createDialog(view: View): AlertDialog {

        val dialogBox: AlertDialog.Builder = AlertDialog.Builder(this);
        dialogBox.setView(view);
        dialogBox.setCancelable(false);

        val cDialog = dialogBox.create();
        cDialog.window.setBackgroundDrawableResource(android.R.color.transparent);

        return cDialog;
    }

    private fun logout() {
        googleSignInHelper?.signOut(object : GoogleSignOutListener {
            override fun onCompleted() {
                UserManager.remove(context);
                return;
            }
        });
        UserManager.remove(context);
    }

    /**
     * Show register dialog
     */
    private fun registerDialog(view: View) {

        dialog = createDialog(view);

        submit = view.findViewById(R.id.dialog_register_submit);
        loginLoading = view.findViewById(R.id.dialog_register_submit_loading)
        val cancel = view.findViewById<CardView>(R.id.dialog_register_cancel);
        val name = view.findViewById<TextInputEditText>(R.id.dialog_register_display_name);
        val email = view.findViewById<TextInputEditText>(R.id.dialog_register_email);
        val password = view.findViewById<TextInputEditText>(R.id.dialog_register_password);

        submit?.setOnClickListener({
            submit(email.text.toString(), password.text.toString(), name.text.toString());
        });

        cancel?.setOnClickListener({
            dialog?.dismiss();
            AppClient.cancelLastRequest();
        })

        dialog?.show();
    }

    /**
     * Show login dialog
     */
    private fun loginDialog(view: View) {

        dialog = createDialog(view);

        submit = view.findViewById(R.id.dialog_login_submit);
        loginLoading = view.findViewById(R.id.dialog_register_submit_loading)
        val cancel = view.findViewById<CardView>(R.id.dialog_login_cancel);
        val email = view.findViewById<TextInputEditText>(R.id.dialog_login_email);
        val password = view.findViewById<TextInputEditText>(R.id.dialog_login_password);


        submit?.setOnClickListener({
            submit(email.text.toString(), password.text.toString());
        });

        cancel?.setOnClickListener({
            dialog?.dismiss();
            AppClient.cancelLastRequest();
        })

        dialog?.show();
    }

    /**
     * Inflate login dialog/register dialog layout
     */
    private fun getView(index: LoginActivity.Views): View {
        return when (index) {
            LoginActivity.Views.LOGIN -> {
                LayoutInflater.from(context).inflate(R.layout.dialog_login, null);
            }

            LoginActivity.Views.ACCOUNT -> {
                LayoutInflater.from(context).inflate(R.layout.dialog_register, null);
            }
        }
    }

    /**
     * Logo blinker animation
     */
    private fun blinkerAnimation(view: View?) {

        val blinker = AnimationUtils.loadAnimation(context, R.anim.blink_animation);
        blinker?.setAnimationListener(logoAnimationListener);
        view?.startAnimation(blinker);
    }

    /**
     *
     * Translate Veezee's logo to the top of the screen to show login dialog
     *
     */
    private fun entryAnimation(delay: Long, duration: Long = 1000) {

        object : CountDownTimer(delay, delay) {

            override fun onFinish() {
                val logoHeight = logo?.height;
                val rootHeight = root?.height;
                val statusBarHeight = StatusBarHelper.getHeight(window);

                val extraSpace = ((rootHeight!! * 2) / 100);

                val logoContainerHeight = (logoHeight!! / 2) + (extraSpace * 2) + statusBarHeight;
                logoContainer?.layoutParams?.height = logoContainerHeight;
                logoContainer?.requestLayout();

                logo?.animate()?.translationY(((rootHeight / 2) - (logoHeight / 4) - extraSpace - statusBarHeight).toFloat() * -1)
                        ?.scaleY(0.5f)
                        ?.scaleX(0.5f)
                        ?.setDuration(duration)?.interpolator = AccelerateInterpolator();

//                logoContainer?.animate()?.translationY(toFloat(logoContainerHeight))
//                        ?.setDuration(duration)?.interpolator = AccelerateInterpolator();
//
                loginContainer?.animate()?.alpha(1f)
                        ?.setDuration(duration)
                        ?.setStartDelay(duration / 2)
                        ?.setListener(loginContainerAnimationListener)
                        ?.start();
            }

            override fun onTick(millisUntilFinished: Long) {

            }
        }.start();
    }

    private fun resetUIonRequestFailed() {
        allowSubmit = true;
        loginLoading?.visibility = View.INVISIBLE;
        submit?.text = getString(R.string.submit);
    }

    /**
     * Retry button and offline mode button َare shown
     */
    private fun showFailedButtons() {
        if (canShowOfflineButton) {
            retryButton?.visibility = View.VISIBLE;
            offlineButton?.visibility = View.VISIBLE;

            object : CountDownTimer(100, 100) {
                override fun onFinish() {
                    val halfButtonWidth = retryButton?.width!! / 2

                    retryButton?.animate()?.translationX(-halfButtonWidth.toFloat())?.start();
                    offlineButton?.animate()?.translationX(halfButtonWidth.toFloat())?.start();
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }.start();
        } else {
            retryButton?.visibility = View.VISIBLE;
            canShowOfflineButton = true;
        }
    }

    /**
     * Start HomePageActivity
     */
    private fun redirectToMainPage(delay: Long) {
        submit?.text = context.getString(R.string.loading);
        val homePageActivity = Intent(this, HomePageActivity::class.java);

        object : CountDownTimer(delay, delay) {
            override fun onFinish() {

                dialog?.dismiss();

                startActivity(homePageActivity);
                finish();
            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }.start();
    }

    private fun mkUserFromJson(user: JSONObject): UserManager = Gson().fromJson(user.toString(), UserManager::class.java);

    /**
     * Show network response message
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Buttons onClick method
     */
    fun buttons(view: View) {

        val loginId = loginButton?.id
        val accountId = registerButton?.id;
        val googleId = googleButton?.id;
        val retryButtonId = retryButton?.id;
        val offlineButtonId = offlineButton?.id;

        when (view.id) {
            loginId -> {
                loginDialog(getView(LoginActivity.Views.LOGIN));
            }
            accountId -> {
                registerDialog(getView(LoginActivity.Views.ACCOUNT));
            }
            googleId -> {
                if (GoogleSignInHelper.isGooglePlayServiceAvailable(context)) {
                    if (allowSubmit) {
                        allowSubmit = false;
                        googleButton?.text = context.getString(R.string.loading);
                        googleAccounts();
                    }
                }
            }
            retryButtonId -> {
                validateLogin();
            }
            offlineButtonId -> {
                App.offlineMode = true;
                readyToLaunchOfflineMode();
            }
        }
    }

}
