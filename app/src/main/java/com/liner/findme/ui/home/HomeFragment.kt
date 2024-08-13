package com.liner.findme.ui.home

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import com.liner.findme.R
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.ui.theme.FindMeTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    // region private properties

    private val viewModel: HomeViewModel by viewModels<HomeViewModelImpl>()

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted.values.any { !it }) {

            }
        }

    private val menuProvider = object : MenuProvider {
        lateinit var selectedFilterUri: String
        var selectedFilterTitle: MutableState<String> = mutableStateOf("")

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_actionbar_home, menu)
            selectedFilterUri = getString(R.string.homefragment_explore_key)
            selectedFilterTitle.value = getString(R.string.homefragment_explore)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            val filter = when (menuItem.itemId) {
                R.id.filter_explore -> getString(R.string.homefragment_explore_key)
                R.id.filter_mine -> getString(R.string.homefragment_mine_key)
                R.id.filter_voted -> getString(R.string.homefragment_voted_key)
                else -> null
            }
            filter?.let {
                selectedFilterTitle.value = menuItem.title as String
                selectedFilterUri = it
                viewModel.getPhotoCards(it)
            }
            return true
        }
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

        viewModel.getPhotoCards()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.CREATED)

        (view as ComposeView).setContent {
            FindMeTheme() {
                Home()
            }
        }

    }

    // endregion

    // region composable

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun Home() {
        val photoCards by viewModel.photoCards.collectAsState()
        val loadingState by viewModel.loadingState.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.background))
        ) {

            if(loadingState == HomeViewModel.STATE_START){

                AppBarFilter()
                CircularProgressIndicator(modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp))

            } else if (photoCards.isEmpty()) {

                AppBarFilter()

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .wrapContentSize(Alignment.Center),
                    text = getString(R.string.homefragment_message_empty),
                    fontSize = 28.sp,
                    color = Color(0.6f, 0.6f, 0.6f, 0.4f),
                    textAlign = TextAlign.Center
                )

            } else {

                LazyColumn(
                    modifier = Modifier
                        .background(color = Color(243, 243, 243, 255))
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(photoCards) { index, photoCard ->
                        if (index == 0) {
                            AppBarFilter()
                        }
                        PhotoCard(photoCard)
                    }
                }

            }

        }

    }

    @Composable
    private fun PhotoCard(photoCard: PhotoCard) {
        ElevatedCard(
            modifier = Modifier
                // .rotate((PI/8).toFloat())
                .padding(4.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 4.dp)
                        .background(colorResource(id = R.color.background))
                ) {

                    AsyncImage(
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionHomeToAnyUserDetails(
                                        photoCard
                                    )
                                )
                            },
                        model = photoCard.userProfileImage.data,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.CenterStart,
                        onError = {
                            // TODO
                        }
                    )

                    Text(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = photoCard.nickname,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )

                    var formattedDate = ""
                    try{
                        /*val dateString = photoCard.createdAt // in Mongoose, e.g. "2023-02-02T22:13:16.816Z"
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        val date = inputFormat.parse(dateString)
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                        formattedDate = if(date!= null) outputFormat.format(date).toString() else ""*/
                        val dateString = photoCard.createdAt
                        val pattern = "(\\d{4})-(\\d{2})-(\\d{2})".toRegex()
                        val matchResult = pattern.find(dateString)
                        if (matchResult != null) {
                            val result = matchResult.groupValues
                            val year = result[1]
                            val month = result[2]
                            val day = result[3]
                            formattedDate = "$day/$month/$year"
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        formattedDate = ""
                    }

                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterEnd),
                        text = formattedDate,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )

                }
                Box {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        model = photoCard.photo.data,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        onError = {
                            // TODO
                        }
                    )
                    Column(modifier = Modifier.align(Alignment.BottomEnd)) {

                        if (menuProvider.selectedFilterUri == "explore") {
                            IconButton(onClick = {

                                val isLocationPermissionGranted = ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED &&
                                        ContextCompat.checkSelfPermission(
                                            requireContext(),
                                            android.Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED

                                when (isLocationPermissionGranted) {
                                    true -> findNavController().navigate(
                                        HomeFragmentDirections.actionHomeToMap(
                                            photoCard
                                        )
                                    )
                                    else -> requestLocationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                            android.Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    )
                                }

                            }) {
                                Icon(
                                    modifier = Modifier.size(40.dp),
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "",
                                    tint = colorResource(id = R.color.location_color),
                                )
                            }
                        }
                        if (photoCard.scores.isNotEmpty()) {
                            IconButton(onClick = {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionHomeToScores(
                                        photoCard
                                    )
                                )
                            }) {
                                Icon(
                                    modifier = Modifier.size(40.dp),
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "",
                                    tint = colorResource(id = R.color.score_color)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 4.dp)
                        .background(colorResource(id = R.color.background))
                ) {
                    Text(
                        modifier = Modifier
                            // .padding(horizontal = 8.dp)
                            .align(Alignment.CenterStart),
                        text = photoCard.caption,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    @Composable
    private fun AppBarFilter(){
        if(menuProvider.selectedFilterTitle.value.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, top = 0.dp, start = 0.dp, end = 0.dp)
                    .background(color = colorResource(R.color.primary)),
                color = colorResource(R.color.background),
                text = menuProvider.selectedFilterTitle.value,
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
        }
    }

    // endregion


    // region private function


    // endregion

}