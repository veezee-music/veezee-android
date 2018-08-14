package cloud.veezee.android.api

import android.content.Context
import android.webkit.URLUtil
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.utils.urlMaker
import java.net.URLEncoder

fun API.Lists.Companion.home(context: Context, responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.MAIN_CONTENT);
    AppClient(context).stringRequest(url, responseListener);
}

fun API.Lists.Companion.search(context: Context, q: String, responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.SEARCH_CONTENT, "?q=${URLEncoder.encode(q, "UTF-8")}");
    AppClient(context).stringRequest(url, responseListener);
}

fun API.Lists.Companion.albums(context: Context, lastId: String = "", responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.ALBUMS, "?lastId=${URLEncoder.encode(lastId, "UTF-8")}");
    AppClient(context).stringRequest(url, responseListener);
}

fun API.Lists.Companion.tracks(context: Context, lastId: String = "", responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.TRACKS, "?lastId=${URLEncoder.encode(lastId, "UTF-8")}");
    AppClient(context).stringRequest(url, responseListener);
}

fun API.Lists.Companion.playlists(context: Context, lastId: String = "", responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.PLAYLISTS, "?lastId=${URLEncoder.encode(lastId, "UTF-8")}");
    AppClient(context).stringRequest(url, responseListener);
}