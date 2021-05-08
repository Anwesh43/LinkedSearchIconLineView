package com.example.searchiconlineview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.app.Activity
import android.content.Context

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
