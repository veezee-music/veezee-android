package cloud.veezee.android.utils

import org.json.JSONObject

class RequestParameters {
    companion object {

        const val EMAIL: String = "email";
        const val NAME: String = "name";
        const val PASSWORD: String = "password";
        const val TOKEN: String = "token";

        fun prepareParameters(name: String? = null, email: String, password: String): JSONObject {
            val parameter = JSONObject();

            if (name != null)
                parameter.put(NAME, name);
            parameter.put(EMAIL, email);
            parameter.put(PASSWORD, password);

            return parameter;
        }
    }
}