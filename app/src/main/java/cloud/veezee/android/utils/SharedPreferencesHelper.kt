package cloud.veezee.android.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper {

    companion object {
        private const val NAME = "preferences";
        private const val MODE_PRIVATE = 0;

        fun save(context: Context?, key: String, value: String?): Boolean {
            return try {
                val pref: SharedPreferences.Editor? = context?.getSharedPreferences(NAME, MODE_PRIVATE)?.edit();
                pref?.putString(key, value);
                pref?.apply();
                true
            }catch (e: Exception) {
                false;
            }
        }

        fun get(context: Context?, key: String): String? {
            val pref: SharedPreferences? = context?.getSharedPreferences(NAME, MODE_PRIVATE);
            return pref?.getString(key, null);
        }

        fun exist(context: Context?, key: String): Boolean {
            val pref: SharedPreferences? = context?.getSharedPreferences(NAME, MODE_PRIVATE);
            return pref?.contains(key) ?: false;
        }

        fun delete(context: Context?, key: String?): Boolean {
            return try {
                val pref: SharedPreferences.Editor? = context?.getSharedPreferences(NAME, MODE_PRIVATE)?.edit();
                pref?.remove(key);
                pref?.apply();
                true;
            } catch (e: Exception) {
                false ;
            }
        }
    }
}