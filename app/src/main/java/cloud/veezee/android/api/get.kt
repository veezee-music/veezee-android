package cloud.veezee.android.api

import android.content.Context
import android.webkit.URLUtil
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.utils.urlMaker
import java.net.URLEncoder

fun API.Get.Companion.album(context: Context, id: String = "", responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.ALBUM + "/$id");
    AppClient(context).stringRequest(url, responseListener);
}