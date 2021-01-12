package com.hamedrahimvand.barcodex.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.hamedrahimvand.barcodex.R


class DarkFrame @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mTransparentPaint: Paint? = null
    private var mSemiBlackPaint: Paint? = null
    private val mPath = Path()

    init {
        initPaints()
    }

    private var centerOfCanvas: Point? = null

    private fun initPaints() {
        mTransparentPaint = Paint()
        mTransparentPaint!!.color = Color.TRANSPARENT
        mTransparentPaint!!.strokeWidth = 10F
        mTransparentPaint!!.isAntiAlias = true

        mSemiBlackPaint = Paint()
        mSemiBlackPaint!!.color = Color.TRANSPARENT
        mSemiBlackPaint!!.strokeWidth = 10F
        mSemiBlackPaint!!.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (centerOfCanvas == null)
            centerOfCanvas = Point(width / 2, height / 2)
        val rectW = width / 2
        val rectH = width / 2
        val left = (centerOfCanvas!!.x - rectW / 2).toFloat()
        val top = (centerOfCanvas!!.y - rectH / 2).toFloat()
        val right = (centerOfCanvas!!.x + rectW / 2).toFloat()
        val bottom = (centerOfCanvas!!.y + rectH / 2).toFloat()

        mPath.reset()
        mPath.addRect(left, top, right, bottom, Path.Direction.CW)
        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD

        mTransparentPaint?.let {
            canvas.drawCircle(
                (canvas.width / 2).toFloat(), (canvas.height / 2).toFloat(), 550F,
                it
            )
        }

        mSemiBlackPaint?.let { canvas.drawPath(mPath, it) }
        canvas.clipPath(mPath)
        canvas.drawColor(ContextCompat.getColor(context, R.color.dark_frame_color))
    }
}