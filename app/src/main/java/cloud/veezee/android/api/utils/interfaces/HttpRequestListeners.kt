package cloud.veezee.android.api.utils.interfaces

import org.json.JSONObject

class HttpRequestListeners {

    interface JsonObjectResponseListener {

        fun response(response: JSONObject);

        fun headers(json: JSONObject);

        fun error(er: JSONObject);
    }

    interface StringResponseListener {

        fun response(response: String?);

        fun headers(json: JSONObject);

        fun error(error: JSONObject?)
    }
}
