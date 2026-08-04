package com.example.ui.screens.auth

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ChronovaApplication
import com.example.data.auth.AuthRepository
import com.example.ui.components.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhoneAuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()
    
    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    fun sendOtp(phoneNumber: String, activity: Activity) {
        if (phoneNumber.isBlank()) {
            _errorMessage.value = "Please enter a valid phone number."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        
        authRepository.startPhoneVerification(
            phoneNumber = phoneNumber,
            activity = activity,
            onCodeSent = { id ->
                _isLoading.value = false
                _verificationId.value = id
                startCountdown()
            },
            onError = { error ->
                _isLoading.value = false
                _errorMessage.value = error
            }
        )
    }

    fun verifyOtp(code: String, onSuccess: () -> Unit) {
        if (code.length != 6) {
            _errorMessage.value = "Code must be 6 digits."
            return
        }
        val vId = _verificationId.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.verifyPhoneCode(vId, code)
            _isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    private fun startCountdown() {
        _countdown.value = 60
        viewModelScope.launch {
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value -= 1
            }
        }
    }

    fun reset() {
        _verificationId.value = null
        _errorMessage.value = null
        _countdown.value = 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: PhoneAuthViewModel = viewModel(
        factory = viewModelFactory { PhoneAuthViewModel(appContainer.authRepository) }
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val verificationId by viewModel.verificationId.collectAsState()
    val countdown by viewModel.countdown.collectAsState()

    var countryCode by remember { mutableStateOf("+91") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone Verification") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (verificationId == null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { countryCode = it },
                        label = { Text("Code") },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        val fullNumber = countryCode + phoneNumber
                        viewModel.sendOtp(fullNumber, activity)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Send OTP")
                    }
                }
            } else {
                Text(
                    text = "Enter the 6-digit code sent to $countryCode$phoneNumber",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text("6-digit OTP") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = { viewModel.verifyOtp(otpCode, onAuthSuccess) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading && otpCode.length == 6
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Verify OTP")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val fullNumber = countryCode + phoneNumber
                            viewModel.sendOtp(fullNumber, activity)
                        },
                        enabled = countdown == 0 && !isLoading
                    ) {
                        Text(if (countdown > 0) "Resend in ${countdown}s" else "Resend OTP")
                    }

                    TextButton(
                        onClick = {
                            viewModel.reset()
                            otpCode = ""
                        },
                        enabled = !isLoading
                    ) {
                        Text("Change number")
                    }
                }
            }
        }
    }
}
