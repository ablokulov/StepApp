package com.example.stepcounter

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class RingProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var progress = 0
    private var strokeWidth = 28f
    private var isLarge = true
    private var isDarkMode = false

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

    private lateinit var gradient: SweepGradient

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RingProgressView)
        strokeWidth = a.getDimension(R.styleable.RingProgressView_ringStrokeWidth, 28f)
        isLarge = a.getBoolean(R.styleable.RingProgressView_ringLarge, true)
        a.recycle()

        isDarkMode = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        fgPaint.strokeWidth = strokeWidth
        bgPaint.strokeWidth = strokeWidth
        glowPaint.strokeWidth = strokeWidth * 0.6f

        updateColors()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private fun updateColors() {
        if (isDarkMode) {
            bgPaint.color = Color.parseColor("#1A2D40")
            glowPaint.color = Color.parseColor("#4400BBFF")
        } else {
            bgPaint.color = Color.parseColor("#EEF0FF")
            glowPaint.color = Color.parseColor("#334F46E5")
        }
    }

    fun setProgress(pct: Int) {
        progress = pct.coerceIn(0, 100)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isDarkMode = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        updateColors()

        val colors = if (isDarkMode) {
            intArrayOf(
                Color.parseColor("#0088CC"),
                Color.parseColor("#00CCFF"),
                Color.parseColor("#00FFEE"),
                Color.parseColor("#0088CC")
            )
        } else {
            intArrayOf(
                Color.parseColor("#4F46E5"),
                Color.parseColor("#7C3AED"),
                Color.parseColor("#6366F1"),
                Color.parseColor("#4F46E5")
            )
        }
        gradient = SweepGradient(w / 2f, h / 2f, colors, floatArrayOf(0f, 0.33f, 0.66f, 1f))
        fgPaint.shader = gradient
        glowPaint.shader = gradient
    }

    override fun onDraw(canvas: Canvas) {
        val pad = strokeWidth / 2f + strokeWidth * 0.3f
        val rect = RectF(pad, pad, width - pad, height - pad)
        canvas.drawArc(rect, -90f, 360f, false, bgPaint)
        if (progress > 0) {
            val sweep = 360f * progress / 100f
            canvas.drawArc(rect, -90f, sweep, false, glowPaint)
            canvas.drawArc(rect, -90f, sweep, false, fgPaint)
        }
    }
}