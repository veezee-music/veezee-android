package cloud.veezee.android.api

import android.content.Context
import android.webkit.URLUtil
import cloud.veezee.android.api.API
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.Track
import cloud.veezee.android.utils.urlMaker
import com.android.volley.Request
import com.google.gson.Gson
import org.json.JSONObject

fun API.Account.PlayLists.Track.Companion.add(context: Context, track: Track, playlist: Album, responseListener: HttpRequestListeners.JsonObjectResponseListener) {

    val params = JSONObject();
    params.put("track", Gson().toJson(track));
    params.put("playlist", Gson().toJson(playlist));

    val url = URLUtil().urlMaker(AppClient.Server.ADD_TRACK_TO_PLAYLIST);
    AppClient(context).jsonObjRequest(url, Request.Method.POST, params, responseListener);
}

fun API.Account.PlayLists.Companion.new(context: Context, title: String, responseListener: HttpRequestListeners.JsonObjectResponseListener) {

    val param = JSONObject();
    param.put("title", title);

    val url = URLUtil().urlMaker(AppClient.Server.CREATE_PLAYLIST);
    AppClient(context).jsonObjRequest(url, Request.Method.POST, param, responseListener);
}

fun API.Account.PlayLists.Companion.get(context: Context, responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.GET_USER_PLAYLISTS);
    AppClient(context).stringRequest(url, responseListener);
}

fun API.Account.PlayLists.Companion.delete(context: Context, responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(AppClient.Server.DELETE_PLAYLIST);
    AppClient(context).stringRequest(url, responseListener);
}

fun API.Account.Companion.login(context: Context, email: String, password: String, responseListener: HttpRequestListeners.JsonObjectResponseListener) {

    val params = JSONObject();
    params.put("email", email);
    params.put("password", password);

    val url = URLUtil().urlMaker(AppClient.Server.LOGIN);
    AppClient(context).jsonObjRequest(url, Request.Method.POST, params, responseListener);
}

fun API.Account.Companion.register(context: Context, email: String, password: String, name: String, responseListener: HttpRequestListeners.JsonObjectResponseListener) {

    val params = JSONObject();
    params.put("email", email);
    params.put("password", password);
    params.put("name", name);

    val url = URLUtil().urlMaker(AppClient.Server.REGISTER);
    AppClient(context).jsonObjRequest(url, Request.Method.POST, params, responseListener);
}

fun API.Account.Companion.logout() {

}

fun API.Account.Companion.validateLogin(context: Context, token: String, responseListener: HttpRequestListeners.JsonObjectResponseListener) {

    val param = JSONObject();
    param.put("token", token);

    val url = URLUtil().urlMaker(AppClient.Server.VALIDATE_LOGIN);
    AppClient(context).jsonObjRequest(url, Request.Method.POST, param, responseListener);
}

fun API.Account.Companion.googleLogin(context: Context, serverAuthCode: String?, responseListener: HttpRequestListeners.JsonObjectResponseListener) {
    val param = JSONObject();
    param.put("serverAuthCode", serverAuthCode);

    val url = URLUtil().urlMaker(AppClient.Server.GOOGLE);
    AppClient(context).jsonObjRequest(url, Request.Method.POST, param, responseListener);
}