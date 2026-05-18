package com.estudiante.techscoop.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild2
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat

class NestedWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.webViewStyle
) : WebView(context, attrs, defStyleAttr), NestedScrollingChild2 {

    private val childHelper = NestedScrollingChildHelper(this)
    private var lastY = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedYOffset = 0

    init {
        isNestedScrollingEnabled = true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val event = MotionEvent.obtain(ev)
        val action = ev.actionMasked

        if (action == MotionEvent.ACTION_DOWN) {
            nestedYOffset = 0
        }

        event.offsetLocation(0f, nestedYOffset.toFloat())

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = ev.y.toInt()
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
                performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                var deltaY = lastY - ev.y.toInt()
                if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset, ViewCompat.TYPE_TOUCH)) {
                    deltaY -= scrollConsumed[1]
                    lastY = ev.y.toInt() - scrollOffset[1]
                    event.offsetLocation(0f, (-scrollOffset[1]).toFloat())
                    nestedYOffset += scrollOffset[1]
                }
                lastY = ev.y.toInt()
                val oldY = scrollY
                super.onTouchEvent(event)
                val scrolledDeltaY = scrollY - oldY
                val unconsumedY = deltaY - scrolledDeltaY
                if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, scrollOffset, ViewCompat.TYPE_TOUCH)) {
                    lastY -= scrollOffset[1]
                    event.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedYOffset += scrollOffset[1]
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopNestedScroll(ViewCompat.TYPE_TOUCH)
            }
        }
        super.onTouchEvent(event)
        return true
    }

    override fun startNestedScroll(axes: Int, type: Int) = childHelper.startNestedScroll(axes, type)
    override fun stopNestedScroll(type: Int) = childHelper.stopNestedScroll(type)
    override fun hasNestedScrollingParent(type: Int) = childHelper.hasNestedScrollingParent(type)
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int) =
        childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int) =
        childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
}
