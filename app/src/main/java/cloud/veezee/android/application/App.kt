package cloud.veezee.android.application

import android.app.Application
import android.content.res.Configuration
import cloud.veezee.android.Constants
import cloud.veezee.android.Theme
import java.lang.Exception
import java.util.*

class App : Application() {

    companion object {
        var autoLoginSessionExpireDate: Long = 0
            set(value) {
                val calendar = Calendar.getInstance();
                calendar.timeInMillis = value;
                calendar.add(Calendar.MINUTE, 15);
                field = calendar.timeInMillis;
            }

        lateinit var instance: App
            private set;
    }

    override fun onCreate() {
        super.onCreate();

        instance = this;

        try {
            val mode = this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    Constants.THEME = Theme.BLACK
                }
                Configuration.UI_MODE_NIGHT_NO -> {}
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }
        } catch (e: Exception) {

        }
    }
}