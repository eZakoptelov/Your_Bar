package com.example.yourbar.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.yourbar.R
import com.example.yourbar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        isAdmin = intent.getBooleanExtra(LoginActivity.EXTRA_IS_ADMIN, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = insets.left, right = insets.right, top = insets.top)
            if (view is CoordinatorLayout) {
                view.updatePadding(bottom = 0)
                view.fitsSystemWindows = false
            }
            WindowInsetsCompat.CONSUMED
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        val topLevelDestinations = if (isAdmin) {
            setOf(R.id.dest_assembly, R.id.dest_cart, R.id.dest_price, R.id.dest_work_price)
        } else {
            setOf(R.id.dest_assembly, R.id.dest_cart)
        }

        appBarConfiguration = AppBarConfiguration(topLevelDestinations)

        if (!isAdmin) {
            val menu = binding.bottomNav.menu
            menu.findItem(R.id.dest_price)?.isVisible = false
            menu.findItem(R.id.dest_work_price)?.isVisible = false
        }

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val calculatorDestinations = setOf(
                R.id.calc_budget_fragment,
                R.id.calc_premium_fragment,
                R.id.calc_perfectum_fragment,
                R.id.calc_exclusive_fragment
            )

            val shouldHideBottomNav = destination.id in calculatorDestinations

            if (shouldHideBottomNav) {
                binding.bottomNav.animate()
                    .translationY(binding.bottomNav.height.toFloat())
                    .setDuration(250)
                    .withEndAction { binding.bottomNav.visibility = View.GONE }
                    .start()
            } else {
                binding.bottomNav.visibility = View.VISIBLE
                binding.bottomNav.animate()
                    .translationY(0f)
                    .setDuration(250)
                    .start()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
