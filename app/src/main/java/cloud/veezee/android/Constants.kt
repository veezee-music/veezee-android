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
            const val USER_TRACKS_HISTORY = "/account/vex/user-tracks-history";
            const val UPDATE_NAME_AND_PASSWORD = "/account/update-name-and-password";
        }
    }
}

enum class Theme(val value: String) {
    PURPLE_DARK("PURPLE_DARK"),
    WHITE("WHITE"),
    BLACK("BLACK")
}