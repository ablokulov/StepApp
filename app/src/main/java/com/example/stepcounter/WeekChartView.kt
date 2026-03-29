package com.example.stepcounter

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class WeekChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data = IntArray(7)
    private var goal = 10000
    private var isDarkMode = false

    private val days = arrayOf("DU", "SE", "CH", "PA", "SH", "YA", "BU")

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }
    private val areaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val dotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    private val dotGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 0.8f
        pathEffect = DashPathEffect(floatArrayOf(4f, 5f), 0f)
    }
    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val activeDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val activePillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var lineGradient: LinearGradient

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        updateTheme()
    }

    private fun updateTheme() {
        isDarkMode = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            glowPaint.color = Color.parseColor("#4400BBFF")
            dotStrokePaint.color = Color.parseColor("#00BBFF")
            dotGlowPaint.color = Color.parseColor("#8800BBFF")
            gridPaint.color = Color.parseColor("#1AFFFFFF")
            dayPaint.color = Color.parseColor("#55FFFFFF")
            activeDayPaint.color = Color.WHITE
            activePillPaint.color = Color.parseColor("#33FFFFFF")
        } else {
            glowPaint.color = Color.parseColor("#334F46E5")
            dotStrokePaint.color = Color.parseColor("#4F46E5")
            dotGlowPaint.color = Color.parseColor("#554F46E5")
            gridPaint.color = Color.parseColor("#1A4F46E5")
            dayPaint.color = Color.parseColor("#94A3B8")
            activeDayPaint.color = Color.parseColor("#1E293B")
            activePillPaint.color = Color.parseColor("#1A4F46E5")
        }
    }

    fun setData(steps: IntArray, goalVal: Int) {
        data = steps.copyOf()
        goal = goalVal
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        updateTheme()
        val c1 = if (isDarkMode) Color.parseColor("#0088CC") else Color.parseColor("#4F46E5")
        val c2 = if (isDarkMode) Color.parseColor("#00DDFF") else Color.parseColor("#7C3AED")
        lineGradient = LinearGradient(0f, 0f, w.toFloat(), 0f, intArrayOf(c1, c2), null, Shader.TileMode.CLAMP)
        linePaint.shader = lineGradient
    }

    override fun onDraw(canvas: Canvas) {
        val W = width.toFloat(); val H = height.toFloat()
        val dayH = 44f; val chartTop = dayH + 8f; val chartBot = H - 16f; val chartH = chartBot - chartTop
        val maxVal = maxOf(data.maxOrNull() ?: goal, goal).toFloat() * 1.15f
        val n = 7; val colW = W / n
        val xs = Array(n) { i -> colW * i + colW / 2f }
        val ys = Array(n) { i -> chartBot - (data[i].toFloat() / maxVal) * chartH }

        val goalY = chartBot - (goal.toFloat() / maxVal) * chartH
        canvas.drawLine(0f, goalY, W, goalY, gridPaint)
        for (i in 0 until n) canvas.drawLine(xs[i], chartTop, xs[i], chartBot, gridPaint)

        val areaPath = Path()
        areaPath.moveTo(xs[0], ys[0])
        for (i in 1 until n) { val cx = (xs[i-1]+xs[i])/2f; areaPath.cubicTo(cx, ys[i-1], cx, ys[i], xs[i], ys[i]) }
        areaPath.lineTo(xs[n-1], chartBot); areaPath.lineTo(xs[0], chartBot); areaPath.close()

        val ac1 = if (isDarkMode) Color.parseColor("#5500BBFF") else Color.parseColor("#334F46E5")
        val ac2 = if (isDarkMode) Color.parseColor("#0000BBFF") else Color.parseColor("#004F46E5")
        areaPaint.shader = LinearGradient(0f, chartTop, 0f, chartBot, intArrayOf(ac1, ac2), null, Shader.TileMode.CLAMP)
        canvas.drawPath(areaPath, areaPaint)

        val linePath = Path()
        linePath.moveTo(xs[0], ys[0])
        for (i in 1 until n) { val cx = (xs[i-1]+xs[i])/2f; linePath.cubicTo(cx, ys[i-1], cx, ys[i], xs[i], ys[i]) }
        canvas.drawPath(linePath, glowPaint)
        canvas.drawPath(linePath, linePaint)

        for (i in 0 until n) {
            val isLast = i == n-1; val r = if (isLast) 9f else 6f
            if (isLast) {
                canvas.drawCircle(xs[i], ys[i], r+4f, dotGlowPaint)
                val lp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isDarkMode) Color.parseColor("#00BBFF") else Color.parseColor("#4F46E5")
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(xs[i], ys[i], r, lp)
            } else {
                val dp = Paint(dotPaint).apply { color = if (isDarkMode) Color.WHITE else Color.WHITE }
                canvas.drawCircle(xs[i], ys[i], r, dp)
                canvas.drawCircle(xs[i], ys[i], r, dotStrokePaint)
            }
        }

        for (i in 0 until n) {
            val isToday = i == n-1
            if (isToday) {
                val pillW = 42f; val pillH = 28f; val rx = xs[i]-pillW/2f; val ry = 8f
                canvas.drawRoundRect(rx, ry, rx+pillW, ry+pillH, 14f, 14f, activePillPaint)
                canvas.drawText(days[i], xs[i], dayH-10f, activeDayPaint)
            } else {
                canvas.drawText(days[i], xs[i], dayH-10f, dayPaint)
            }
        }
    }
}