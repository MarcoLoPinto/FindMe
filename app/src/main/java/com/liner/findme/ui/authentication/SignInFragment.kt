package com.liner.findme.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.liner.findme.R
import com.liner.findme.ui.MainActivity
import com.liner.findme.ui.composable.FindMeLottieAnimation
import com.liner.findme.ui.home.HomeViewModel
import com.liner.findme.ui.home.HomeViewModelImpl
import com.liner.findme.ui.theme.FindMeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    // region private properties

    private val viewModel: AuthenticationViewModel by viewModels<AuthenticationViewModelImpl>()

    @Inject
    lateinit var signInClient: GoogleSignInClient

    private var shouldShowUsernameAndNickName: MutableState<Boolean> = mutableStateOf(false)

    private var username: MutableState<String> = mutableStateOf("")
    private var nickname: MutableState<String> = mutableStateOf("")

    private val requestGoogleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            Log.d("SignInFragment", "ActivityResult received: $result")
            Log.d("SignInFragment", "Result code: ${result.resultCode}")
            Log.d("SignInFragment", "Intent data: ${result.data?.extras?.toString()}")

            try {

                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)

                Log.d("SignInFragment", "GoogleSignInAccount task: $task")

                lifecycleScope.launch {
                    val username = username.value
                    val nickname = nickname.value
                    viewModel.signIn(task, username = username, nickname = nickname)
                        .flowWithLifecycle(lifecycle)
                        .collectLatest { authenticationResult ->
                            authenticationResult
                                .onSuccess { result -> // already registered, go to home

                                    if (result) {
                                        startActivity(
                                            Intent(
                                                requireContext(),
                                                MainActivity::class.java
                                            )
                                        )
                                    }
                                    else { // 302 = register!
                                        shouldShowUsernameAndNickName.value = true
                                    }

                                }
                                .onFailure { t ->

                                    t.printStackTrace()

                                    MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(R.string.signin_fragment_authentication_error_title)
                                        .setMessage(R.string.signin_fragment_authentication_error_message) // t.localizedMessage)
                                        .setNeutralButton(R.string.signin_fragment_authentication_error_neutral_button) { dialog, _ ->
                                            dialog.dismiss()
                                            signInClient.signOut()
                                        }.show()

                                }
                        }
                }


            } catch (ex: Exception) {
                Log.e("SignInFragment", "Exception in sign-in flow", ex)
                ex.printStackTrace()
                signInClient.signOut()
            }

        }

    // endregion

    // region lifecycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext())

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view as ComposeView).setContent {

            FindMeTheme() {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(
                        20.dp,
                        alignment = Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val showUsernameAndNickName by rememberSaveable { shouldShowUsernameAndNickName }

                    if (!showUsernameAndNickName) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = getString(R.string.signin_fragment_welcome),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )
                    }

                    FindMeLottieAnimation(
                        R.raw.world,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .wrapContentSize(Alignment.Center)
                    )

                    if (showUsernameAndNickName) {

                        val maxCharUsername = 10
                        val maxCharNickname = 10

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = username.value,
                            label = { Text(text = "username") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            onValueChange = { username ->
                                if (username.length <= maxCharUsername) this@SignInFragment.username.value = username
                            })
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = nickname.value,
                            label = { Text(text = "nickname") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            onValueChange = { nickname ->
                                if (nickname.length <= maxCharNickname) this@SignInFragment.nickname.value = nickname
                            })
                    }
                    Button(modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                        onClick = {

                            val intent = signInClient.signInIntent

                            requestGoogleSignInLauncher.launch(intent)

                        }
                    ) {
                        Text(text = if (showUsernameAndNickName) "Register" else "Login")
                    }


                }
            }



        }

    }

    // endregion

}