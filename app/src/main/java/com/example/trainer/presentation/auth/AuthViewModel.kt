package com.example.trainer.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainer.domain.model.User
import com.example.trainer.domain.repository.AuthRepository
import com.example.trainer.domain.usecase.*
import com.example.trainer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/** Drives Firebase authentication and profile synchronization. */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val repository: AuthRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow<UiState<User>>(UiState.Empty)
    val state = mutable.asStateFlow()
    val signedIn get() = repository.isSignedIn()
    private var job: Job? = null
    fun login(email: String, password: String) = submit { loginUseCase(email, password) }
    fun register(name: String, email: String, password: String) = submit { registerUseCase(name, email, password) }
    fun reset() { job?.cancel(); mutable.value = UiState.Empty }
    private fun submit(action: suspend () -> User) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            mutable.value = UiState.Loading
            try { mutable.value = UiState.Success(action()) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.value = UiState.Error(e.userMessage()) }
        }
    }
}

