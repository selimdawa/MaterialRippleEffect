@file:Suppress("RemoveRedundantQualifierName")

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
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
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
import kotlin.math.pow
import kotlin.math.sqrt

@Suppress("unused")
class MaterialRippleEffect @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = Rect()

    private var rippleColor = 0
    private var rippleOverlay = false
    private var rippleHover = false
    private var rippleDiameter = 0
    private var rippleDuration = 0
    private var rippleAlpha = 0
    private var rippleDelayClick = false
    private var rippleFadeDuration = 0
    private var ripplePersistent = false
    private var rippleBackground: Drawable? = null
    private var rippleInAdapter = false
    private var rippleRoundedCorners = 0f

    private var radius = 0f
    private var parentAdapter: AdapterView<*>? = null
    private var childView: View? = null

    private var rippleAnimator: AnimatorSet? = null
    private var hoverAnimator: ObjectAnimator? = null

    private val currentCoordinates = Point()
    private val previousCoordinates = Point()

    private var layerType = 0
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
            return super.onDown(e)
        }
    }

    private val gestureDetector = GestureDetector(context, longClickListener)

    init {
        setWillNotDraw(false)

        val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialRippleLayout)
        rippleColor = a.getColor(R.styleable.MaterialRippleLayout_mrl_rippleColor, DEFAULT_COLOR)
        rippleDiameter = a.getDimensionPixelSize(
            R.styleable.MaterialRippleLayout_mrl_rippleDimension,
            dpToPx(resources, DEFAULT_DIAMETER_DP).toInt()
        )
        rippleOverlay =
            a.getBoolean(R.styleable.MaterialRippleLayout_mrl_rippleOverlay, DEFAULT_RIPPLE_OVERLAY)
        rippleHover = a.getBoolean(R.styleable.MaterialRippleLayout_mrl_rippleHover, DEFAULT_HOVER)
        rippleDuration =
            a.getInt(R.styleable.MaterialRippleLayout_mrl_rippleDuration, DEFAULT_DURATION)
        rippleAlpha = (255 * a.getFloat(
            R.styleable.MaterialRippleLayout_mrl_rippleAlpha, DEFAULT_ALPHA
        )).toInt()
        rippleDelayClick =
            a.getBoolean(R.styleable.MaterialRippleLayout_mrl_rippleDelayClick, DEFAULT_DELAY_CLICK)
        rippleFadeDuration = a.getInteger(
            R.styleable.MaterialRippleLayout_mrl_rippleFadeDuration, DEFAULT_FADE_DURATION
        )
        rippleBackground = ColorDrawable(
            a.getColor(
                R.styleable.MaterialRippleLayout_mrl_rippleBackground, DEFAULT_BACKGROUND
            )
        )
        ripplePersistent =
            a.getBoolean(R.styleable.MaterialRippleLayout_mrl_ripplePersistent, DEFAULT_PERSISTENT)
        rippleInAdapter = a.getBoolean(
            R.styleable.MaterialRippleLayout_mrl_rippleInAdapter, DEFAULT_SEARCH_ADAPTER
        )
        rippleRoundedCorners = a.getDimensionPixelSize(
            R.styleable.MaterialRippleLayout_mrl_rippleRoundedCorners, DEFAULT_ROUNDED_CORNERS
        ).toFloat()
        a.recycle()

        paint.color = rippleColor
        paint.alpha = rippleAlpha
        enableClipPathSupportIfNecessary()
    }

    override fun addView(child: View, index: Int, params: android.view.ViewGroup.LayoutParams?) {
        check(childCount <= 0) { "MaterialRippleLayout can host only one child" }
        childView = child
        super.addView(child, index, params)
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        checkNotNull(childView) { "MaterialRippleLayout must have a child view to handle clicks" }
        childView?.setOnClickListener(onClickListener)
    }

    override fun setOnLongClickListener(onClickListener: OnLongClickListener?) {
        checkNotNull(childView) { "MaterialRippleLayout must have a child view to handle clicks" }
        childView?.setOnLongClickListener(onClickListener)
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
                        ViewConfiguration.getPressedStateDuration().toLong()
                    )
                }
                if (isEventInBounds) {
                    startRipple(pendingClickEvent)
                } else if (!rippleHover) {
                    setRadius(0f)
                }
                if (!rippleDelayClick && isEventInBounds) {
                    pendingClickEvent?.run()
                }
                cancelPressedEvent()
            }

            MotionEvent.ACTION_DOWN -> {
                setPositionInAdapter()
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
                if (rippleHover) {
                    if (!repressed) startRipple(null)
                } else {
                    child.isPressed = false
                }
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
                    duration = HOVER_DURATION
                    interpolator = LinearInterpolator()
                    start()
                }
    }

    private fun startRipple(animationEndRunnable: Runnable?) {
        if (eventCancelled) return
        val endRadiusVal = endRadius
        cancelAnimations()

        rippleAnimator = AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!ripplePersistent) {
                        setRadius(0f)
                        setRippleAlpha(rippleAlpha)
                    }
                    if (animationEndRunnable != null && rippleDelayClick) {
                        animationEndRunnable.run()
                    }
                    childView?.isPressed = false
                }
            })

            val ripple = ObjectAnimator.ofFloat(
                this@MaterialRippleEffect, radiusProperty, radius, endRadiusVal
            ).apply {
                duration = rippleDuration.toLong()
                interpolator = DecelerateInterpolator()
            }

            val fade =
                ObjectAnimator.ofInt(this@MaterialRippleEffect, circleAlphaProperty, rippleAlpha, 0)
                    .apply {
                        duration = rippleFadeDuration.toLong()
                        interpolator = AccelerateInterpolator()
                        startDelay =
                            (rippleDuration - rippleFadeDuration - FADE_EXTRA_DELAY).toLong()
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

    private val endRadius: Float
        get() {
            val halfWidth = width / 2
            val halfHeight = height / 2
            val radiusX =
                (if (halfWidth > currentCoordinates.x) width - currentCoordinates.x else currentCoordinates.x).toFloat()
            val radiusY =
                (if (halfHeight > currentCoordinates.y) height - currentCoordinates.y else currentCoordinates.y).toFloat()
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

    private fun findParentAdapterView(): AdapterView<*> {
        parentAdapter?.let { return it }
        var current = parent
        while (current != null) {
            if (current is AdapterView<*>) {
                parentAdapter = current
                return current
            }
            current = current.parent
        }
        throw RuntimeException("Could not find a parent AdapterView")
    }

    private fun setPositionInAdapter() {
        if (rippleInAdapter) {
            positionInAdapter = findParentAdapterView().getPositionForView(this)
        }
    }

    private fun adapterPositionChanged(): Boolean {
        if (rippleInAdapter) {
            val newPosition = findParentAdapterView().getPositionForView(this)
            val changed = newPosition != positionInAdapter
            positionInAdapter = newPosition
            if (changed) {
                cancelPressedEvent()
                cancelAnimations()
                childView?.isPressed = false
                setRadius(0f)
            }
            return changed
        }
        return false
    }

    private fun findClickableViewInChild(view: View, x: Int, y: Int): Boolean {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val rect = Rect()
                child.getHitRect(rect)
                if (rect.contains(x, y) && findClickableViewInChild(
                        child, x - rect.left, y - rect.top
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
    }

    override fun isInEditMode(): Boolean = true

    /*
     * Drawing
     */
    override fun draw(canvas: Canvas) {
        val positionChanged = adapterPositionChanged()
        if (rippleOverlay) {
            if (!positionChanged) rippleBackground?.draw(canvas)
            super.draw(canvas)
            if (!positionChanged) {
                if (rippleRoundedCorners != 0f) {
                    val clipPath = Path()
                    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                    clipPath.addRoundRect(
                        rect, rippleRoundedCorners, rippleRoundedCorners, Path.Direction.CW
                    )
                    canvas.clipPath(clipPath)
                }
                canvas.drawCircle(
                    currentCoordinates.x.toFloat(), currentCoordinates.y.toFloat(), radius, paint
                )
            }
        } else {
            if (!positionChanged) {
                rippleBackground?.draw(canvas)
                canvas.drawCircle(
                    currentCoordinates.x.toFloat(), currentCoordinates.y.toFloat(), radius, paint
                )
            }
            super.draw(canvas)
        }
    }

    /*
     * Accessor
     */
    fun setRippleColor(rippleColor: Int) {
        this.rippleColor = rippleColor
        paint.color = rippleColor
        paint.alpha = rippleAlpha
        invalidate()
    }

    fun setRippleOverlay(rippleOverlay: Boolean) {
        this.rippleOverlay = rippleOverlay
    }

    fun setRippleDiameter(rippleDiameter: Int) {
        this.rippleDiameter = rippleDiameter
    }

    fun setRippleDuration(rippleDuration: Int) {
        this.rippleDuration = rippleDuration
    }

    fun setRippleBackground(color: Int) {
        rippleBackground = ColorDrawable(color).apply { bounds = bounds }
        invalidate()
    }

    fun setRippleHover(rippleHover: Boolean) {
        this.rippleHover = rippleHover
    }

    fun setRippleDelayClick(rippleDelayClick: Boolean) {
        this.rippleDelayClick = rippleDelayClick
    }

    fun setRippleFadeDuration(rippleFadeDuration: Int) {
        this.rippleFadeDuration = rippleFadeDuration
    }

    fun setRipplePersistent(ripplePersistent: Boolean) {
        this.ripplePersistent = ripplePersistent
    }

    fun setRippleInAdapter(rippleInAdapter: Boolean) {
        this.rippleInAdapter = rippleInAdapter
    }

    fun setRippleRoundedCorners(rippleRoundedCorner: Int) {
        this.rippleRoundedCorners = rippleRoundedCorner.toFloat()
        enableClipPathSupportIfNecessary()
    }

    fun setDefaultRippleAlpha(alpha: Float) {
        this.rippleAlpha = (255 * alpha).toInt()
        paint.alpha = rippleAlpha
        invalidate()
    }

    fun performRipple() {
        currentCoordinates.set(width / 2, height / 2)
        startRipple(null)
    }

    fun performRipple(anchor: Point) {
        currentCoordinates.set(anchor.x, anchor.y)
        startRipple(null)
    }

    private fun enableClipPathSupportIfNecessary() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (rippleRoundedCorners != 0f) {
                setLayerType(LAYER_TYPE_SOFTWARE, null)
            } else {
                setLayerType(layerType, null)
            }
        }
    }

    fun getRippleAlpha(): Int = paint.alpha

    fun setRippleAlpha(rippleAlpha: Int) {
        paint.alpha = rippleAlpha
        invalidate()
    }

    fun setRadius(radius: Float) {
        this.radius = radius
        invalidate()
    }

    /*
     * Animations
     */
    private val radiusProperty =
        object : Property<MaterialRippleEffect, Float>(Float::class.java, "radius") {
            override fun get(`object`: MaterialRippleEffect): Float = `object`.radius
            override fun set(`object`: MaterialRippleEffect, value: Float) {
                `object`.setRadius(value)
            }
        }

    private val circleAlphaProperty =
        object : Property<MaterialRippleEffect, Int>(Int::class.java, "rippleAlpha") {
            override fun get(`object`: MaterialRippleEffect): Int = `object`.getRippleAlpha()
            override fun set(`object`: MaterialRippleEffect, value: Int) {
                `object`.setRippleAlpha(value)
            }
        }

    /*
     * Helper
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
                clickAdapterView(findParentAdapterView())
            } else {
                childView?.performClick()
            }
        }

        fun clickAdapterView(parent: AdapterView<*>) {
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

    /*
  * Builder
  */
    class RippleBuilder(private val child: View) {
        private val context: Context = child.context

        private var rippleColor: Int = DEFAULT_COLOR
        private var rippleOverlay: Boolean = DEFAULT_RIPPLE_OVERLAY
        private var rippleHover: Boolean = DEFAULT_HOVER
        private var rippleDiameter: Float = DEFAULT_DIAMETER_DP
        private var rippleDuration: Int = DEFAULT_DURATION
        private var rippleAlpha: Float = DEFAULT_ALPHA
        private var rippleDelayClick: Boolean = DEFAULT_DELAY_CLICK
        private var rippleFadeDuration: Int = DEFAULT_FADE_DURATION
        private var ripplePersistent: Boolean = DEFAULT_PERSISTENT
        private var rippleBackground: Int = DEFAULT_BACKGROUND
        private var rippleSearchAdapter: Boolean = DEFAULT_SEARCH_ADAPTER
        private var rippleRoundedCorner: Float = DEFAULT_ROUNDED_CORNERS.toFloat()

        fun rippleColor(color: Int): RippleBuilder = apply { this.rippleColor = color }

        fun rippleOverlay(overlay: Boolean): RippleBuilder = apply { this.rippleOverlay = overlay }

        fun rippleHover(hover: Boolean): RippleBuilder = apply { this.rippleHover = hover }

        fun rippleDiameterDp(diameterDp: Int): RippleBuilder =
            apply { this.rippleDiameter = diameterDp.toFloat() }

        fun rippleDuration(duration: Int): RippleBuilder = apply { this.rippleDuration = duration }

        fun rippleAlpha(alpha: Float): RippleBuilder = apply { this.rippleAlpha = alpha }

        fun rippleDelayClick(delayClick: Boolean): RippleBuilder =
            apply { this.rippleDelayClick = delayClick }

        fun rippleFadeDuration(fadeDuration: Int): RippleBuilder =
            apply { this.rippleFadeDuration = fadeDuration }

        fun ripplePersistent(persistent: Boolean): RippleBuilder =
            apply { this.ripplePersistent = persistent }

        fun rippleBackground(color: Int): RippleBuilder = apply { this.rippleBackground = color }

        fun rippleInAdapter(inAdapter: Boolean): RippleBuilder =
            apply { this.rippleSearchAdapter = inAdapter }

        fun rippleRoundedCorners(radiusDp: Int): RippleBuilder =
            apply { this.rippleRoundedCorner = radiusDp.toFloat() }

        fun create(): MaterialRippleEffect {
            val layout = MaterialRippleEffect(context).apply {
                setRippleColor(rippleColor)
                setDefaultRippleAlpha(rippleAlpha.toFloat())
                setRippleDelayClick(rippleDelayClick)

                setRippleDiameter(dpToPx(context.resources, rippleDiameter.toFloat()).toInt())

                setRippleDuration(rippleDuration)
                setRippleFadeDuration(rippleFadeDuration)
                setRippleHover(rippleHover)
                setRipplePersistent(ripplePersistent)
                setRippleOverlay(rippleOverlay)
                setRippleBackground(this@RippleBuilder.rippleBackground)
                setRippleInAdapter(rippleSearchAdapter)

                setRippleRoundedCorners(dpToPx(context.resources, rippleRoundedCorner).toInt())
            }

            val params = child.layoutParams
            val parent = child.parent as ViewGroup?
            var index = 0

            check(parent !is MaterialRippleEffect) { "MaterialRippleLayout could not be created: parent of the view already is a MaterialRippleLayout" }

            parent?.let {
                index = it.indexOfChild(child)
                it.removeView(child)
            }

            layout.addView(
                child, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            )
            parent?.addView(layout, index, params)

            return layout
        }
    }

    companion object {
        private const val DEFAULT_DURATION = 350
        private const val DEFAULT_FADE_DURATION = 75
        private const val DEFAULT_DIAMETER_DP = 35f
        private const val DEFAULT_ALPHA = 0.2f
        private const val DEFAULT_COLOR = Color.BLACK
        private const val DEFAULT_BACKGROUND = Color.TRANSPARENT
        private const val DEFAULT_HOVER = true
        private const val DEFAULT_DELAY_CLICK = true
        private const val DEFAULT_PERSISTENT = false
        private const val DEFAULT_SEARCH_ADAPTER = false
        private const val DEFAULT_RIPPLE_OVERLAY = false
        private const val DEFAULT_ROUNDED_CORNERS = 0

        private const val FADE_EXTRA_DELAY = 50
        private const val HOVER_DURATION: Long = 2500

        @JvmStatic
        fun on(view: View): RippleBuilder = RippleBuilder(view)

        @JvmStatic
        fun dpToPx(resources: Resources, dp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
            )
        }
    }
}