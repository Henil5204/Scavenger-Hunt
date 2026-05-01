package ca.gbc.comp3074.scavengerhunt

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Little file of one-liners I kept finding myself rewriting in every activity.
// Nothing clever — just convenience.

/** Toast.makeText(this, msg, SHORT).show() but shorter. */
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Context.toast(resId: Int)  = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

/** Quick visibility toggle so I can write `view.showIf(list.isEmpty())` in the activity. */
fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/**
 * Pulls out the singleton repository from the Application.
 * Saves me writing `(application as ScavengerHuntApp).repository` ten different times.
 */
val AppCompatActivity.repo
    get() = (application as ScavengerHuntApp).repository
