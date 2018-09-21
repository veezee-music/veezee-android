package cloud.veezee.android

import cloud.veezee.android.application.App
import cloud.veezee.android.utils.SharedPreferencesHelper
import java.io.File

class Constants {
    companion object {
        const val API_BASE_URL = "https://veezee.cloud/api/v1";
        const val GUEST_MODE = true;

        var COLORED_PLAYER = true
            set(value) {
                field = value;

                SharedPreferencesHelper(App.instance).save("COLORED_PLAYER", value);
            };

        var OFFLINE_ACCESS = true
            set(value) {
                field = value;

                SharedPreferencesHelper(App.instance).save("OFFLINE_ACCESS", value);
            };

        var THEME = Theme.WHITE
            set(value) {
                field = value;

                SharedPreferencesHelper(App.instance).save("THEME", value.value);
            };

        var SETTINGS_CHANGED_NOTIFICATION_ID = "SettingsNotification";

        var DIRECTORY = ""
            get() {
                return File(App.instance.filesDir, "veezee").toString();
            };
    }
}

enum class Theme(val value: String) {
    PURPLE_DARK("PURPLE_DARK"),
    WHITE("WHITE"),
    BLACK("BLACK")
}