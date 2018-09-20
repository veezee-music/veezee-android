package cloud.veezee.android.api.utils

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import cloud.veezee.android.R
import cloud.veezee.android.utils.requestWith
import cloud.veezee.android.utils.userToken
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners

import org.json.JSONObject

class AppClient(val context: Context) {

    private val TIME_OUT: Int = 20000;

    companion object {
        const val DATA = "data";
        const val HEADER = "headers";
        const val ERROR = "error";

        const val STATUS_CODE = "statusCode";
        const val ERROR_MESSAGE = "message";

        private const val REQUEST_TAG: String = "volley request";

        private var currentQueue: RequestQueue? = null;

        /**
         * Cancels all HTTP requests that are the same as the last one in the client queue
         */
        fun cancelLastRequest() {
            AppClient.currentQueue?.cancelAll(REQUEST_TAG);
        }

    }

    class Server {
        companion object {
            const val GOOGLE = "/account/google/process-login";
            const val REGISTER = "/account/register";
            const val LOGIN = "/account/login";
            const val VALIDATE_LOGIN = "/account/validate-login";
            const val SEARCH_CONTENT = "/get/search";
            const val MAIN_CONTENT = "/get/home-page-collection";
            const val ALBUMS = "/get/albums";
            const val TRACKS = "/get/tracks";
            const val ALBUM = "/get/album";
            const val PLAYLISTS = "/get/playlists";
            const val GET_USER_PLAYLISTS = "/account/playlists/get";
            const val CREATE_PLAYLIST = "/account/playlists/new";
            const val DELETE_PLAYLIST = "/account/playlists/delete";
            const val ADD_TRACK_TO_PLAYLIST = "/account/playlists/tracks/add";
        }
    }

    init {
        AppClient.currentQueue = Volley.newRequestQueue(context);
    }

    private var stringListeners: HttpRequestListeners.StringResponseListener? = null;
    private var jsonObjListeners: HttpRequestListeners.JsonObjectResponseListener? = null;

    private val errorListener = Response.ErrorListener { error ->
        val errorObject = handleVolleyErrors(error);

        stringListeners?.error(errorObject);
        jsonObjListeners?.error(errorObject);
    }

    private val stringResponseListener = Response.Listener<String> { response ->
        stringListeners?.response(response);
    }

    private val jsonObjResponseListener = Response.Listener<JSONObject> { response ->

        jsonObjListeners?.response(response.getJSONObject(DATA));
        jsonObjListeners?.headers(response.getJSONObject(HEADER));
    }

    fun stringRequest(url: String, listener: HttpRequestListeners.StringResponseListener? = null, method: Int = Request.Method.GET) {

        if (listener != null)
            stringListeners = listener;

        val req = object : StringRequest(method, url, stringResponseListener, errorListener) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("X-Requested-With", requestWith(context));
                headers.put("Authorization", "Bearer ${userToken(context)}");

                return headers
            }
        };

        req.tag = REQUEST_TAG;
        retryPolicy(req);

        AppClient.currentQueue?.add(req);
    }


    fun jsonObjRequest(url: String, method: Int, params: JSONObject? = null, listener: HttpRequestListeners.JsonObjectResponseListener) {

        jsonObjListeners = listener;

        val req = object : JsonObjRequest(method, url, params, jsonObjResponseListener, errorListener) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("X-Requested-With", requestWith(context));
                headers.put("Authorization", "Bearer ${userToken(context)}");

                return headers
            }
        };

        req.tag = REQUEST_TAG;
        retryPolicy(req);

        AppClient.currentQueue?.add(req);
    }

    private fun <T> retryPolicy(request: Request<T>) {
        request.retryPolicy = DefaultRetryPolicy(
                TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        );
    }

    private fun trimMessage(msg: String): String? {

        return try {
            val errorBody = JSONObject(msg);
            if (errorBody.has(ERROR))
                errorBody.getString(ERROR);
            else
                null;

        } catch (e: Exception) {
            null;
        }
    }

    private fun handleVolleyErrors(er: VolleyError): JSONObject {
        val networkResponse = er.networkResponse;
        var statusCode = 0;

        val jsonError = JSONObject();
        jsonError.put(STATUS_CODE, statusCode);

        if (networkResponse?.data != null) {
            statusCode = networkResponse.statusCode;

            var error = trimMessage(String(networkResponse.data)) ?: context.getString(R.string.request_failed);
            error = "#$statusCode $error";

            jsonError.put(STATUS_CODE, statusCode);
            jsonError.put(ERROR_MESSAGE, error);

            return jsonError;
        }

        jsonError.put(ERROR_MESSAGE, context.getString(R.string.request_failed));

        return jsonError;
    }
}