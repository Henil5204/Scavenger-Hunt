package ca.gbc.comp3074.scavengerhunt

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ca.gbc.comp3074.scavengerhunt.data.Hunt
import ca.gbc.comp3074.scavengerhunt.databinding.ActivityAddEditHuntBinding
import ca.gbc.comp3074.scavengerhunt.viewmodel.HuntViewModel

/**
 * One screen, two modes:
 *   - create: no extras passed -> blank form, INSERT on save
 *   - edit:   EXTRA_HUNT_ID passed -> form pre-filled, UPDATE on save
 *
 * Mode is decided by checking if `editingId == -1L`.
 */
class AddEditHuntActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditHuntBinding

    private val huntVm: HuntViewModel by viewModels { HuntViewModel.Factory(repo) }

    private var editingId: Long = -1L

    // hold onto the existing `completed` flag so editing doesn't reset it
    private var existingCompleted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditHuntBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editingId = intent.getLongExtra(EXTRA_HUNT_ID, -1L)
        supportActionBar?.setTitle(if (editingId == -1L) R.string.new_hunt else R.string.edit_hunt)

        if (editingId != -1L) {
            huntVm.loadHunt(editingId)
            huntVm.selectedHunt.observe(this) { it?.let(::fillForm) }
        }

        binding.btnSave.setOnClickListener { onSavePressed() }
    }

    private fun fillForm(hunt: Hunt) {
        binding.name.setText(hunt.name)
        binding.tag.setText(hunt.tag)
        binding.address.setText(hunt.address)
        binding.city.setText(hunt.city)
        binding.description.setText(hunt.description)
        binding.rating.rating = hunt.rating
        existingCompleted = hunt.completed
    }

    private fun onSavePressed() {
        val name = binding.name.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) { toast(R.string.error_name_required); return }

        val hunt = Hunt(
            // id == 0L is Room's "I'm new, generate me an id" sentinel
            id          = if (editingId == -1L) 0L else editingId,
            name        = name,
            tag         = binding.tag.text?.toString()?.trim().orEmpty(),
            address     = binding.address.text?.toString()?.trim().orEmpty(),
            city        = binding.city.text?.toString()?.trim().orEmpty(),
            description = binding.description.text?.toString()?.trim().orEmpty(),
            rating      = binding.rating.rating,
            completed   = existingCompleted
        )
        huntVm.saveHunt(hunt)
        toast(R.string.hunt_saved)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    companion object { const val EXTRA_HUNT_ID = "hunt_id" }
}
