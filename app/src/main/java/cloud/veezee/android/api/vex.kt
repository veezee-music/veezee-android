package cloud.veezee.android.api

import android.content.Context
import android.webkit.URLUtil
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.utils.urlMaker
import java.net.URLEncoder

fun API.VEX.Companion.tracksHistory(context: Context, responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.USER_TRACKS_HISTORY);
    AppClient(context).stringRequest(url, responseListener);
}