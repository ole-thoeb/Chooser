package chooser.com.example.eloem.chooser.helperClasses

import android.content.Context
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import chooser.com.example.eloem.chooser.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Error

class AnimatedIconFab @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = com.google.android.material.R.attr.floatingActionButtonStyle
) : FloatingActionButton(context, attrs, defStyleAttr) {
    
    enum class Icon {
        ADD, NEXT, CHECK, REFRESH;
        
        fun simpleDrawableRes(): Int = when(this) {
            ADD -> R.drawable.add_to_check
            NEXT -> R.drawable.next_to_refresh
            CHECK -> R.drawable.check_to_add
            REFRESH -> R.drawable.refresh_to_check
        }
    }
    
    private lateinit var currentIcon: Icon
    
    var icon: Icon
        get() = currentIcon
        set(value) {
            currentIcon = value
            setImageResource(icon.simpleDrawableRes())
        }
    
    fun animateToIcon(icon: Icon) {
        if (icon == currentIcon) return
        setAnimatableAndStart(getDrawableResTransition(currentIcon, icon))
        currentIcon = icon
    }
    
    private fun setAnimatableAndStart(@DrawableRes resourceId: Int) {
        val drawable = context.getDrawable(resourceId)
        val animatable = drawable as Animatable
        setImageDrawable(drawable)
        animatable.start()
    }
    
    private fun getDrawableResTransition(from: Icon, to: Icon): Int = when {
        from == Icon.ADD && to == Icon.CHECK -> R.drawable.add_to_check
        from == Icon.ADD && to == Icon.REFRESH -> R.drawable.add_to_refresh
        from == Icon.ADD && to == Icon.NEXT -> R.drawable.add_to_next
        
        from == Icon.CHECK && to == Icon.ADD -> R.drawable.check_to_add
        from == Icon.CHECK && to == Icon.REFRESH -> R.drawable.check_to_refresh
        from == Icon.CHECK && to == Icon.NEXT -> R.drawable.check_to_next
    
        from == Icon.REFRESH && to == Icon.CHECK -> R.drawable.refresh_to_check
        from == Icon.REFRESH && to == Icon.ADD -> R.drawable.refresh_to_add
        from == Icon.REFRESH && to == Icon.NEXT -> R.drawable.refresh_to_next
        
        from == Icon.NEXT && to == Icon.CHECK -> R.drawable.next_to_check
        from == Icon.NEXT && to == Icon.REFRESH -> R.drawable.next_to_refresh
        from == Icon.NEXT && to == Icon.ADD -> R.drawable.next_to_add
        
        else -> throw Error("Unknown transition from: $from to $to")
    }
}