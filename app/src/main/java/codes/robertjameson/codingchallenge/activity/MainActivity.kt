package codes.robertjameson.codingchallenge.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import codes.robertjameson.codingchallenge.R
import codes.robertjameson.codingchallenge.databinding.ActivityMainBinding
import codes.robertjameson.codingchallenge.utils.checkForInternet
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkForInternet(this)) {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_blog, R.id.navigation_map
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        } else {
            Toast.makeText(
                this, "I need internet to work", Toast.LENGTH_LONG
            ).show()
        }
    }

    // Had to Google how to add back stacking for the top navigation button
    //https://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}