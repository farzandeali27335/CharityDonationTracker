package farzand.e4383983.charitydonationtracker.data

import android.content.Context

object UserDetails {

    private const val PREFS_NAME = "CharityDonationApp"

    fun saveUserLoginStatus(context: Context, value: Boolean) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("LOGIN_STATUS", value).apply()
    }

    fun getUserLoginStatus(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean("LOGIN_STATUS", false)
    }

    fun saveName(context: Context, name: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("USER_NAME", name).apply()
    }

    fun getName(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString("USER_NAME", null)
    }

    fun saveGender(context: Context, gender: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("USER_GENDER", gender).apply()
    }

    fun getGender(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString("USER_GENDER", null)
    }


    fun saveEmail(context: Context, email: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("USER_EMAIL", email).apply()
    }

    fun getEmail(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString("USER_EMAIL", null)
    }
}