package com.example.stepcounter

import android.content.Context
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

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#1A2D40")
    }

    private val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)
    }

    private lateinit var gradient: SweepGradient

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RingProgressView)
        strokeWidth = a.getDimension(R.styleable.RingProgressView_ringStrokeWidth, 28f)
        isLarge = a.getBoolean(R.styleable.RingProgressView_ringLarge, true)
        a.recycle()

        fgPaint.strokeWidth = strokeWidth
        bgPaint.strokeWidth = strokeWidth
        glowPaint.strokeWidth = strokeWidth * 0.7f

        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setProgress(pct: Int) {
        progress = pct.coerceIn(0, 100)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gradient = SweepGradient(
            w / 2f, h / 2f,
            intArrayOf(
                Color.parseColor("#0088CC"),
                Color.parseColor("#00CCFF"),
                Color.parseColor("#00FFEE"),
                Color.parseColor("#0088CC")
            ),
            floatArrayOf(0f, 0.33f, 0.66f, 1f)
        )
        fgPaint.shader = gradient
        glowPaint.shader = gradient
    }

    override fun onDraw(canvas: Canvas) {
        val pad = strokeWidth / 2f + strokeWidth * 0.3f
        val rect = RectF(pad, pad, width - pad, height - pad)

        // Fon ring
        canvas.drawArc(rect, -90f, 360f, false, bgPaint)

        if (progress > 0) {
            val sweep = 360f * progress / 100f

            // Glow effekt
            canvas.drawArc(rect, -90f, sweep, false, glowPaint)

            // Asosiy ring
            canvas.drawArc(rect, -90f, sweep, false, fgPaint)
        }
    }
}