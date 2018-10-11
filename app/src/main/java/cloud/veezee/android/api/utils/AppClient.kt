package cloud.veezee.android.api.utils

import android.content.Context
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.utils.requestWith
import cloud.veezee.android.utils.userToken
import com.android.volley.*
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.nio.charset.Charset


class AppClient(private val context: Context?) {

    private val TIME_OUT: Int = 20000;

    companion object {
        const val DATA = "data";
        const val HEADER = "headers";
        const val ERROR = "message";

        const val STATUS_CODE = "statusCode";
        const val ERROR_MESSAGE = "message";

        private const val REQUEST_TAG: String = "volley request";

        private var currentQueue: RequestQueue? = null;

        /**
         * Cancels all HTTP requests that are the same as the last one in the client queue
         */
        fun cancelLastRequest() {
            currentQueue?.cancelAll(REQUEST_TAG);
        }
    }

    init {
        currentQueue = Volley.newRequestQueue(context);
    }

    private var stringListeners: HttpRequestListeners.StringResponseListener? = null;

    private val errorListener = Response.ErrorListener { error ->
        val errorObject = handleVolleyErrors(error);
        stringListeners?.error(errorObject[0] as String, errorObject[1] as Int);
    }

    private val stringResponseListener = Response.Listener<String> { response ->
        stringListeners?.response(response);
    }

    fun customJsonObjectRequest(url: String, method: Int, params: JSONObject? = null, listener: HttpRequestListeners.StringResponseListener?) {

        stringListeners = listener;

        val req = object : CustomJsonObjectRequest(method, url, params, stringResponseListener, errorListener) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>();
                headers.put("X-Requested-With", requestWith(context!!));
                headers.put("Authorization", "Bearer ${userToken(context!!)}");

                return headers
            }
        };

        req.tag = REQUEST_TAG;
        retryPolicy(req);

        currentQueue?.add(req);
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

    private fun handleVolleyErrors(er: VolleyError): Array<Any> {
        val networkResponse = er.networkResponse;
        val statusCode = networkResponse.statusCode;

        val data = networkResponse?.data;

        if (data != null) {
            val jsonData = JSONObject(String(networkResponse.data, Charset.defaultCharset()));

            if(jsonData.has("error")) {
                return arrayOf(jsonData.getString("error"), statusCode);
            } else {
                return arrayOf("Unknown error. Please try again.", statusCode);
            }
        } else {
            return arrayOf("System error. Please try again.", statusCode);
        }
    }
}
