package cloud.veezee.android.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import cloud.veezee.android.R

class LogoutRequireActivity : AppCompatActivity() {

    private val context: Context = this;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout_require);

        //window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    fun logout(view: View) {

        val loginActivity = Intent(context, LoginActivity::class.java);
        loginActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
        startActivity(loginActivity);

        finish();
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            System.exit(0);
        }

        return super.onKeyDown(keyCode, event)
    }
}
