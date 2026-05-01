package ca.gbc.comp3074.scavengerhunt

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ca.gbc.comp3074.scavengerhunt.databinding.ActivitySignUpBinding
import ca.gbc.comp3074.scavengerhunt.viewmodel.AuthViewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var session: SessionManager

    private val authVm: AuthViewModel by viewModels { AuthViewModel.Factory(repo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        binding.signUpButton.setOnClickListener { trySignup() }

        // back-to-login is just finish() — avoids piling up activities on the back stack
        binding.loginLink.setOnClickListener { finish() }

        authVm.signupResult.observe(this) { result ->
            when (result) {
                null -> { /* idle */ }
                ""   -> onSignupSuccess()
                else -> toast(R.string.error_username_taken)
            }
            if (result != null) authVm.clearSignupResult()
        }
    }

    private fun trySignup() {
        val u = binding.username.text?.toString()?.trim().orEmpty()
        val p = binding.password.text?.toString().orEmpty()
        val c = binding.confirmPassword.text?.toString().orEmpty()

        when {
            u.isBlank() || p.isBlank() || c.isBlank() -> toast(R.string.error_empty_fields)
            p != c                                     -> toast(R.string.error_password_mismatch)
            p.length < 4                               -> toast(R.string.error_password_short)
            else                                       -> authVm.attemptSignup(u, p)
        }
    }

    private fun onSignupSuccess() {
        // auto-log them in — feels nicer than making them retype the same creds
        val u = binding.username.text?.toString()?.trim().orEmpty()
        session.login(u)
        toast(R.string.account_created)
        val i = Intent(this, HuntListActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i); finish()
    }
}
