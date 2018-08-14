package cloud.veezee.android.application

import android.app.Application
import android.util.Log
import io.fabric.sdk.android.Fabric
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import cloud.veezee.android.utils.userToken
import cloud.veezee.android.utils.Setting
import java.util.*


class App : Application() {

    companion object {
        var setting: Setting? = null;
        var offlineMode: Boolean = false;

        var autoLoginSessionExpireDate: Long = 0
            set(value) {
                val calendar = Calendar.getInstance();
                calendar.timeInMillis = value;
                calendar.add(Calendar.MINUTE, 15);
                field = calendar.timeInMillis;
            }
    }

    override fun onCreate() {
        super.onCreate();

        Log.i("Token", userToken(this));

        try {

            App.setting = Setting.getSetting(this);

            val fabric = Fabric.Builder(this)
                    .kits(Answers(), Crashlytics())
                    .build();
            Fabric.with(fabric);

        } catch (e: Exception) {

        }
    }
}