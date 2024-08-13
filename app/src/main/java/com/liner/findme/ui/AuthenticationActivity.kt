package com.liner.findme.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.liner.findme.R
import com.liner.findme.databinding.ActivityAuthenticationBinding
import com.liner.findme.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {

    // region private properties

    private val navController: NavController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navHostFragment.navController
    }

    private lateinit var binding: ActivityAuthenticationBinding

    // endregion

    // region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater) // layoutInflater = connect xml with code, ActivityMainBinding uses it
        val view = binding.root
        setContentView(view)

    }

    // endregion

}