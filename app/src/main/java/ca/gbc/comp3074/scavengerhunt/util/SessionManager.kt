package ca.gbc.comp3074.scavengerhunt.util

import android.content.Context

/**
 * Tiny wrapper around SharedPreferences for "is somebody logged in right now?".
 *
 * SharedPreferences is overkill-but-fine for one string. Using it instead of
 * just keeping the username in memory because:
 *   - it survives the app being killed by the OS
 *   - it lets us auto-skip the login screen on next launch
 *
 * If we ever needed to store anything sensitive, we'd switch to EncryptedSharedPreferences.
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Username of whoever is logged in, or null if nobody is. */
    val currentUser: String?
        get() = prefs.getString(KEY_USER, null)

    val isLoggedIn: Boolean
        get() = currentUser != null

    fun login(username: String) {
        prefs.edit().putString(KEY_USER, username).apply()
    }

    fun logout() {
        prefs.edit().remove(KEY_USER).apply()
    }

    companion object {
        private const val PREFS = "scavenger_hunt_session"
        private const val KEY_USER = "current_user"
    }
}
