package cloud.veezee.android.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(var context: Context) {
    companion object {
        private const val NAME = "preferences";
        private const val MODE_PRIVATE = 0;
    }

    fun save(key: String, value: String): Boolean {
        return try {
            val pref: SharedPreferences.Editor? = context.getSharedPreferences(NAME, MODE_PRIVATE)?.edit();
            pref?.putString(key, value);
            pref?.apply();
            true
        } catch (e: Exception) {
            false;
        }
    }

    fun save(key: String, value: Boolean): Boolean {
        return try {
            val pref: SharedPreferences.Editor? = context.getSharedPreferences(NAME, MODE_PRIVATE)?.edit();
            pref?.putBoolean(key, value);
            pref?.apply();
            true
        } catch (e: Exception) {
            false;
        }
    }

    fun get(key: String): String? {
        val pref: SharedPreferences? = context.getSharedPreferences(NAME, MODE_PRIVATE);
        return pref?.getString(key, null);
    }

    fun getString(key: String, default: String): String {
        val pref: SharedPreferences = context.getSharedPreferences(NAME, MODE_PRIVATE) ?: return default;

        return pref.getString(key, default);
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        val pref: SharedPreferences = context.getSharedPreferences(NAME, MODE_PRIVATE) ?: return default;

        return pref.getBoolean(key, default);
    }

    fun exist(key: String): Boolean {
        val pref: SharedPreferences? = context.getSharedPreferences(NAME, MODE_PRIVATE);
        return pref?.contains(key) ?: false;
    }

    fun delete(key: String): Boolean {
        return try {
            val pref: SharedPreferences.Editor? = context.getSharedPreferences(NAME, MODE_PRIVATE)?.edit();
            pref?.remove(key);
            pref?.apply();
            true;
        } catch (e: Exception) {
            false ;
        }
    }
}