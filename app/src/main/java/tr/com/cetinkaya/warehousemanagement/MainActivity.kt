package tr.com.cetinkaya.warehousemanagement

import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import tr.com.cetinkaya.feature_common.BackPressInterceptor
import tr.com.cetinkaya.feature_common.BaseActivity
import tr.com.cetinkaya.warehousemanagement.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val bindLayout: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun prepareView(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

//        supportActionBar?.setDisplayShowTitleEnabled(true)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                tr.com.cetinkaya.feature_auth.R.id.auth_nav_graph,
                tr.com.cetinkaya.feature_home.R.id.home_graph,


            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
//        val config = AppBarConfiguration(setOf(id.auth_nav_graph, id.home_graph))
//        binding.toolbar.setupWithNavController(navController, config)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val current = navHostFragment.childFragmentManager.fragments.firstOrNull()

        if (current is BackPressInterceptor && current.onToolbarBackButtonPressed()) {
            return true
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}