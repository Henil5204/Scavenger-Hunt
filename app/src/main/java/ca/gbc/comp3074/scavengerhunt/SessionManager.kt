package ca.gbc.comp3074.scavengerhunt

import android.content.Context

/**
 * Wraps SharedPreferences so the app remembers who's logged in across launches.
 * Could've kept it as a static var but then if Android killed the process for
 * memory you'd be logged out — pretty annoying UX.
 *
 * Stores literally one string. SharedPrefs is overkill for that, but it's the
 * "right" tool here and adding DataStore felt like overengineering for one key.
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val currentUser: String? get() = prefs.getString(KEY_USER, null)
    val isLoggedIn: Boolean   get() = currentUser != null

    fun login(username: String) { prefs.edit().putString(KEY_USER, username).apply() }
    fun logout()                 { prefs.edit().remove(KEY_USER).apply() }

    private companion object {
        const val PREFS    = "scavenger_hunt_session"
        const val KEY_USER = "current_user"
    }
}
