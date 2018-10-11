package cloud.veezee.android.api

import android.content.Context
import android.webkit.URLUtil
import cloud.veezee.android.Constants
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.utils.urlMaker
import com.android.volley.Request
import java.net.URLEncoder

fun API.VEX.Companion.tracksHistory(context: Context, responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(Constants.Server.USER_TRACKS_HISTORY);
    AppClient(context).customJsonObjectRequest(url, Request.Method.GET, null, responseListener);
}