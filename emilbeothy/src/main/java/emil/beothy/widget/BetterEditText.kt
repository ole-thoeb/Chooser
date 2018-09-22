package emil.beothy.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import emil.beothy.R
import emil.beothy.utilFun.countSubstrings
import emil.beothy.utilFun.findPositionOfSubstring
import emil.beothy.utilFun.minus
import emil.beothy.utilFun.removeAllSubstrings

class BetterEditText: EditText {
    private var hasTextWatcher = false
    private var textWatcher: TextWatcher? = null
    
    //called when text changes
    var setTextChangeListener = {p0: CharSequence, v: BetterEditText -> Unit}
    //called when enter key at the end pressed
    var setSpecialKeyListener = {key: KeyEvent -> Unit}
    //called when enter ist pressed
    var setEnterKeyListener = {stringAfterEnter: String -> Unit}
    
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int = R.style.BetterEditText): super(context, attrs, defStyleAttr, defStyleRes)
    
    init {
        addTextChangedListener((object: TextWatcher{
            lateinit var lastString: String
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    val string = editable.toString()
                    //check if there is a new linebreak
                    if (string.countSubstrings("\n") != lastString.countSubstrings("\n")){
                        //see if its just a new linebreak
                        if (lastString.length == string.length - 1){
                            val beforePos = lastString.findPositionOfSubstring("\n")
                            val afterPos = string.findPositionOfSubstring("\n")
                            
                            var i = 0
                            while (i > beforePos.size && beforePos[i] == afterPos[i]) i++
                            val pos = afterPos[i]
                            
                            val stringAfterEnter = string.substring(pos + 1)
                            editable.delete(pos, editable.length)
                            setEnterKeyListener(stringAfterEnter)
                        }
                    }
                }
            }
    
            override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {
                lastString = p0.toString()
            }
    
            override fun onTextChanged(p0: CharSequence, start: Int, before: Int, count: Int) {
                setTextChangeListener(p0, this@BetterEditText)
            }
        }))
    }
    
    override fun addTextChangedListener(watcher: TextWatcher?) {
        if (!hasTextWatcher){
            textWatcher = watcher
            super.addTextChangedListener(watcher)
        }
    }
    
    fun removeTextChangedListener() {
        if (hasTextWatcher){
            hasTextWatcher = false
            this.removeTextChangedListener(textWatcher)
            textWatcher = null
        }
    }
}