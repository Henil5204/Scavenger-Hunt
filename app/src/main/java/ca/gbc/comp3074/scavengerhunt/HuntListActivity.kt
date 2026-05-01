package ca.gbc.comp3074.scavengerhunt

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ca.gbc.comp3074.scavengerhunt.data.Hunt
import ca.gbc.comp3074.scavengerhunt.data.HuntSort
import ca.gbc.comp3074.scavengerhunt.databinding.ActivityHuntListBinding
import ca.gbc.comp3074.scavengerhunt.viewmodel.HuntViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * Home screen — the hub of the app.
 *
 * What's going on:
 *   - toolbar with sort + about + logout
 *   - search bar that filters as you type (no debounce because the dataset is tiny)
 *   - RecyclerView of hunt rows
 *       tap          -> detail
 *       long-press   -> delete with snackbar undo
 *   - "+" coral FAB -> add new hunt
 *   - centred empty-state when there's nothing to show
 */
class HuntListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHuntListBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: HuntAdapter

    private val huntVm: HuntViewModel by viewModels { HuntViewModel.Factory(repo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHuntListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        // small touch — show whoever's logged in as the toolbar title
        supportActionBar?.title = session.currentUser ?: getString(R.string.my_hunts)

        setupRecycler()
        setupSearch()

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditHuntActivity::class.java))
        }
    }

    private fun setupRecycler() {
        adapter = HuntAdapter(
            onClick     = { hunt -> openDetail(hunt) },
            onLongPress = { hunt -> deleteWithUndo(hunt) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        huntVm.hunts.observe(this) { list ->
            adapter.submitList(list)
            binding.empty.showIf(list.isEmpty())
        }
    }

    private fun setupSearch() {
        // pushing every keystroke into the VM. switchMap there reactively re-queries.
        // for a real app I'd add a 200ms debounce so we're not pounding sqlite,
        // but with a handful of rows it really doesn't matter.
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                huntVm.setSearch(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openDetail(hunt: Hunt) {
        val i = Intent(this, HuntDetailActivity::class.java)
            .putExtra(HuntDetailActivity.EXTRA_HUNT_ID, hunt.id)
        startActivity(i)
    }

    private fun deleteWithUndo(hunt: Hunt) {
        huntVm.deleteHunt(hunt)
        Snackbar.make(binding.root, getString(R.string.deleted, hunt.name), Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) { huntVm.restore(hunt) }
            .show()
    }

    // ---- toolbar menu ----

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_hunt_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_sort   -> { showSortDialog(); true }
        R.id.action_about  -> { startActivity(Intent(this, AboutActivity::class.java)); true }
        R.id.action_logout -> { confirmLogout(); true }
        else               -> super.onOptionsItemSelected(item)
    }

    private fun showSortDialog() {
        val labels = arrayOf(
            getString(R.string.sort_newest),
            getString(R.string.sort_name),
            getString(R.string.sort_rating)
        )
        val checked = when (huntVm.currentSort()) {
            HuntSort.NEWEST -> 0
            HuntSort.NAME   -> 1
            HuntSort.RATING -> 2
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.sort_by)
            .setSingleChoiceItems(labels, checked) { dlg, which ->
                huntVm.setSort(when (which) {
                    0    -> HuntSort.NEWEST
                    1    -> HuntSort.NAME
                    else -> HuntSort.RATING
                })
                dlg.dismiss()
            }
            .show()
    }

    private fun confirmLogout() {
        // small "are you sure" gate — pretty annoying to log out by accident
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_confirm_title)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.logout) { _, _ ->
                session.logout()
                val i = Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(i); finish()
            }
            .show()
    }
}
