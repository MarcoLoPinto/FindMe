package com.liner.findme.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.liner.findme.R
import com.liner.findme.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.beryukhov.reactivenetwork.ReactiveNetwork

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // region private properties

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestLocationPermission()
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.main_activity_permission_not_granted),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted.values.any { !it }) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.main_activity_permission_not_granted),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                navController.navigate(R.id.photo)
            }
        }

    private val navController: NavController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navHostFragment.navController
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_actionbar_main, menu)
        }

        override fun onPrepareMenu(menu: Menu) {
            super.onPrepareMenu(menu)
            menu.findItem(R.id.user_details).isVisible =
                navController.currentDestination?.id == R.id.home
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return menuItem.onNavDestinationSelected(navController)
        }
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var binding: ActivityMainBinding

    private var connectionAlert: AlertDialog? = null

    // endregion

    // region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            ActivityMainBinding.inflate(layoutInflater) // layoutInflater = connect xml with code, ActivityMainBinding uses it
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.topAppBar)

        // connect action bar with navigation component
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // add provider for handling icons in action bar
        addMenuProvider(menuProvider)

        binding.takePhoto.setOnClickListener { requestCameraPermission() }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            handleDestinationChanged(destination)
        }
        lifecycleScope.launch {
            ReactiveNetwork().observeInternetConnectivity()
                .flowWithLifecycle(lifecycle)
                .catch { emit(false) }
                .flowOn(Dispatchers.IO)
                .drop(1)
                .collectLatest { isConnected ->
                    connectionAlert?.dismiss()
                    if (isConnected.not()) {
                        connectionAlert = MaterialAlertDialogBuilder(this@MainActivity)
                            .setMessage(R.string.main_activity_internet_not_available_message)
                            .setCancelable(false)
                            .show()
                    }
                }
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) or super.onSupportNavigateUp()
    }

    // endregion

    // region private functions

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> { // Permission previously granted
                requestLocationPermission()
            }

            this.shouldShowRequestPermissionRationale(
                android.Manifest.permission.CAMERA
            ) -> { // Show camera permissions dialog
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.main_activity_permission_camera_warning_title)
                    .setMessage(R.string.main_activity_permission_camera_warning_message)
                    .setPositiveButton(R.string.main_activity_permission_camera_warning_positive_button) { dialog, _ ->
                        dialog.dismiss()
                        this.requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    .setNegativeButton(R.string.main_activity_permission_camera_warning_negative_button) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            else -> this.requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> { // Permission previously granted
                navController.navigate(R.id.photo)
            }

            this.shouldShowRequestPermissionRationale(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) ||
                    this.shouldShowRequestPermissionRationale(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) -> { // Show position permissions dialog
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.main_activity_permission_position_warning_title)
                    .setMessage(R.string.main_activity_permission_position_warning_message)
                    .setPositiveButton(R.string.main_activity_permission_position_warning_positive_button) { dialog, _ ->
                        dialog.dismiss()
                        val permissions = arrayOf(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        this.requestLocationPermissionLauncher.launch(permissions)
                    }
                    .setNegativeButton(R.string.main_activity_permission_position_warning_negative_button) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            else -> {
                val permissions = arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                this.requestLocationPermissionLauncher.launch(permissions)
            }
        }
    }

    private fun handleDestinationChanged(destination: NavDestination) {
        when (destination.id) {
            R.id.photo,
            R.id.global_scores,
            R.id.user_details -> with(binding) {
                // bottomAppBar.performHide(true)
                takePhoto.hide()
                invalidateOptionsMenu()
            }
            R.id.map -> with(binding) {
                // bottomAppBar.performHide(true)
                takePhoto.hide()
            }
            else -> with(binding) {
                // bottomAppBar.performShow(true)
                takePhoto.show()
            }
        }
    }

    // endregion

}