package com.liner.findme.ui.home.any_user_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.compose.AsyncImage
import com.liner.findme.R
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.ui.composable.RatingBarVotedDistance
import com.liner.findme.ui.theme.FindMeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnyUserDetailsFragment : Fragment() {

    // region private values

    private val args: AnyUserDetailsFragmentArgs by navArgs()

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
                GenericUserProfile(args.photoCard)
            }
        }

    }

    // endregion

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GenericUserProfile(photoCard: PhotoCard){
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
            if (photoCard.userProfileImage.data.isEmpty()) {
                Image(
                    modifier = imageModifier,
                    painter = painterResource(id = R.drawable.ic_user_details),
                    contentDescription = ""
                )
            } else {
                AsyncImage(
                    modifier = imageModifier,
                    model = photoCard.userProfileImage.data,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.CenterStart,
                    onError = {
                        // TODO
                    }
                )

            }

            // user scores

            RatingBarVotedDistance(
                voted_distance_mean = photoCard.votedDistanceMean,
                voted_num = photoCard.votedNum
            )
            Text(text = "(${getString(R.string.userdetails_voted_count_message1)} ${photoCard.votedNum} ${getString(
                R.string.userdetails_voted_count_message2)})")

            // end user scores

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                value = photoCard.nickname,
                label = { Text("Nickname") },
                onValueChange = {},
                enabled = false,
                readOnly = true
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                value = photoCard.username,
                label = { Text("Username") },
                onValueChange = {},
                enabled = false,
                readOnly = true
            )

        }
    }

}