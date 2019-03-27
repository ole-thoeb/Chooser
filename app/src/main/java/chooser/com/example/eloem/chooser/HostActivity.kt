package chooser.com.example.eloem.chooser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.findNavController
import chooser.com.example.eloem.chooser.util.currentNoActionBarTheme
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_host.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*

class HostActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentNoActionBarTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        setSupportActionBar(toolbar)
        
        val navController = findNavController(R.id.navHostFragment)
        var currentDestination = navController.currentDestination?.id
        
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.chooserItemChooserEditorFragment) {
            
            }
            
            currentDestination = destination.id
        }
    
        
        bottomSheet.listTitleTV.setOnClickListener {
            sheetBehavior.state = if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_EXPANDED
            else BottomSheetBehavior.STATE_COLLAPSED
        }
    }
    
    val rootView: View by lazy { findViewById<CoordinatorLayout>(R.id.rootCoordinator) }
    
    val mainFab: FloatingActionButton by lazy { findViewById<FloatingActionButton>(R.id.mainFab) }
    
    val bottomSheet: LinearLayout by lazy { findViewById<LinearLayout>(R.id.bottomSheet) }
    
    private val sheetBehavior by lazy { BottomSheetBehavior.from(bottomSheet) }
    
    fun hideBottomSheet() {
        sheetBehavior.isHideable = true
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
    
    fun showBottomSheet() {
        sheetBehavior.isHideable = false
        //sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    
    override fun onSupportNavigateUp() =
            findNavController(R.id.navHostFragment).navigateUp()
}
