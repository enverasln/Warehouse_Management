package tr.com.cetinkaya.warehousemanagement

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tr.com.cetinkaya.feature_common.BackPressInterceptor
import tr.com.cetinkaya.feature_common.BaseActivity
import tr.com.cetinkaya.feature_common.app_effect.AppEffect
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import tr.com.cetinkaya.feature_common.dialog.global_dialog.GlobalDialogHostFragment
import tr.com.cetinkaya.feature_common.snackbar.showErrorSnackbar
import tr.com.cetinkaya.feature_common.snackbar.showSuccessSnackbar
import tr.com.cetinkaya.warehousemanagement.databinding.ActivityMainBinding
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val bindLayout: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    @Inject
    lateinit var appEventBus: AppEventBus

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appEventBus.effect.collect { effect ->
                    when (effect) {
                        is AppEffect.ShowError -> {
                            binding.root.showErrorSnackbar(effect.message, Snackbar.LENGTH_LONG)
                        }

                        is AppEffect.ShowSuccess -> {
                            binding.root.showSuccessSnackbar(effect.message, Snackbar.LENGTH_LONG)
                        }

                        else -> {}
                    }
                }
            }
        }

        val fm = supportFragmentManager
        if (fm.findFragmentByTag(GlobalDialogHostFragment.TAG) == null) {
            fm.beginTransaction()
                .add(GlobalDialogHostFragment.newInstance(), GlobalDialogHostFragment.TAG)
                .commitNow()    // host hemen hazır olsun
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val current = navHostFragment.childFragmentManager.fragments.firstOrNull()

        if (current is BackPressInterceptor && current.onToolbarBackButtonPressed()) {
            return true
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}