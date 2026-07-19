package io.selimdawa.rippleeffect

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Property
import android.util.TypedValue
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.withClip
import io.selimdawa.rippleeffect.MaterialRippleEffect.RippleBuilder
import kotlin.math.pow
import kotlin.math.sqrt
import android.view.ViewGroup.LayoutParams as ViewGroupLayoutParams

class MaterialRippleEffect @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = Rect()
    private val clipPath = Path()
    private val clipRect = RectF()

    var rippleColor = Color.BLACK
        set(value) {
            field = value
            paint.color = value
            paint.alpha = rippleAlpha
            invalidate()
        }
    var rippleOverlay = false
        set(value) {
            field = value
            invalidate()
        }
    var rippleHover = true
    var rippleDiameter = 0
    var rippleDuration = 0
    var rippleAlpha = 0
        set(value) {
            field = value
            paint.alpha = value
            invalidate()
        }
    var rippleDelayClick = true
    var rippleFadeDuration = 0
    var ripplePersistent = false
    var rippleBackground: Drawable? = null
        set(value) {
            field = value
            field?.bounds = bounds
            invalidate()
        }
    var rippleInAdapter = false
    var rippleRoundedCorners = 0f
        set(value) {
            field = value
            updateClipPath()
            invalidate()
        }

    var radius = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var parentAdapter: AdapterView<*>? = null
    private var childView: View? = null

    private var rippleAnimator: AnimatorSet? = null
    private var hoverAnimator: ObjectAnimator? = null

    private val currentCoordinates = Point()
    private val previousCoordinates = Point()

    private var eventCancelled = false
    private var repressed = false
    private var positionInAdapter = 0

    private var pendingClickEvent: PerformClickEvent? = null
    private var pendingPressEvent: PressedEvent? = null
    private var hasPerformedLongPress = false

    private val longClickListener = object : SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            hasPerformedLongPress = childView?.performLongClick() ?: false
            if (hasPerformedLongPress) {
                if (rippleHover) startRipple(null)
                cancelPressedEvent()
            }
        }

        override fun onDown(e: MotionEvent): Boolean {
            hasPerformedLongPress = false
            return true
        }
    }

    private val gestureDetector = GestureDetector(context, longClickListener)

    init {
        setWillNotDraw(false)

        val res = resources
        val defaultColor = ResourcesCompat.getColor(res, R.color.mrl_default_color, null)
        val defaultBackground = ResourcesCompat.getColor(res, R.color.mrl_default_background, null)
        val defaultDiameter = res.getDimensionPixelSize(R.dimen.mrl_default_diameter)
        val defaultRoundedCorners =
            res.getDimensionPixelSize(R.dimen.mrl_default_rounded_corners).toFloat()
        val defaultAlpha = res.getFloatCompat(R.dimen.mrl_default_alpha)
        val defaultDuration = res.getInteger(R.integer.mrl_default_duration)
        val defaultFadeDuration = res.getInteger(R.integer.mrl_default_fade_duration)
        val defaultHover = res.getBoolean(R.bool.mrl_default_hover)
        val defaultDelayClick = res.getBoolean(R.bool.mrl_default_delay_click)
        val defaultPersistent = res.getBoolean(R.bool.mrl_default_persistent)
        val defaultInAdapter = res.getBoolean(R.bool.mrl_default_search_adapter)
        val defaultOverlay = res.getBoolean(R.bool.mrl_default_ripple_overlay)

        context.withStyledAttributes(attrs, R.styleable.MaterialRippleEffect) {
            rippleColor = getColor(R.styleable.MaterialRippleEffect_mrl_rippleColor, defaultColor)
            rippleDiameter = getDimensionPixelSize(
                R.styleable.MaterialRippleEffect_mrl_rippleDimension, defaultDiameter
            )
            rippleOverlay = getBoolean(
                R.styleable.MaterialRippleEffect_mrl_rippleOverlay, defaultOverlay
            )
            rippleHover = getBoolean(
                R.styleable.MaterialRippleEffect_mrl_rippleHover, defaultHover
            )
            rippleDuration = getInt(
                R.styleable.MaterialRippleEffect_mrl_rippleDuration, defaultDuration
            )
            rippleAlpha = (255 * getFloat(
                R.styleable.MaterialRippleEffect_mrl_rippleAlpha, defaultAlpha
            )).toInt()
            rippleDelayClick = getBoolean(
                R.styleable.MaterialRippleEffect_mrl_rippleDelayClick, defaultDelayClick
            )
            rippleFadeDuration = getInteger(
                R.styleable.MaterialRippleEffect_mrl_rippleFadeDuration, defaultFadeDuration
            )
            rippleBackground = getColor(
                R.styleable.MaterialRippleEffect_mrl_rippleBackground, defaultBackground
            ).toDrawable()
            ripplePersistent = getBoolean(
                R.styleable.MaterialRippleEffect_mrl_ripplePersistent, defaultPersistent
            )
            rippleInAdapter = getBoolean(
                R.styleable.MaterialRippleEffect_mrl_rippleInAdapter, defaultInAdapter
            )
            rippleRoundedCorners = getDimensionPixelSize(
                R.styleable.MaterialRippleEffect_mrl_rippleRoundedCorners,
                defaultRoundedCorners.toInt()
            ).toFloat()
        }

        paint.color = rippleColor
        paint.alpha = rippleAlpha
    }

    override fun addView(child: View, index: Int, params: ViewGroupLayoutParams?) {
        check(childCount <= 0) { "MaterialRippleEffect can host only one child" }
        childView = child
        super.addView(child, index, params)
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        childView?.setOnClickListener(onClickListener)
            ?: error("MaterialRippleEffect must have a child view")
    }

    override fun setOnLongClickListener(onClickListener: OnLongClickListener?) {
        childView?.setOnLongClickListener(onClickListener)
            ?: error("MaterialRippleEffect must have a child view")
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val child = childView ?: return false
        return !findClickableViewInChild(child, event.x.toInt(), event.y.toInt())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val superOnTouchEvent = super.onTouchEvent(event)
        val child = childView ?: return superOnTouchEvent

        if (!isEnabled || !child.isEnabled) return superOnTouchEvent

        val isEventInBounds = bounds.contains(event.x.toInt(), event.y.toInt())
        if (isEventInBounds) {
            previousCoordinates.set(currentCoordinates.x, currentCoordinates.y)
            currentCoordinates.set(event.x.toInt(), event.y.toInt())
        }

        if (gestureDetector.onTouchEvent(event) || hasPerformedLongPress) return true

        when (event.actionMasked) {
            MotionEvent.ACTION_UP -> {
                pendingClickEvent = PerformClickEvent()
                if (repressed) {
                    child.isPressed = true
                    postDelayed(
                        { child.isPressed = false },
                        ViewConfiguration.getPressedStateDuration().toLong(),
                    )
                }
                if (isEventInBounds) {
                    startRipple(pendingClickEvent)
                    performClick()
                } else if (!rippleHover) {
                    radius = 0f
                }
                if (!rippleDelayClick && isEventInBounds) {
                    pendingClickEvent?.run()
                }
                cancelPressedEvent()
            }

            MotionEvent.ACTION_DOWN -> {
                updatePositionInAdapter()
                eventCancelled = false
                pendingPressEvent = PressedEvent(event)
                if (isInScrollingContainer) {
                    cancelPressedEvent()
                    repressed = true
                    postDelayed(pendingPressEvent, ViewConfiguration.getTapTimeout().toLong())
                } else {
                    pendingPressEvent?.run()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (rippleInAdapter) {
                    currentCoordinates.set(previousCoordinates.x, previousCoordinates.y)
                    previousCoordinates.set(0, 0)
                }
                child.onTouchEvent(event)
                if (rippleHover && !repressed) startRipple(null)
                else child.isPressed = false
                cancelPressedEvent()
            }

            MotionEvent.ACTION_MOVE -> {
                if (rippleHover) {
                    if (isEventInBounds && !eventCancelled) {
                        invalidate()
                    } else if (!isEventInBounds) {
                        startRipple(null)
                    }
                }
                if (!isEventInBounds) {
                    cancelPressedEvent()
                    hoverAnimator?.cancel()
                    child.onTouchEvent(event)
                    eventCancelled = true
                }
            }
        }
        return true
    }

    private fun cancelPressedEvent() {
        pendingPressEvent?.let {
            removeCallbacks(it)
            repressed = false
        }
    }

    private fun startHover() {
        if (eventCancelled) return
        hoverAnimator?.cancel()

        val calcRadius =
            (sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0)) * 1.2f).toFloat()
        hoverAnimator =
            ObjectAnimator.ofFloat(this, radiusProperty, rippleDiameter.toFloat(), calcRadius)
                .apply {
                    duration = resources.getInteger(R.integer.mrl_hover_duration).toLong()
                    interpolator = LinearInterpolator()
                    start()
                }
    }

    private fun startRipple(animationEndRunnable: Runnable?) {
        if (eventCancelled) return
        val endRadiusVal = calculateEndRadius()
        cancelAnimations()

        rippleAnimator = AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!ripplePersistent) {
                        radius = 0f
                        paint.alpha = rippleAlpha
                        invalidate()
                    }
                    if ((animationEndRunnable != null) && rippleDelayClick) {
                        animationEndRunnable.run()
                    }
                    childView?.isPressed = false
                }
            })

            val ripple = ObjectAnimator.ofFloat(
                this@MaterialRippleEffect, radiusProperty, radius, endRadiusVal,
            ).apply {
                duration = rippleDuration.toLong()
                interpolator = DecelerateInterpolator()
            }

            val fadeExtraDelay = resources.getInteger(R.integer.mrl_fade_extra_delay)
            val fade =
                ObjectAnimator.ofInt(this@MaterialRippleEffect, paintAlphaProperty, rippleAlpha, 0)
                    .apply {
                        duration = rippleFadeDuration.toLong()
                        interpolator = AccelerateInterpolator()
                        startDelay = (rippleDuration - rippleFadeDuration - fadeExtraDelay).toLong()
                    }

            if (ripplePersistent) {
                play(ripple)
            } else if (radius > endRadiusVal) {
                fade.startDelay = 0
                play(fade)
            } else {
                playTogether(ripple, fade)
            }
            start()
        }
    }

    private fun cancelAnimations() {
        rippleAnimator?.apply {
            cancel()
            removeAllListeners()
        }
        hoverAnimator?.cancel()
    }

    private fun calculateEndRadius(): Float {
        val halfWidth = width / 2
        val halfHeight = height / 2
        val radiusX =
            if (halfWidth > currentCoordinates.x) width - currentCoordinates.x else currentCoordinates.x
        val radiusY =
            if (halfHeight > currentCoordinates.y) height - currentCoordinates.y else currentCoordinates.y
        return sqrt(radiusX.toDouble().pow(2.0) + radiusY.toDouble().pow(2.0)).toFloat() * 1.2f
    }

    private val isInScrollingContainer: Boolean
        get() {
            var p = parent
            while (p is ViewGroup) {
                if (p.shouldDelayChildPressedState()) return true
                p = p.parent
            }
            return false
        }

    private fun findParentAdapterView(): AdapterView<*>? {
        if (parentAdapter != null) return parentAdapter
        var current = parent
        while (current != null) {
            if (current is AdapterView<*>) {
                parentAdapter = current
                return current
            }
            current = current.parent
        }
        return null
    }

    private fun updatePositionInAdapter() {
        if (rippleInAdapter) {
            positionInAdapter =
                findParentAdapterView()?.getPositionForView(this) ?: AdapterView.INVALID_POSITION
        }
    }

    private fun adapterPositionChanged(): Boolean {
        if (!rippleInAdapter) return false
        val adapter = findParentAdapterView() ?: return false
        val newPosition = adapter.getPositionForView(this)
        val changed = newPosition != positionInAdapter
        positionInAdapter = newPosition
        if (changed) {
            cancelPressedEvent()
            cancelAnimations()
            childView?.isPressed = false
            radius = 0f
        }
        return changed
    }

    private fun findClickableViewInChild(view: View, x: Int, y: Int): Boolean {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val rect = Rect()
                child.getHitRect(rect)
                if (rect.contains(x, y) && findClickableViewInChild(
                        child, x - rect.left, y - rect.top,
                    )
                ) {
                    return true
                }
            }
        } else if (view !== childView) {
            return view.isEnabled && (view.isClickable || view.isLongClickable || view.isFocusableInTouchMode)
        }
        return view.isFocusableInTouchMode
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bounds.set(0, 0, w, h)
        rippleBackground?.bounds = bounds
        updateClipPath()
    }

    private fun updateClipPath() {
        clipPath.reset()
        if (rippleRoundedCorners > 0) {
            clipRect.set(0f, 0f, width.toFloat(), height.toFloat())
            clipPath.addRoundRect(
                clipRect, rippleRoundedCorners, rippleRoundedCorners, Path.Direction.CW,
            )
        }
    }

    override fun isInEditMode(): Boolean = true

    override fun draw(canvas: Canvas) {
        val positionChanged = adapterPositionChanged()
        if (positionChanged) {
            super.draw(canvas)
            return
        }

        if (rippleRoundedCorners > 0) {
            canvas.withClip(clipPath) {
                drawInternal(this)
            }
        } else {
            drawInternal(canvas)
        }
    }

    private fun drawInternal(canvas: Canvas) {
        if (rippleOverlay) {
            rippleBackground?.draw(canvas)
            super.draw(canvas)
            drawRipple(canvas)
        } else {
            rippleBackground?.draw(canvas)
            drawRipple(canvas)
            super.draw(canvas)
        }
    }

    private fun drawRipple(canvas: Canvas) {
        if (radius <= 0) return
        canvas.drawCircle(
            currentCoordinates.x.toFloat(), currentCoordinates.y.toFloat(), radius, paint,
        )
    }

    override fun performClick(): Boolean {
        if (childView?.performClick() == true) return true
        return super.performClick()
    }

    fun setRippleBackground(color: Int) {
        rippleBackground = color.toDrawable()
    }

    fun performRipple() {
        currentCoordinates.set(width / 2, height / 2)
        startRipple(null)
    }

    fun performRipple(anchor: Point) {
        currentCoordinates.set(anchor.x, anchor.y)
        startRipple(null)
    }

    private val radiusProperty =
        object : Property<MaterialRippleEffect, Float>(Float::class.java, "radius") {
            override fun get(`object`: MaterialRippleEffect): Float = `object`.radius
            override fun set(`object`: MaterialRippleEffect, value: Float) {
                `object`.radius = value
            }
        }

    private val paintAlphaProperty =
        object : Property<MaterialRippleEffect, Int>(Int::class.java, "paintAlpha") {
            override fun get(`object`: MaterialRippleEffect): Int = `object`.paint.alpha
            override fun set(`object`: MaterialRippleEffect, value: Int) {
                `object`.paint.alpha = value
                `object`.invalidate()
            }
        }

    /*
     * Internal Helpers
     */
    private inner class PerformClickEvent : Runnable {
        override fun run() {
            if (hasPerformedLongPress) return
            val parentView = parent

            if (parentView is AdapterView<*>) {
                if (childView?.performClick() == false) {
                    clickAdapterView(parentView)
                }
            } else if (rippleInAdapter) {
                findParentAdapterView()?.let { clickAdapterView(it) }
            } else {
                childView?.performClick()
            }
        }

        private fun clickAdapterView(parent: AdapterView<*>) {
            val position = parent.getPositionForView(this@MaterialRippleEffect)
            val itemId = parent.adapter?.getItemId(position) ?: 0L
            if (position != AdapterView.INVALID_POSITION) {
                parent.performItemClick(this@MaterialRippleEffect, position, itemId)
            }
        }
    }

    private inner class PressedEvent(private val event: MotionEvent?) : Runnable {
        override fun run() {
            repressed = false
            childView?.apply {
                isLongClickable = false
                onTouchEvent(event)
                isPressed = true
            }
            if (rippleHover) startHover()
        }
    }

    class RippleBuilder(private val child: View) {
        private val context: Context = child.context

        private var rippleColor: Int
        private var rippleOverlay: Boolean
        private var rippleHover: Boolean
        private var rippleDiameter: Float
        private var rippleDuration: Int
        private var rippleAlpha: Float
        private var rippleDelayClick: Boolean
        private var rippleFadeDuration: Int
        private var ripplePersistent: Boolean
        private var rippleBackgroundColor: Int
        private var rippleInAdapter: Boolean
        private var rippleRoundedCorner: Float

        init {
            val res = context.resources
            rippleColor = ResourcesCompat.getColor(res, R.color.mrl_default_color, null)
            rippleOverlay = res.getBoolean(R.bool.mrl_default_ripple_overlay)
            rippleHover = res.getBoolean(R.bool.mrl_default_hover)
            rippleDiameter =
                res.getDimension(R.dimen.mrl_default_diameter) / res.displayMetrics.density
            rippleDuration = res.getInteger(R.integer.mrl_default_duration)
            rippleAlpha = res.getFloatCompat(R.dimen.mrl_default_alpha)
            rippleDelayClick = res.getBoolean(R.bool.mrl_default_delay_click)
            rippleFadeDuration = res.getInteger(R.integer.mrl_default_fade_duration)
            ripplePersistent = res.getBoolean(R.bool.mrl_default_persistent)
            rippleBackgroundColor =
                ResourcesCompat.getColor(res, R.color.mrl_default_background, null)
            rippleInAdapter = res.getBoolean(R.bool.mrl_default_search_adapter)
            rippleRoundedCorner =
                res.getDimension(R.dimen.mrl_default_rounded_corners) / res.displayMetrics.density
        }

        fun rippleColor(color: Int) = apply { rippleColor = color }
        fun rippleOverlay(overlay: Boolean) = apply { rippleOverlay = overlay }
        fun rippleHover(hover: Boolean) = apply { rippleHover = hover }
        fun rippleDiameterDp(diameterDp: Int) = apply { rippleDiameter = diameterDp.toFloat() }
        fun rippleDuration(duration: Int) = apply { rippleDuration = duration }
        fun rippleAlpha(alpha: Float) = apply { rippleAlpha = alpha }
        fun rippleDelayClick(delayClick: Boolean) = apply { rippleDelayClick = delayClick }
        fun rippleFadeDuration(fadeDuration: Int) = apply { rippleFadeDuration = fadeDuration }
        fun ripplePersistent(persistent: Boolean) = apply { ripplePersistent = persistent }
        fun rippleBackground(color: Int) = apply { rippleBackgroundColor = color }
        fun rippleInAdapter(inAdapter: Boolean) = apply { rippleInAdapter = inAdapter }
        fun rippleRoundedCorners(radiusDp: Int) =
            apply { rippleRoundedCorner = radiusDp.toFloat() }

        fun create(): MaterialRippleEffect {
            val layout = MaterialRippleEffect(context).apply {
                rippleColor = this@RippleBuilder.rippleColor
                rippleAlpha = (255 * this@RippleBuilder.rippleAlpha).toInt()
                rippleDelayClick = this@RippleBuilder.rippleDelayClick
                rippleDiameter = context.resources.dpToPx(this@RippleBuilder.rippleDiameter).toInt()
                rippleDuration = this@RippleBuilder.rippleDuration
                rippleFadeDuration = this@RippleBuilder.rippleFadeDuration
                rippleHover = this@RippleBuilder.rippleHover
                ripplePersistent = this@RippleBuilder.ripplePersistent
                rippleOverlay = this@RippleBuilder.rippleOverlay
                rippleBackground = this@RippleBuilder.rippleBackgroundColor.toDrawable()
                rippleInAdapter = this@RippleBuilder.rippleInAdapter
                rippleRoundedCorners =
                    context.resources.dpToPx(this@RippleBuilder.rippleRoundedCorner)
            }

            val params = child.layoutParams
            val parent = child.parent as ViewGroup?
            val index = parent?.indexOfChild(child) ?: 0

            check(parent !is MaterialRippleEffect) { "View already has a MaterialRippleEffect parent" }

            parent?.removeView(child)
            layout.addView(
                child, ViewGroupLayoutParams(
                    ViewGroupLayoutParams.MATCH_PARENT, ViewGroupLayoutParams.MATCH_PARENT
                )
            )
            parent?.addView(layout, index, params)

            return layout
        }
    }

    companion object {
        fun on(view: View): RippleBuilder = RippleBuilder(view)

        private fun Resources.dpToPx(dp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics
            )
        }

        private fun Resources.getFloatCompat(id: Int): Float {
            val outValue = TypedValue()
            getValue(id, outValue, true)
            return outValue.float
        }
    }
}

fun View.materialRipple(init: RippleBuilder.() -> Unit = {}): MaterialRippleEffect {
    return MaterialRippleEffect.on(this).apply(init).create()
}