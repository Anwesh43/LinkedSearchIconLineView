package com.example.searchiconlineview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
val colors : Array<Int> = arrayOf(
    "#54EE11",
    "#CC1134",
    "#1122CC",
    "#DEAB12",
    "#56ADFE"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 4
val scGap : Float = 0.02f
val lineSizeFactor1 : Float = 6.9f
val rFactor : Float = 15.8f
val lineSizeFactor2 : Float = 3.9f
val delay : Long = 20
val strokeFactor : Float = 90f
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawSearchIconLine(scale : Float, w : Float, h : Float, paint : Paint) {
    val lineSize1 : Float = Math.min(w, h) / lineSizeFactor1
    val lineSize2 : Float = Math.min(w, h) / lineSizeFactor2
    val r : Float = Math.min(w, h) / rFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val sf4 : Float = sf.divideScale(3, parts)
    save()
    translate(w / 2, h / 2)
    save()
    rotate(-45f * sf3)
    drawLine(0f, 0f, 0f, -lineSize1 * sf1, paint)
    drawArc(RectF(-r, -lineSize1 - r, r, -lineSize1 + r), 90f, 360f * sf2,false, paint)
    restore()
    drawLine(0f, 0f, -lineSize2 * sf4, 0f, paint)
    restore()
}

fun Canvas.drawSILNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.style = Paint.Style.STROKE
    drawSearchIconLine(scale, w, h, paint)
}

class SearchIconLineView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SILNode(var i : Int, val state : State = State()) {

        private var next : SILNode? = null
        private var prev : SILNode? = null

        init {

        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = SILNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSILNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SILNode {
            var curr : SILNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SearchIconLine(var i : Int) {

        private var curr : SILNode = SILNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SearchIconLineView) {

        private val animator : Animator = Animator(view)
        private val sil : SearchIconLine = SearchIconLine(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            sil.draw(canvas, paint)
            animator.animate {
                sil.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sil.startUpdating {
                animator.start()
            }
        }
    }
}
