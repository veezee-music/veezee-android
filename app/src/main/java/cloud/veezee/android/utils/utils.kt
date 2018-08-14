package cloud.veezee.android.utils

import android.app.Activity
import android.content.Context
import android.app.ActivityManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import cloud.veezee.android.Constants
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.R
import java.io.File

fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
}

fun hideKeyboard(context: Context) {
    try {
        val view: View = (context as Activity).currentFocus;

        val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
        imm.hideSoftInputFromWindow(view.windowToken, 0);
    } catch (e: Exception) {
    }
}

fun mkDirs(path: String?): Boolean {
    if (!File(path).exists()) {
        return File(path).mkdirs();
    }
    return true;
}

//fun isColorDark(color: Int): Boolean {
//    val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
//    return darkness >= 0.5
//}

fun isOnline(context: Context?): Boolean {
    val cm: ConnectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;
    val netWorkInformation = cm.activeNetworkInfo;

    return netWorkInformation != null && netWorkInformation.isConnectedOrConnecting;
}

fun <T> View.contentReadyToShow(show: Boolean, loading: T?) {

    loading as View;

    if (show) {
            loading.visibility = View.GONE;
            this.visibility = View.VISIBLE;
            this.animate().alpha(1f).setDuration(800).start();
    } else {
        loading.visibility = View.VISIBLE;
        this.visibility = View.GONE;
        this.animate().alpha(0f).setDuration(500).start();
    }
}

fun now(): Long = System.currentTimeMillis();

fun URLUtil.urlMaker(url: String, query: String = ""): String {

    val completeUrl: ArrayList<String> = ArrayList();

    val domain = Constants.API_BASE_URL;
    completeUrl.add(domain);
    completeUrl.add(if (url.toCharArray()[0].toString() == "/") url else "/$url");

    if (query != "")
        completeUrl.add(query);

    return completeUrl.joinToString("");
}

fun requestWith(context: Context): String {
    val appName = context.resources.getString(R.string.app_name);
    val appVersionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName;
    val buildNumber = Build.FINGERPRINT;
    val osVersion = Build.VERSION.SDK_INT;

    return "$appName-$appVersionName:$buildNumber/Android-$osVersion"
}

fun userToken(context: Context): String = UserManager.get(context).token;

fun extractThemeColors(context: Context): ArrayList<Int> {

    val colors: ArrayList<Int> = ArrayList();
    val typeValue = TypedValue();

    context.theme.resolveAttribute(R.attr.primaryText, typeValue, true);
    colors.add(typeValue.data);
    context.theme.resolveAttribute(R.attr.secondaryText, typeValue, true);
    colors.add(typeValue.data);

    return colors;
}