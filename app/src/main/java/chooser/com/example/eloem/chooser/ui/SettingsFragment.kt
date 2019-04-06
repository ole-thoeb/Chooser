package chooser.com.example.eloem.chooser.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.util.getAttribute

class SettingsFragment : PreferenceFragmentCompat() {
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey)
        
        findPreference<ListPreference>("settingsTheme")?.setOnPreferenceChangeListener { preference, newValue ->
            requireActivity().recreate()
            true
        }
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    
        (activity as HostActivity?)?.apply{
            supportActionBar?.apply {
                title = resources.getString(R.string.action_settings)
                setDisplayShowTitleEnabled(true)
                setDisplayShowCustomEnabled(false)
                setDisplayHomeAsUpEnabled(true)
            }
            
            hideBottomSheet()
            hideFab()
        }
    }
    
    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        val rv = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        rv.setBackgroundColor(requireContext().getAttribute(R.attr.backgroundColor).data)
        return rv
    }
}
