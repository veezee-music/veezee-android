package cloud.veezee.android.api.utils.interfaces;

import org.json.JSONObject

class HttpRequestListeners {
    interface StringResponseListener {
        fun response(response: String?);
        fun error(er: String?, responseStatusCode: Int? = null) {};
    }
}
