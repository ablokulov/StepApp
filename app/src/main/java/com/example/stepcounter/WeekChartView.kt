package com.example.stepcounter

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class WeekChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data = IntArray(7)
    private var goal = 10000

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
        color = Color.parseColor("#4400BBFF")
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    private val areaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val dotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BBFF")
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val dotGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8800BBFF")
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1AFFFFFF")
        strokeWidth = 0.8f
        pathEffect = DashPathEffect(floatArrayOf(4f, 5f), 0f)
    }

    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55FFFFFF")
        textSize = 22f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    private val activeDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val activePillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFFFFF")
    }

    private lateinit var lineGradient: LinearGradient

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setData(steps: IntArray, goalVal: Int) {
        data = steps.copyOf()
        goal = goalVal
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lineGradient = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            intArrayOf(Color.parseColor("#0088CC"), Color.parseColor("#00DDFF")),
            null, Shader.TileMode.CLAMP
        )
        linePaint.shader = lineGradient
    }

    override fun onDraw(canvas: Canvas) {
        val W = width.toFloat()
        val H = height.toFloat()

        val dayH = 44f
        val chartTop = dayH + 8f
        val chartBot = H - 16f
        val chartH = chartBot - chartTop

        val maxVal = maxOf(data.max() ?: goal, goal).toFloat() * 1.15f
        val n = 7
        val colW = W / n

        // X koordinatalari
        val xs = Array(n) { i -> colW * i + colW / 2f }

        // Y koordinatalari
        val ys = Array(n) { i ->
            chartBot - (data[i].toFloat() / maxVal) * chartH
        }

        // Maqsad chizig'i
        val goalY = chartBot - (goal.toFloat() / maxVal) * chartH
        canvas.drawLine(0f, goalY, W, goalY, gridPaint)

        // Vertikal grid
        for (i in 0 until n) {
            canvas.drawLine(xs[i], chartTop, xs[i], chartBot, gridPaint)
        }

        // Area gradient
        val areaPath = Path()
        areaPath.moveTo(xs[0], ys[0])
        for (i in 1 until n) {
            val cx = (xs[i - 1] + xs[i]) / 2f
            areaPath.cubicTo(cx, ys[i - 1], cx, ys[i], xs[i], ys[i])
        }
        areaPath.lineTo(xs[n - 1], chartBot)
        areaPath.lineTo(xs[0], chartBot)
        areaPath.close()

        areaPaint.shader = LinearGradient(
            0f, chartTop, 0f, chartBot,
            intArrayOf(Color.parseColor("#5500BBFF"), Color.parseColor("#0000BBFF")),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawPath(areaPath, areaPaint)

        // Chiziq glow
        val linePath = Path()
        linePath.moveTo(xs[0], ys[0])
        for (i in 1 until n) {
            val cx = (xs[i - 1] + xs[i]) / 2f
            linePath.cubicTo(cx, ys[i - 1], cx, ys[i], xs[i], ys[i])
        }
        canvas.drawPath(linePath, glowPaint)
        canvas.drawPath(linePath, linePaint)

        // Nuqtalar
        for (i in 0 until n) {
            val isLast = i == n - 1
            val r = if (isLast) 9f else 6f
            if (isLast) {
                canvas.drawCircle(xs[i], ys[i], r + 4f, dotGlowPaint)
                canvas.drawCircle(xs[i], ys[i], r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#00BBFF")
                    style = Paint.Style.FILL
                })
                canvas.drawCircle(xs[i], ys[i], r, dotStrokePaint.apply { strokeWidth = 2.5f })
            } else {
                canvas.drawCircle(xs[i], ys[i], r, dotPaint)
                canvas.drawCircle(xs[i], ys[i], r, dotStrokePaint)
            }
        }

        // Kun nomlari
        for (i in 0 until n) {
            val isToday = i == n - 1
            if (isToday) {
                val pillW = 42f; val pillH = 28f
                val rx = xs[i] - pillW / 2f
                val ry = 8f
                canvas.drawRoundRect(rx, ry, rx + pillW, ry + pillH, 14f, 14f, activePillPaint)
                canvas.drawText(days[i], xs[i], dayH - 10f, activeDayPaint)
            } else {
                canvas.drawText(days[i], xs[i], dayH - 10f, dayPaint)
            }
        }
    }
}