package ca.gbc.comp3074.scavengerhunt

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ca.gbc.comp3074.scavengerhunt.databinding.ActivityMainBinding
import ca.gbc.comp3074.scavengerhunt.viewmodel.AuthViewModel

/**
 * Login. Also short-circuits straight to the list if there's an existing
 * session, so returning users don't see the form at all.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

    private val authVm: AuthViewModel by viewModels { AuthViewModel.Factory(repo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        session = SessionManager(this)

        // already logged in? skip the form, go straight home.
        // finish() so back button doesn't bounce them back here.
        if (session.isLoggedIn) {
            startActivity(Intent(this, HuntListActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // pad for the notch / nav bar so nothing gets clipped on edge-to-edge displays
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.loginButton.setOnClickListener { tryLogin() }
        binding.signupText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // observe the auth result
        authVm.loginResult.observe(this) { result ->
            when (result) {
                null -> { /* idle / already consumed */ }
                ""   -> onLoginSuccess()
                else -> toast(R.string.error_invalid_login)
            }
            if (result != null) authVm.clearLoginResult()
        }
    }

    private fun tryLogin() {
        val u = binding.username.text?.toString().orEmpty()
        val p = binding.password.text?.toString().orEmpty()
        if (u.isBlank() || p.isBlank()) {
            toast(R.string.error_empty_fields); return
        }
        authVm.attemptLogin(u, p)
    }

    private fun onLoginSuccess() {
        val u = binding.username.text?.toString()?.trim().orEmpty()
        session.login(u)
        toast(getString(R.string.welcome_back, u))
        startActivity(Intent(this, HuntListActivity::class.java))
        finish()
    }
}
