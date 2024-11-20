package com.example.yoloapp.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.yoloapp.R
import com.example.yoloapp.utils.AuthManager
import com.example.yoloapp.utils.isValidEmail
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResetPasswordScreen(
    authManager: AuthManager,
    onPasswordResetSent: () -> Unit,
    onLoginClick: () -> Unit
) {
    val emailState = remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val imeNestedScrollConnection = rememberNestedScrollInteropConnection()


    Scaffold {


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
                    emailState.value = TextFieldValue(trimmedText, selection = newText.selection)
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
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

                    if (!isValidEmail(email)) {
                        errorMessage = "Invalid email format"
                        return@OutlinedButton
                    }

                    coroutineScope.launch {
                        val result = authManager.resetPassword(email)
                        if (result.isSuccess) {
                            onPasswordResetSent()
                            Toast.makeText(
                                context,
                                "Check your email and reset your password",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = emailState.value.text.isNotEmpty()
            ) {
                Text("Send Password Reset")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onLoginClick) {
                Text("Back to Login")
            }
        }
    }

}
