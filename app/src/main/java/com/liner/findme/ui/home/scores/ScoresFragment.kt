package com.liner.findme.ui.home.scores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.liner.findme.network.models.LocationScore
import com.liner.findme.ui.theme.FindMeTheme
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@AndroidEntryPoint
class ScoresFragment : Fragment() {

    // region private values

    private val args: ScoresFragmentArgs by navArgs()

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
            FindMeTheme() {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(args.photoCard.scores.sortedBy { it.distance }) { index, item ->
                        Score(score = item, index = index+1)
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }

    }

    // endregion

    // region composable

    @Composable
    private fun Score(score: LocationScore, index: Int = -1) {
        val formatter = DecimalFormat("0.00").also { df ->
            df.roundingMode = RoundingMode.CEILING
            df.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${if(index!=-1) index else ""}. ${score.nickname} (${score.username})")
            Text(text = "${formatter.format(score.distance)} km")
        }
    }

    // endregion

}