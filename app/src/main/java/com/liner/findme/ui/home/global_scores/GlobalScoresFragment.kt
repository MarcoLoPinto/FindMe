package com.liner.findme.ui.home.global_scores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.compose.AsyncImage
import com.liner.findme.network.models.User
import com.liner.findme.ui.theme.FindMeTheme
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@AndroidEntryPoint
class GlobalScoresFragment : Fragment() {

    // region private properties

    private val viewModel: GlobalScoresViewModel by viewModels<GlobalScoresViewModelImpl>()

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
                val users by viewModel.getGlobalScores().collectAsState(initial = null)
                if(users == null) {
                    Box{
                        CircularProgressIndicator(modifier = Modifier
                            .align(Alignment.Center)
                            .size(60.dp))
                    }

                }
                else GlobalScoresComposable(users!!)
            }
        }

    }

    // endregion

    // region composable

    @Composable
    private fun GlobalScoresComposable(users: List<User>){
        val formatter = DecimalFormat("0.00").also { df ->
            df.roundingMode = RoundingMode.CEILING
            df.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
        }
        LazyColumn() {
            itemsIndexed(users.sortedBy { it.votedDistanceMean }) { i, user ->
                UserScoreComposable(user, formatter, i+1)
                Divider(color = Color.Gray, thickness = 1.dp)
            }
        }
    }

    @Composable
    private fun UserScoreComposable(user: User, formatter: DecimalFormat, index: Int = -1){
        val imageSize = 60.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageSize)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row (
                modifier = Modifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    modifier = Modifier
                        .size(imageSize)
                        .clip(CircleShape),
                    model = user.userProfileImage.data,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.CenterStart,
                    onError = {
                        // TODO
                    }
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "${if (index != -1) index.toString().plus(".") else ""}"
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "${user.nickname}\n(${user.username})"
                )
            }
            val maxDistance = 500f
            if (user.votedDistanceMean > maxDistance) Text(text = ">${maxDistance} km")
            else Text(text = "${formatter.format(user.votedDistanceMean)} km")
        }
    }

    // endregion
}