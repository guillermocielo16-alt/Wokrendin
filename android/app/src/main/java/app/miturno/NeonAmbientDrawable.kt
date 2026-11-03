package app.miturno

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Ambient "underglow" for the native UI, matching the dark WebView palette.
 * It is deliberately a BACKGROUND drawable: cards, labels and controls remain sharp.
 * One slow animation, throttled to ~12 fps, and stopped when the Activity is paused.
 * Never uses a network connection, a bitmap, or the work-calendar date calculations.
 */
internal class NeonAmbientDrawable(
    private var base: Int,
    private var darkMode: Boolean,
    private var secondCalendar: Boolean,
) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f
    private var lastInvalidation = 0L

    private val animator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
        duration = 26_000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            val now = SystemClock.uptimeMillis()
            if (now - lastInvalidation >= 80L) {
                lastInvalidation = now
                invalidateSelf()
            }
        }
    }

    fun configure(background: Int, dark: Boolean, second: Boolean) {
        if (base != background || darkMode != dark || secondCalendar != second) {
            base = background
            darkMode = dark
            secondCalendar = second
            invalidateSelf()
        }
    }

    fun start() {
        if (ValueAnimator.areAnimatorsEnabled() && !animator.isStarted) animator.start()
    }

    fun stop() {
        if (animator.isStarted) animator.cancel()
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        paint.shader = null
        paint.color = base
        canvas.drawRect(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat(), paint)

        val w = b.width().toFloat()
        val h = b.height().toFloat()
        val swing = sin(phase.toDouble()).toFloat()
        val drift = cos(phase.toDouble()).toFloat()
        val intensity = if (darkMode) 1f else 0.34f

        // Colors and positions mirror the former warm/cool WebView card shadows.
        if (secondCalendar) {
            halo(canvas, b.left + w * (0.06f + 0.05f * swing),
                b.top + h * (0.22f + 0.05f * drift), w * 0.91f,
                Color.rgb(201, 143, 176), (150f * intensity).toInt())
            halo(canvas, b.left + w * (0.98f - 0.04f * drift),
                b.top + h * (0.74f + 0.06f * swing), w * 0.83f,
                Color.rgb(212, 175, 106), (130f * intensity).toInt())
        } else {
            halo(canvas, b.left + w * (0.02f + 0.05f * swing),
                b.top + h * (0.22f + 0.05f * drift), w * 0.97f,
                Color.rgb(233, 168, 53), (160f * intensity).toInt())
            halo(canvas, b.left + w * (0.98f - 0.05f * drift),
                b.top + h * (0.72f + 0.05f * swing), w * 0.93f,
                Color.rgb(124, 131, 219), (145f * intensity).toInt())
        }
        halo(canvas, b.left + w * (0.25f - 0.07f * drift),
            b.top + h * (0.91f - 0.05f * swing), w * 0.80f,
            Color.rgb(36, 176, 123), (95f * intensity).toInt())
    }

    private fun halo(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int, alpha: Int) {
        val a = alpha.coerceIn(0, 255)
        paint.shader = RadialGradient(
            x, y, max(1f, radius),
            intArrayOf(
                Color.argb(a, Color.red(color), Color.green(color), Color.blue(color)),
                Color.argb(a / 3, Color.red(color), Color.green(color), Color.blue(color)),
                Color.TRANSPARENT,
            ),
            floatArrayOf(0f, 0.43f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(x, y, radius, paint)
        paint.shader = null
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Required by Android Drawable")
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
