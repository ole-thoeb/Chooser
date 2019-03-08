package chooser.com.example.eloem.chooser.util

import android.content.Context
import android.preference.PreferenceManager
import chooser.com.example.eloem.chooser.R
import org.jetbrains.anko.defaultSharedPreferences

const val ITEM_ID_ID = "itemIdId"

fun newItemId(context: Context): Int = newId(context, ITEM_ID_ID)

const val LIST_ID_ID = "listIdId"

fun newListId(context: Context): Int = newId(context, LIST_ID_ID)

fun newId(context: Context, key: String): Int{
    val DSP = context.defaultSharedPreferences
    val id = DSP.getInt(key, 0)
    DSP.edit().putInt(key, id + 1).apply()
    return id
}

const val CURRENT_THEME_ID = "settingsTheme"

private val Context.curThemeId get() = defaultSharedPreferences.getString(CURRENT_THEME_ID, "0")

val Context.currentColoredTheme get() = when(curThemeId){
    "1" -> R.style.DarkAppTheme_ColoredActionBar
    "2" -> R.style.BlackAppTheme
    else -> R.style.LightAppTheme_ColoredActionBar
}

val Context.currentTheme get() = when(curThemeId){
    "1" -> R.style.DarkAppTheme
    "2" -> R.style.BlackAppTheme
    else -> R.style.LightAppTheme
}

val Context.currentNoActionBarTheme get() = when(curThemeId){
    "1" -> R.style.LightAppTheme_NoActionBar
    "2" -> R.style.LightAppTheme_NoActionBar
    else -> R.style.LightAppTheme_NoActionBar
}

const val NEED_TO_RECREATE_MAIN = "recreateMain"

fun writeRecreateMain(context: Context, value: Boolean){
    val pM = PreferenceManager.getDefaultSharedPreferences(context).edit()
    pM.putBoolean(NEED_TO_RECREATE_MAIN, value).apply()
}

fun readRecreateMain(context: Context): Boolean{
    val pM = PreferenceManager.getDefaultSharedPreferences(context)
    return pM.getBoolean(NEED_TO_RECREATE_MAIN, false)
}