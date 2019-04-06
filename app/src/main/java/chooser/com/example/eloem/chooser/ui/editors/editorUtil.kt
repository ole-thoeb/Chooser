package chooser.com.example.eloem.chooser.ui.editors

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.Toast
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.util.afterTextChanged
import chooser.com.example.eloem.chooser.util.focusAndShowKeyboard
import chooser.com.example.eloem.chooser.util.hideSoftKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.simple_edit_dialog.view.*


val intNotGreaterZero = { string: String -> !string.matches("""\d+""".toRegex()) || string == "0" }

inline fun showEditDialog(
        context: Context,
        hint: String,
        startString: String,
        crossinline invalid: (String) -> Boolean,
        errorMessage: String,
        crossinline positiveAction: (String, DialogInterface, Int) -> Unit,
        crossinline negativeAction: (DialogInterface, Int) -> Unit = { _, _ -> },
        hideSoftInput: Boolean = false
) {
    
    @SuppressLint("InflateParams")
    val custView = LayoutInflater
            .from(context)
            .inflate(R.layout.simple_edit_dialog, null, false)
    custView.valueET.apply {
        setText(startString)
        setSelection(startString.length)
        addTextChangedListener(afterTextChanged { s ->
            val string = s.toString()
            error = if (invalid(string)) errorMessage else null
        })
        
        //showSoftInputOnFocus = true
        //requestFocusFromTouch()
        focusAndShowKeyboard()
    }
    custView.textInputLayout.hint = hint
    
    MaterialAlertDialogBuilder(context)
            .setView(custView)
            .setPositiveButton(R.string.dialogPositive) { dialog, which ->
                if (hideSoftInput) hideSoftKeyboard(context, custView.windowToken)
                val text = custView.valueET.text.toString()
                if (invalid(text)) {
                    Toast.makeText(context,
                            R.string.messageWeightGreaterZero,
                            Toast.LENGTH_SHORT)
                            .show()
                    return@setPositiveButton
                }
                positiveAction(text, dialog, which)
            }
            .setNegativeButton(R.string.dialogNegative) { dialog, whitch ->
                if (hideSoftInput) hideSoftKeyboard(context, custView.windowToken)
                negativeAction(dialog, whitch)
            }
            .setCancelable(false)
            .show()
}