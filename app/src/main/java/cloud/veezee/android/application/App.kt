package cloud.veezee.android.application

import android.app.Application
import io.fabric.sdk.android.Fabric
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
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
            val fabric = Fabric.Builder(this)
                    .kits(Answers(), Crashlytics())
                    .build();
            Fabric.with(fabric);

        } catch (e: Exception) {

        }
    }
}