package com.hamedrahimvand.barcodex.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.hamedrahimvand.barcodex.R
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxModel
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxStates
import com.hamedrahimvand.barcodex.utils.dpToPx

/**
 *
 *@author Hamed.Rahimvand
 *@since 4/21/20
 */
class BarcodeBoundingBox : FrameLayout{
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    var barcodeBoundingBoxModelList: List<BarcodeBoundingBoxModel>? = null

    fun init() {
        setWillNotDraw(false)

    }

    val validPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.green3_translucent)
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dpToPx(context, 2f).toFloat()
        pathEffect = CornerPathEffect(dpToPx(context, 0f).toFloat())
        isAntiAlias = true
    }
    val invalidPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.red_translucent)
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dpToPx(context, 2f).toFloat()
        pathEffect = CornerPathEffect(dpToPx(context, 0f).toFloat())
        isAntiAlias = true
    }
    val duplicatePaint = Paint().apply {
//        color = ContextCompat.getColor(context, R.color.red_translucent)
        color = ContextCompat.getColor(context, R.color.green3_translucent)
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dpToPx(context, 2f).toFloat()
        pathEffect = CornerPathEffect(dpToPx(context, 0f).toFloat())
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        barcodeBoundingBoxModelList?.let { barcodeBoundingBoxList ->
            for (barcodeBoundingBoxModel in barcodeBoundingBoxList) {
                barcodeBoundingBoxModel.rect?.let { rect ->
                    val paint = when (barcodeBoundingBoxModel.barcodeBoundingBoxStates) {
                        BarcodeBoundingBoxStates.VALID -> {
                                validPaint
                        }
                        BarcodeBoundingBoxStates.INVALID -> {
                            invalidPaint
                        }
                        BarcodeBoundingBoxStates.DUPLICATE -> {
                             duplicatePaint
                        }
                        else -> {
                            null
                        }
                    }
                    paint?.let { p ->
                        canvas?.drawRect(rect, p)
                    }
                }
            }
        }
    }

    fun drawBoundingBox(barcodeBoundingBoxModelList: List<BarcodeBoundingBoxModel>) {
        clean()
        this.barcodeBoundingBoxModelList = barcodeBoundingBoxModelList
        invalidate()
    }


    fun clean() {
        this.barcodeBoundingBoxModelList = null
        invalidate()
    }
}
