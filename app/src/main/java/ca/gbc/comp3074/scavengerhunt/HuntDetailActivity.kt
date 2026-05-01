package ca.gbc.comp3074.scavengerhunt

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ca.gbc.comp3074.scavengerhunt.data.Hunt
import ca.gbc.comp3074.scavengerhunt.databinding.ActivityHuntDetailBinding
import ca.gbc.comp3074.scavengerhunt.viewmodel.HuntViewModel

/**
 * Detail screen for one hunt.
 *
 * We get only the hunt id from the intent, then re-load it from Room here.
 * Doing it this way means:
 *   1. Hunt doesn't have to be Parcelable
 *   2. The screen always shows fresh data after an edit
 */
class HuntDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHuntDetailBinding

    private val huntVm: HuntViewModel by viewModels { HuntViewModel.Factory(repo) }

    // currently-loaded hunt. nullable because it might still be loading,
    // or could have been deleted from another screen.
    private var current: Hunt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHuntDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getLongExtra(EXTRA_HUNT_ID, -1L)
        if (id == -1L) { finish(); return }   // shouldn't happen but fail safe

        huntVm.loadHunt(id)
        huntVm.selectedHunt.observe(this) { hunt ->
            if (hunt == null) { finish(); return@observe }   // got deleted elsewhere
            current = hunt
            paint(hunt)
        }

        binding.btnShare.setOnClickListener      { current?.let(::shareHunt) }
        binding.btnDirections.setOnClickListener { current?.let(::openInMaps) }
        binding.btnComplete.setOnClickListener   { current?.let { huntVm.toggleCompleted(it) } }
    }

    private fun paint(hunt: Hunt) {
        supportActionBar?.title = hunt.name
        binding.huntName.text        = hunt.name
        binding.huntTag.text         = hunt.tag.ifBlank { "untagged" }.uppercase()
        binding.huntAddress.text     = hunt.address.ifBlank { "—" }
        binding.huntCity.text        = hunt.city.ifBlank { "—" }
        binding.huntDescription.text = hunt.description.ifBlank { "(no description)" }
        binding.huntRating.rating    = hunt.rating

        binding.completedBadge.showIf(hunt.completed)
        binding.btnComplete.setText(
            if (hunt.completed) R.string.mark_incomplete else R.string.mark_complete
        )

        // directions makes no sense without an address — disable the button
        binding.btnDirections.isEnabled = hunt.address.isNotBlank() || hunt.city.isNotBlank()
    }

    // -------------------- share --------------------

    private fun shareHunt(hunt: Hunt) {
        val body = getString(
            R.string.share_template,
            hunt.name, hunt.tag, hunt.address, hunt.city, hunt.description
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            putExtra(Intent.EXTRA_TEXT, body)
        }
        // createChooser forces the picker every time. without it Android remembers
        // your last pick and never asks again, which is annoying for sharing.
        startActivity(Intent.createChooser(send, getString(R.string.share)))
    }

    // -------------------- directions --------------------

    /**
     * Open the hunt's address in a maps app.
     *
     * Why this is more involved than it looks:
     *
     *   1. Android 11 (API 30) introduced "package visibility" — by default an app
     *      can't see what OTHER apps are installed. That meant our previous
     *      resolveActivity() check returned null even when Google Maps WAS
     *      installed, so the button silently did nothing. We now declare a
     *      <queries> block in the manifest so Android lets us see geo:-handlers.
     *   2. Even with that, some emulators / minimal devices have NO maps app.
     *      So if the geo: intent throws, we fall back to opening the Google Maps
     *      URL in a browser — which is basically guaranteed to work.
     *   3. If even THAT fails (no browser either), we show a toast instead of
     *      pretending nothing happened.
     */
    private fun openInMaps(hunt: Hunt) {
        val query = listOf(hunt.address, hunt.city)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        if (query.isBlank()) { toast(R.string.error_no_address); return }

        val encoded = Uri.encode(query)

        // try #1: native geo: intent. Google Maps, Waze, Maps.me etc all handle it.
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded"))
        try {
            startActivity(geo); return
        } catch (e: ActivityNotFoundException) {
            // fall through to web
        }

        // try #2: web URL via Google Maps. handled by literally any browser.
        val web = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")
        )
        try {
            startActivity(web)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.error_no_maps_app)
        }
    }

    // -------------------- toolbar --------------------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_hunt_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_edit -> {
            current?.let {
                val i = Intent(this, AddEditHuntActivity::class.java)
                    .putExtra(AddEditHuntActivity.EXTRA_HUNT_ID, it.id)
                startActivity(i)
            }; true
        }
        R.id.action_delete -> { confirmDelete(); true }
        android.R.id.home  -> { onBackPressedDispatcher.onBackPressed(); true }
        else               -> super.onOptionsItemSelected(item)
    }

    private fun confirmDelete() {
        val hunt = current ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                huntVm.deleteHunt(hunt); finish()
            }
            .show()
    }

    // re-load on resume so the screen is fresh after coming back from edit
    override fun onResume() {
        super.onResume()
        val id = intent.getLongExtra(EXTRA_HUNT_ID, -1L)
        if (id != -1L) huntVm.loadHunt(id)
    }

    companion object { const val EXTRA_HUNT_ID = "hunt_id" }
}
