package cloud.veezee.android.utils

import android.content.Context
import com.google.gson.Gson
import java.io.File

class Setting {

    enum class Theme {
        PURPLE_DARK,
        WHITE,
        BLACK
    }

    var theme: Theme? = Theme.WHITE;
    var directory: String = "";
    var offlineAccess: Boolean = true;
    var coloredPlayer: Boolean = true;

    companion object {
        const val NAME = "setting";
        const val SETTING_NOTIFICATION = "SettingNotification";
        fun getSetting(context: Context?): Setting {

            return if (SharedPreferencesHelper.exist(context, NAME)) {
                Gson().fromJson(SharedPreferencesHelper.get(context, Setting.NAME), Setting::class.java);
            } else {
                val setting = Setting();
                setting.directory = File(context?.filesDir, "veezee").toString();

                setting;
            }
        }

        fun resetSetting(context: Context?): Boolean {
            val b = SharedPreferencesHelper.delete(context, Setting.NAME);

            return b;
        }
    }

    fun save(context: Context?): Boolean {
        val set: Boolean = SharedPreferencesHelper.save(context, Setting.NAME, settingToJson(this));

        return set;
    };

    private fun settingToJson(setting: Setting?): String = Gson().toJson(setting);
}