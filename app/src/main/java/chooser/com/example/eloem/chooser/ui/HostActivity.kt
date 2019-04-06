package chooser.com.example.eloem.chooser.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.findNavController
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.helperClasses.AnimatedIconFab
import chooser.com.example.eloem.chooser.util.currentNoActionBarTheme
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
            when {
                currentDestination == R.id.listOfChooserFragment &&
                    destination.id == R.id.chooserItemChooserEditorFragment -> {
                    
                }
            
            }
            
            currentDestination = destination.id
        }
        
        /*var b = false
        mainFab.setOnClickListener {
            val drawable = getDrawable(if (b) R.drawable.check_to_next else R.drawable.next_to_check)
            b = !b
            val animatable = drawable as Animatable
            mainFab.setImageDrawable(drawable)
            animatable.start()
        }*/
        
        mainFab.icon = AnimatedIconFab.Icon.ADD
        
        bottomSheet.listTitleTV.setOnClickListener {
            sheetBehavior.state = if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_EXPANDED
            else BottomSheetBehavior.STATE_COLLAPSED
        }
    }
    
    val rootView: View by lazy { findViewById<CoordinatorLayout>(R.id.rootCoordinator) }
    
    val mainFab: AnimatedIconFab by lazy { findViewById<AnimatedIconFab>(R.id.mainFab) }
    
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
    
    fun hideFab() {
        val p = mainFab.layoutParams as CoordinatorLayout.LayoutParams
        p.anchorId = View.NO_ID
        p.gravity = Gravity.END or Gravity.BOTTOM
        mainFab.layoutParams = p
        mainFab.hide()
    }
    
    fun showFab() {
        val p = mainFab.layoutParams as CoordinatorLayout.LayoutParams
        p.anchorId = R.id.bottomSheet
        p.gravity = Gravity.NO_GRAVITY
        mainFab.layoutParams = p
        mainFab.show()
    }
    
    override fun onSupportNavigateUp() =
            findNavController(R.id.navHostFragment).navigateUp()
}
