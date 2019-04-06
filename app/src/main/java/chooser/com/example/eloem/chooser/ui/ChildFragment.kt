package chooser.com.example.eloem.chooser.ui

import androidx.fragment.app.Fragment

open class ChildFragment: Fragment() {
    
    val hostActivity: HostActivity get() = activity as HostActivity
}