package chooser.com.example.eloem.chooser.helperClasses

import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log

class WeightProgressDrawable(fillColor: Int, strokeColor: Int): Drawable() {
    
    private val boarderPaint = Paint().apply {
        color = strokeColor
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
    }
    
    private val fillPaint = Paint().apply {
        color = fillColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private lateinit var weightPath: Path
    private lateinit var maskPath: Path
    
    var progressPercent = 1f
        set(value) {
            if (value != field && value > 0f && value <= 100f) {
                progressChanged = true
                invalidateSelf()
                field = value
            }
        }
    
    private var progressChanged = true
    
    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        init()
    }
    
    private var scaleFactor = 0f
    private var xOffset = 0f
    private var yOffset = 0f
    
    private fun init(){
        weightPath = Path()
        val b = bounds
        val height = b.height()
        val width = b.width()
        xOffset = if (height < width) (width - height) / 2f else 0f
        yOffset = if (height > width) (height - width) / 2f else 0f
        val squareSize = width - xOffset
        scaleFactor = squareSize / 24f
        
        boarderPaint.strokeWidth = 1.5f * scaleFactor
        
        weightPath.apply {
            addCircle(12f * scaleFactor + xOffset, 5f * scaleFactor + yOffset, 3f * scaleFactor, Path.Direction.CW)
            
            moveTo(3f * scaleFactor + xOffset, 20f * scaleFactor + yOffset)
            lineTo(21f * scaleFactor + xOffset, 20f * scaleFactor + yOffset)
            lineTo(17f * scaleFactor + xOffset, 8f * scaleFactor + yOffset)
            lineTo(7f * scaleFactor + xOffset,8f * scaleFactor + yOffset)
            lineTo(3f * scaleFactor + xOffset, 20f * scaleFactor + yOffset)
        }
    }
    
    private fun updateMask(){
        if (!progressChanged) return
        
        val percent = 1 - progressPercent / 100f
        val targetY = (17f * percent + 2.5f) * scaleFactor + yOffset
    
        Log.d("WeightDraw", "updating Mask with targetY = $targetY")
        
        maskPath = Path().apply {
            moveTo(0f * scaleFactor + xOffset, 19.5f * scaleFactor + yOffset)
            lineTo(24f * scaleFactor + xOffset, 19.5f * scaleFactor + yOffset)
            lineTo(24f * scaleFactor + xOffset, targetY)
            lineTo(0f * scaleFactor + xOffset, targetY)
            lineTo(0f * scaleFactor + xOffset, 19.5f * scaleFactor + yOffset)
        }
        progressChanged = false
    }
    
    override fun draw(canvas: Canvas) {
        Log.d("WeightDraw", "drawing")
        canvas.drawPath(weightPath, boarderPaint)
        updateMask()
        canvas.clipPath(maskPath)
        canvas.drawPath(weightPath, fillPaint)
        canvas.drawPath(weightPath, boarderPaint)
    }
    
    override fun setAlpha(alpha: Int) {
    }
    
    override fun getOpacity(): Int = PixelFormat.UNKNOWN
    
    override fun setColorFilter(colorFilter: ColorFilter?) {
    }
}