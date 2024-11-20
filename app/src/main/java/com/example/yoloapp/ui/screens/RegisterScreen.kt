package com.example.yoloapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.yoloapp.R
import com.example.yoloapp.utils.AuthManager
import com.example.yoloapp.utils.isValidEmail
import com.example.yoloapp.utils.isValidPassword
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    authManager: AuthManager,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    val emailState = remember { mutableStateOf(TextFieldValue("")) }
    val passwordState = remember { mutableStateOf(TextFieldValue("")) }
    val confirmPasswordState = remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoadingScreen by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val imeNestedScrollConnection = rememberNestedScrollInteropConnection()

    Scaffold {


        if (!isLoadingScreen) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .nestedScroll(imeNestedScrollConnection)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.img_logo),
                    contentDescription = null,
                    modifier = Modifier.size(84.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(id = R.string.app_name),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Thin
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = emailState.value,
                    onValueChange = { newText ->
                        val trimmedText = newText.text.trim()
                        emailState.value =
                            TextFieldValue(trimmedText, selection = newText.selection)
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = passwordState.value,
                    onValueChange = { newText ->
                        val trimmedText = newText.text.trim()
                        passwordState.value =
                            TextFieldValue(trimmedText, selection = newText.selection)
                    },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPasswordState.value,
                    onValueChange = { newText ->
                        val trimmedText = newText.text.trim()
                        confirmPasswordState.value =
                            TextFieldValue(trimmedText, selection = newText.selection)
                    },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let {
                    Text(text = it, color = Color.Red)
                }

                OutlinedButton(
                    onClick = {
                        val email = emailState.value.text
                        val password = passwordState.value.text
                        val confirmPassword = confirmPasswordState.value.text

                        if (!isValidEmail(email)) {
                            errorMessage = "Invalid email format"
                            return@OutlinedButton
                        }

                        if (!isValidPassword(password)) {
                            errorMessage = "Password must be at least 6 characters"
                            return@OutlinedButton
                        }

                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@OutlinedButton
                        }

                        coroutineScope.launch {
                            isLoadingScreen = true
                            val result = authManager.registerWithEmail(email, password)
                            if (result.isSuccess) {
                                onRegisterSuccess()
                                delay(1000)
                                isLoadingScreen = false
                            } else {
                                errorMessage = result.exceptionOrNull()?.message
                                delay(1000)
                                isLoadingScreen = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = emailState.value.text.isNotEmpty() && passwordState.value.text.isNotEmpty() && confirmPasswordState.value.text.isNotEmpty()
                ) {
                    Text("Register")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onLoginClick) {
                    Text("Already have an account? Login")
                }
            }
        } else {
            LoadingScreen()
        }
    }
}
