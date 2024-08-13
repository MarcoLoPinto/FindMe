package com.liner.findme.ui.home.user_details

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import coil.compose.AsyncImage
import com.liner.findme.R
import com.liner.findme.extensions.toByteArray
import com.liner.findme.network.models.PhotoData
import com.liner.findme.network.models.User
import com.liner.findme.ui.AuthenticationActivity
import com.liner.findme.ui.authentication.AuthenticationViewModel
import com.liner.findme.ui.authentication.AuthenticationViewModelImpl
import com.liner.findme.ui.composable.RatingBarVotedDistance
import com.liner.findme.ui.theme.FindMeTheme
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailsFragment : Fragment() {

    // region private properties

    private val viewModel: UserDetailsViewModel by viewModels<UserDetailsViewModelImpl>()

    private var user: MutableState<User?> = mutableStateOf(null)

    private val constraints by lazy {
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresStorageNotLow(false)
            .build()
    }

    // endregion

    // region lifecycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view as ComposeView).setContent {
            FindMeTheme {
                user.value = viewModel.userDetails().collectAsState(initial = null).value

                user.value?.let {
                    UserProfile(it)
                } ?: Box {
                    CircularProgressIndicator(modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp))
                }
            }

        }

    }

    // endregion

    // region composable

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UserProfile(user: User) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageModifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .clip(CircleShape)
            val buttonModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp)
            if (user.userProfileImage.data.isEmpty()) {
                Image(
                    modifier = imageModifier,
                    painter = painterResource(id = R.drawable.ic_user_details),
                    contentDescription = ""
                )
            } else {
                AsyncImage(
                    modifier = imageModifier,
                    model = user.userProfileImage.data,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.CenterStart,
                    onError = {
                        // TODO
                    }
                )

                val context = LocalContext.current
                val launcher =
                    rememberLauncherForActivityResult(PickSinglePhotoContract()) { imageUri ->
                        imageUri?.let {
                            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            lifecycleScope.launch {
                                viewModel.updateUserProfile(it).collectLatest { updatedUser ->
                                    this@UserDetailsFragment.user.value = updatedUser
                                }
                            }
                        }
                    }
                
                Button(onClick = {
                    launcher.launch()
                }) {
                    Text(text = getString(R.string.userdetails_button_change_image))
                }
            }

            // user scores

            RatingBarVotedDistance(
                voted_distance_mean = user.votedDistanceMean,
                voted_num = user.votedNum
            )
            Text(text = "(${getString(R.string.userdetails_voted_count_message1)} ${user.votedNum} ${getString(R.string.userdetails_voted_count_message2)})")

            // end user scores

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                value = user.nickname,
                label = { Text("Nickname") },
                onValueChange = {},
                enabled = false,
                readOnly = true
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                value = user.username,
                label = { Text("Username") },
                onValueChange = {},
                enabled = false,
                readOnly = true
            )

            Button(
                modifier = buttonModifier,
                onClick = {
                lifecycleScope.launch {
                    viewModel.signOut().flowWithLifecycle(lifecycle).collectLatest { _ ->
                        startActivity(Intent(requireContext(), AuthenticationActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }) {
                Text(text = "Logout")
            }

        }
    }

    // endregion

    // region private functions



    // endregion

}