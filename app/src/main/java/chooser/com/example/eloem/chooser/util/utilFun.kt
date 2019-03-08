package chooser.com.example.eloem.chooser.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import chooser.com.example.eloem.chooser.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

fun hideSoftKeyboard(context: Context?, view: View?){
    if (context != null) {
        val ipm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        ipm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun View.focusAndShowKeyboard() {
    requestFocusFromTouch()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun randomInt(from: Int, to: Int) = Random().nextInt(to - from) + from

fun randomInt(range: IntRange) = Random().nextInt((range.last + 1) - range.first) + range.first

fun <T> List<T>.randomEntry() = this[randomInt(0 until size)]

fun Context.getAttribute(resourceId: Int, resolveRef: Boolean = true): TypedValue{
    val tv = TypedValue()
    theme.resolveAttribute(resourceId, tv, resolveRef)
    return tv
}

inline fun View.setNoDoubleClickListener(timeInterval: Long = 500, crossinline action: (View) -> Unit){
    setOnClickListener(makeNoDoubleActivation(timeInterval, action))
}

inline fun <T> makeNoDoubleActivation(coolDown: Long, crossinline action: (T) -> Unit): (T) -> Unit {
    var lastClick = 0L
    return {
        val currTime = System.currentTimeMillis()
        if (lastClick + coolDown < currTime) {
            lastClick = currTime
            action(it)
        }
    }
}

inline fun afterTextChanged(crossinline action: (Editable?) -> Unit): TextWatcher {
    return object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            action(s)
        }
    
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
    
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
}

inline fun showDeleteDialog(context: Context, crossinline deleteAction: () -> Unit) {
    MaterialAlertDialogBuilder(context)
            .setMessage(R.string.dialogDeleteListMessage)
            .setNegativeButton(R.string.dialogNegative) {_, _ ->
                //nothing
            }
            .setPositiveButton(R.string.dialogPositive) { _, _ ->
                deleteAction()
            }
            .show()
}

inline fun showRestartDialog(context: Context, crossinline restartAction: () -> Unit) {
    MaterialAlertDialogBuilder(context)
            .setMessage(R.string.dialogRestartListMessage)
            .setNegativeButton(R.string.dialogNegative) {_, _ ->
                //nothing
            }
            .setPositiveButton(R.string.dialogPositive) { _, _ ->
                restartAction()
            }
            .show()
}