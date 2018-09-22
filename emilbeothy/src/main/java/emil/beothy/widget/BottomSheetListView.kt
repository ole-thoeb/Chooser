package emil.beothy.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView
import android.widget.AbsListView
import android.view.MotionEvent

class BottomSheetListView: ListView {
    
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)
    
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }
    
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (canScrollVertically(this)) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.onTouchEvent(ev)
    }
    
    private fun canScrollVertically(view: AbsListView?): Boolean {
        var canScroll = false
        
        if (view != null && view.childCount > 0) {
            val isOnTop = view.firstVisiblePosition != 0 || view.getChildAt(0).top != 0
            val isAllItemsVisible = isOnTop && view.lastVisiblePosition == view.childCount
            
            if (isOnTop || isAllItemsVisible) {
                canScroll = true
            }
        }
        
        return canScroll
    }
}