package ca.gbc.comp3074.scavengerhunt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.gbc.comp3074.scavengerhunt.databinding.ActivityAboutBinding

// Static "About" page. Reachable from the toolbar overflow on the hunt list.
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.about_title)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
