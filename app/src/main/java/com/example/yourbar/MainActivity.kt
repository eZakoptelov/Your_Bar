package com.example.yourbar

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.yourbar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Обработка системных отступов (статус-бар, навигационная панель)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.dest_assembly, R.id.dest_cart)
        )

        // Связываем BottomNavigationView с NavController
        binding.bottomNav.setupWithNavController(navController)

        // --- ГЛАВНОЕ: Логика скрытия/показа BottomNav ---
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Список ID всех калькуляторов, где BottomNav должен быть скрыт
            val calculatorDestinations = setOf(
                R.id.calc_budget_fragment,
                R.id.calc_premium_fragment,
                R.id.calc_perfectum_fragment,
                R.id.calc_exclusive_fragment
            )

            val shouldHideBottomNav = destination.id in calculatorDestinations

            if (shouldHideBottomNav) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }
        // -----------------------------------------------
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
