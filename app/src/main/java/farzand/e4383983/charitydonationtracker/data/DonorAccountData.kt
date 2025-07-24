package farzand.e4383983.charitydonationtracker.data

import android.content.Context
import android.content.SharedPreferences

object DonorAccountData {

    private const val PREFS_NAME = "CharityDonationApp"

    private const val KEY_DONOR_LOGIN_STATUS = "DONOR_LOGIN_STATUS"
    private const val KEY_DONOR_NAME = "DONOR_NAME"
    private const val KEY_DONOR_GENDER = "DONOR_GENDER"
    private const val KEY_DONOR_EMAIL = "DONOR_EMAIL"
    private const val KEY_DONOR_PROFILE_PIC_BASE64 = "DONOR_PROFILE_PIC_BASE64"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveDonorLoginStatus(context: Context, value: Boolean) {
        val editor = getPrefs(context).edit()
        editor.putBoolean(KEY_DONOR_LOGIN_STATUS, value).apply()
    }

    fun getDonorLoginStatus(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DONOR_LOGIN_STATUS, false)
    }

    fun saveDonorName(context: Context, name: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_DONOR_NAME, name).apply()
    }

    fun getDonorName(context: Context): String? {
        return getPrefs(context).getString(KEY_DONOR_NAME, null)
    }

    fun saveDonorGender(context: Context, gender: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_DONOR_GENDER, gender).apply()
    }

    fun getDonorGender(context: Context): String? {
        return getPrefs(context).getString(KEY_DONOR_GENDER, null)
    }

    fun saveDonorEmail(context: Context, email: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_DONOR_EMAIL, email).apply()
    }

    fun getDonorEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_DONOR_EMAIL, null)
    }

    fun saveDonorProfilePictureBase64(context: Context, base64String: String?) {
        getPrefs(context).edit().putString(KEY_DONOR_PROFILE_PIC_BASE64, base64String).apply()
    }

    fun getDonorProfilePictureBase64(context: Context): String? {
        return getPrefs(context).getString(KEY_DONOR_PROFILE_PIC_BASE64, null)
    }

    fun clearDonorDetails(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}