package ca.gbc.comp3074.scavengerhunt.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.scavengerhunt.data.HuntRepository
import ca.gbc.comp3074.scavengerhunt.data.User
import kotlinx.coroutines.launch

// Login + signup logic. Activities just call attemptLogin/attemptSignup
// and observe the result LiveData — they never touch the DB themselves.
class AuthViewModel(private val repo: HuntRepository) : ViewModel() {

    // null = idle / consumed
    // ""   = success
    // anything else = error code the activity maps to a string resource
    val loginResult  = MutableLiveData<String?>()
    val signupResult = MutableLiveData<String?>()

    fun attemptLogin(username: String, password: String) {
        viewModelScope.launch {
            val u = repo.findUser(username.trim())
            loginResult.postValue(if (u != null && u.password == password) "" else "BAD_CREDS")
        }
    }

    fun attemptSignup(username: String, password: String) {
        viewModelScope.launch {
            val ok = repo.registerUser(User(username.trim(), password))
            signupResult.postValue(if (ok) "" else "TAKEN")
        }
    }

    // call after the activity has reacted to a result so it doesn't fire again on rotation
    fun clearLoginResult()  = loginResult.postValue(null)
    fun clearSignupResult() = signupResult.postValue(null)

    class Factory(private val repo: HuntRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(repo) as T
    }
}
