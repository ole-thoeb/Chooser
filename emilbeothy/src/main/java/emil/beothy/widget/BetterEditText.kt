package emil.beothy.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import emil.beothy.R

class BetterEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                               defStyleAttr: Int = R.attr.editTextStyle):
        EditText(context, attrs, defStyleAttr) {
    
    //called when text changes
    var onTextChangeListener = { s: CharSequence, v: BetterEditText -> Unit }
    //called when lineBreaks appear
    var onLineBreakListener = { subStrings: List<String>, v: BetterEditText -> Unit }
    //called when delete is pressed at position 0 in editText
    var onDelAtStartListener = { remainingString: String, v: BetterEditText -> Unit }
    
    
    init {
        addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    val string = editable.toString()
                    //Log.d(TAG, "afterText editableStr: $string")
                    val list = string.split('\n')
                    if (list.size > 1) {
                        editable.delete(list[0].length, editable.length)
                        onLineBreakListener(list.drop(1), this@BetterEditText)
                    }
                }
            }
    
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
    
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                onTextChangeListener(s, this@BetterEditText)
            }
        })
        
        setOnKeyListener { v, keyCode, keyEvent ->
            //handel special actions on backspace
            if (keyEvent.action == KeyEvent.ACTION_UP) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_DEL -> {
                        onDelAtStartListener(text.toString(), this)
                        return@setOnKeyListener true
                    }
                }
            }
            false
        }
    }
    
    companion object {
        private const val TAG = "BetterEditText"
    }
}