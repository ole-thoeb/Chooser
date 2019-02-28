package chooser.com.example.eloem.chooser

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.view.ViewGroup
import chooser.com.example.eloem.chooser.util.currentColoredTheme
import chooser.com.example.eloem.chooser.util.getAttribute
import chooser.com.example.eloem.chooser.util.writeRecreateMain


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentColoredTheme)
        super.onCreate(savedInstanceState)
        
        setupActionBar()
        findViewById<ViewGroup>(android.R.id.content).setBackgroundColor(
                getAttribute(R.attr.backgroundColor).data
        )
        fragmentManager
                .beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }
    
    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }
    
    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || SettingsFragment::class.java.name == fragmentName
    }
    
    class SettingsFragment: PreferenceFragment(){
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_settings)
            
            findPreference("settingsTheme").setOnPreferenceChangeListener { _, _ ->
                activity.recreate()
                writeRecreateMain(context, true)
    
                true
            }
        }
    }
    
    
    companion object {
        
        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }
    }
}
