package cloud.veezee.android.api

import android.content.Context
import android.webkit.URLUtil
import cloud.veezee.android.Constants
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.Track
import cloud.veezee.android.utils.urlMaker
import com.android.volley.Request
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import org.json.JSONObject
import java.io.StringWriter

fun API.Account.PlayLists.Track.Companion.add(context: Context, track: Track, playlist: Album, responseListener: HttpRequestListeners.StringResponseListener) {
    // don't use JSONObject because of formatting issues such as backslashes
    val writer = StringWriter();
    val jsonWriter = JsonWriter(writer);
    jsonWriter.beginObject();
    jsonWriter.name("track");
    jsonWriter.jsonValue(Gson().toJson(track));
    jsonWriter.name("playlist");
    jsonWriter.jsonValue(Gson().toJson(playlist));
    jsonWriter.endObject();
    val params = JSONObject(writer.toString());

    val url = URLUtil().urlMaker(Constants.Server.ADD_TRACK_TO_PLAYLIST);
    AppClient(context).customJsonObjectRequest(url, Request.Method.POST, params, responseListener);
}

fun API.Account.PlayLists.Companion.new(context: Context, title: String, responseListener: HttpRequestListeners.StringResponseListener) {
    val param = JSONObject();
    param.put("title", title);

    val url = URLUtil().urlMaker(Constants.Server.CREATE_PLAYLIST);
    AppClient(context).customJsonObjectRequest(url, Request.Method.POST, param, responseListener);
}

fun API.Account.PlayLists.Companion.get(context: Context, responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(Constants.Server.GET_USER_PLAYLISTS);
    AppClient(context).customJsonObjectRequest(url, Request.Method.GET, null, responseListener);
}

fun API.Account.PlayLists.Companion.delete(context: Context, responseListener: HttpRequestListeners.StringResponseListener) {
    val url = URLUtil().urlMaker(Constants.Server.DELETE_PLAYLIST);
    AppClient(context).customJsonObjectRequest(url, Request.Method.DELETE, null, responseListener);
}

fun API.Account.Companion.login(context: Context, email: String, password: String, responseListener: HttpRequestListeners.StringResponseListener) {
    val params = JSONObject();
    params.put("email", email);
    params.put("password", password);

    val url = URLUtil().urlMaker(Constants.Server.LOGIN);
    AppClient(context).customJsonObjectRequest(url, Request.Method.POST, params, responseListener);
}

fun API.Account.Companion.register(context: Context, email: String, password: String, name: String, responseListener: HttpRequestListeners.StringResponseListener) {
    val params = JSONObject();
    params.put("email", email);
    params.put("password", password);
    params.put("name", name);

    val url = URLUtil().urlMaker(Constants.Server.REGISTER);
    AppClient(context).customJsonObjectRequest(url, Request.Method.POST, params, responseListener);
}

fun API.Account.Companion.updateNameAndPassword(context: Context, name: String, password: String, responseListener: HttpRequestListeners.StringResponseListener) {
    val param = JSONObject();
    param.put("name", name);
    param.put("password", password);

    val url = URLUtil().urlMaker(Constants.Server.UPDATE_NAME_AND_PASSWORD);
    AppClient(context).customJsonObjectRequest(url, Request.Method.POST, param, responseListener);
}

fun API.Account.Companion.logout() {

}

fun API.Account.Companion.validateLogin(context: Context, token: String, responseListener: HttpRequestListeners.StringResponseListener) {
    val param = JSONObject();
    param.put("token", token);

    val url = URLUtil().urlMaker(Constants.Server.VALIDATE_LOGIN);
    AppClient(context).customJsonObjectRequest(url, Request.Method.POST, param, responseListener);
}

fun API.Account.Companion.googleLogin(context: Context, serverAuthCode: String?, responseListener: HttpRequestListeners.StringResponseListener) {
    val param = JSONObject();
    param.put("serverAuthCode", serverAuthCode);

    val url = URLUtil().urlMaker(Constants.Server.GOOGLE);
    AppClient(context).customJsonObjectRequest(url, Request.Method.POST, param, responseListener);
}