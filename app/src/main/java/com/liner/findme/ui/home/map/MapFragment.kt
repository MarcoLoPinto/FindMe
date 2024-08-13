package com.liner.findme.ui.home.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import coil.compose.AsyncImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liner.findme.R
import com.liner.findme.databinding.FragmentMapBinding
import com.liner.findme.ui.home.photo.PhotoViewModel
import com.liner.findme.ui.home.photo.PhotoViewModelImpl
import com.liner.findme.ui.theme.FindMeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem

@AndroidEntryPoint
class MapFragment : Fragment() {
    // region private properties

    private val viewModel: MapViewModel by viewModels<MapViewModelImpl>()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding

    private val args: MapFragmentArgs by navArgs()

    private var popupPlaces = mutableStateOf(false)

    // endregion

    // region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding?.root
        binding?.composeView?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FindMeTheme() {
                    PlacesPopup()
                }

            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let { secureBinding ->
            val mapView = secureBinding.mapview
            val marker: Marker = Marker(mapView)

            val controller = mapView.controller
            mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            controller.setZoom(7.5)

            lifecycleScope.launch {
                viewModel.location
                    .flowWithLifecycle(lifecycle)
                    .filterNotNull()
                    .map { GeoPoint(it.latitude, it.longitude) }
                    .collectLatest { location ->
                        controller.setCenter(location)
                    }
            }

            mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {

                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    Log.d("TAP", p.toString())
                    controller.setCenter(p)
                    marker.position = p
                    mapView.overlays.add(marker)

                    secureBinding.confirm.isGone = false

                    return true
                }

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    Log.d("LongTAP", p.toString())
                    return true
                }

            }))

            secureBinding.confirm.setOnClickListener {
                val selectedPosition = marker.position ?: return@setOnClickListener
                lifecycleScope.launch {
                    viewModel.guessLocation(args.photoCard, selectedPosition)
                        .flowWithLifecycle(lifecycle).collectLatest { result ->
                            result.onSuccess {
                                findNavController().popBackStack()
                            }.onFailure { t ->
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.signin_fragment_authentication_error_title)
                                    .setMessage(t.localizedMessage)
                                    .setNeutralButton(R.string.signin_fragment_authentication_error_neutral_button) { dialog, _ ->
                                        dialog.dismiss()
                                        findNavController().popBackStack()
                                    }
                                    .setCancelable(false)
                                    .show()
                            }
                        }
                }
            }

            secureBinding.places.setOnClickListener { popupPlaces.value = !popupPlaces.value }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // endregion

    // region composable

    @Composable
    private fun PlacesPopup() {
        if (popupPlaces.value) {
            val places = args.photoCard.placesAround


            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { popupPlaces.value = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = true,
                    excludeFromSystemGesture = true
                )
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.6f),
                    shape = RoundedCornerShape(8.dp), color = Color(0xCCEEEEEE)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Divider(color = Color.Gray, thickness = 1.dp)
                        places.forEach {
                            Text(text = it)
                            Divider(color = Color.Gray, thickness = 1.dp)
                        }

                    }

                }

            }


        }

    }

    // endregion


}