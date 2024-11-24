package com.tera.custom_progressbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class ProgressBarCircle(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributesSet: AttributeSet?, defStyleAttr: Int) :
            this(context, attributesSet, defStyleAttr, 0)

    constructor(context: Context, attributesSet: AttributeSet?) :
            this(context, attributesSet, 0)

    constructor(context: Context) : this(context, null)

    companion object {
        const val TEXT_SIZE = 37
        const val TEXT_COLOR = Color.BLACK
        const val MAX = 20
        const val DASH_COLOR_ACT = Color.BLACK
        const val DASH_COLOR_NO_ACT = -3683893
    }

    private val paintCircle = Paint()
    private val paintDash = Paint()
    private val paintText = Paint()

    private var mRadius = 150f
    private var mCenterX = 0f
    private var mCenterY = 0f
    private val mRect = RectF()
    private var mDensity = 0f
    private var mOffset = 0f

    // Attributes
    private var mCircleStroke = 0f
    private var mMax = MAX
    private var mValue = 0
    private var mPercent = false

    private var mDashWidth = 0f
    private var mDashHeight = 0f
    private var mDashRadius = 0f
    private var mDashColorActive = DASH_COLOR_ACT
    private var mDashColorInactive = DASH_COLOR_NO_ACT

    private var mText: String? = null
    private var mTextSize = TEXT_SIZE.toFloat()
    private var mTextColor = TEXT_COLOR
    private var mFontFamily: Typeface? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBarCircle, 0, defStyleRes)
        // Circle line thickness
        mCircleStroke =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_circleStroke, 0).toFloat()

        mMax = a.getInt(R.styleable.ProgressBarCircle_max, MAX)
        mValue = a.getInt(R.styleable.ProgressBarCircle_value, 0)
        mPercent = a.getBoolean(R.styleable.ProgressBarCircle_showPercent, false)

        mDashWidth = a.getDimensionPixelSize(R.styleable.ProgressBarCircle_dashWidth, 8).toFloat()
        mDashHeight =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_dashHeight, 25).toFloat()
        mDashRadius = a.getDimensionPixelSize(R.styleable.ProgressBarCircle_dashRadius, 0).toFloat()
        mDashColorActive = a.getColor(R.styleable.ProgressBarCircle_dashColorActive, DASH_COLOR_ACT)
        mDashColorInactive =
            a.getColor(R.styleable.ProgressBarCircle_dashColorInactive, DASH_COLOR_NO_ACT)

        mText = a.getString(R.styleable.ProgressBarCircle_text)
        mTextSize =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_textSize, TEXT_SIZE).toFloat()
        mTextColor = a.getColor(R.styleable.ProgressBarCircle_textColor, TEXT_COLOR)
        mFontFamily = a.getFont(R.styleable.ProgressBarCircle_fontFamily)
        a.recycle()

        initPaints()
        getDensity()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mCircleStroke > 0)
            canvas.drawCircle(mCenterX, mCenterY, mRadius, paintCircle)

        drawDashRound(canvas)
        drawText(canvas)
//        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintCircle)
    }

    private fun drawDashRound(canvas: Canvas) {
        val r = mDashRadius
        val x1 = mCenterX - mDashWidth / 2
        val x2 = mCenterX + mDashWidth / 2
        val y1: Float = if (mCircleStroke == 0f)
            mCenterY - mRadius + mOffset
        else
            mCenterY - mRadius + mOffset + mCircleStroke + 5
        val y2 = mCenterY - mRadius + mDashHeight + (mOffset + mCircleStroke)
        var angle: Float
        val dA = 360 / mMax.toFloat()
        canvas.save()
        for (i in 0..<mMax) {
            angle = i * dA
            if (i < mValue) paintDash.color = mDashColorActive
            else paintDash.color = mDashColorInactive
            canvas.save()
            canvas.rotate(angle, mCenterX, mCenterY)
            canvas.drawRoundRect(x1, y1, x2, y2, r, r, paintDash)
            canvas.restore()
        }
        canvas.restore()
    }

    private fun drawText(canvas: Canvas) {
        if (mText == null) return
        val rect = Rect()
        paintText.getTextBounds(mText, 0, mText!!.length, rect)
        val x = rect.exactCenterX()
        val y = rect.exactCenterY()
        canvas.drawText(mText!!, mCenterX - x, mCenterY - y, paintText)
    }

    private fun initPaints() {
        paintCircle.color = Color.GRAY
        paintCircle.strokeWidth = mCircleStroke
        paintCircle.style = Paint.Style.STROKE

        paintText.color = mTextColor
        paintText.textSize = mTextSize
        paintText.typeface = mFontFamily

        paintDash.color = DASH_COLOR_NO_ACT
    }

    // Screen density
    private fun getDensity() {
        val screen = resources.displayMetrics
        mDensity = screen.density
        mOffset = (mDensity * 2)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minWidth = suggestedMinimumWidth
        val minHeight = suggestedMinimumHeight

        val wIndicator = (mRadius * 2).toInt()

        val desiredWidth = max(minWidth, wIndicator)
        val desiredHeight = max(minHeight, wIndicator)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w > h) {
            mRadius = ((h) / 2 - mCircleStroke / 2) - mOffset
            val dW = (w - h) / 2
            mCenterX = mRadius + dW + mCircleStroke / 2 + mOffset
            mCenterY = mRadius + mCircleStroke / 2 + mOffset
        } else {
            mRadius = (w / 2 - mCircleStroke / 2) - mOffset
            val dY = (h - w) / 2
            mCenterX = mRadius + mCircleStroke / 2 + mOffset
            mCenterY = mRadius + dY + mCircleStroke / 2 + mOffset
        }

        val wIndicator = mRadius * 2

        mRect.left = (w - wIndicator)
        mRect.top = (h - wIndicator)
        mRect.right = mRect.left + wIndicator
        mRect.bottom = mRect.top + wIndicator
    }

    // Set text
    private fun setNewText(text: String) {
        if (mPercent) {
            if (text.toIntOrNull() != null) {
                if (mMax == 0) mMax = MAX
                var percent = (mValue * 100 / mMax).toString()
                percent = "$percent%"
                mText = percent
            } else mText = text
        } else mText = text
        invalidate()
    }

    var text: String
        get() = mText.toString()
        set(value) {
            setNewText(value)
        }
    var max: Int
        get() = mMax
        set(value) {
            mMax = value
            invalidate()
        }
    var value: Int
        get() = mValue
        set(value) {
            mValue = value
            invalidate()
        }
    var dashColorActive: Int
        get() = mDashColorActive
        set(value) {
            mDashColorActive = value
            invalidate()
        }
    var dashColorInactive: Int
        get() = mDashColorInactive
        set(value) {
            mDashColorInactive = value
            invalidate()
        }
    var fontFamily: Typeface?
        get() = mFontFamily
        set(value) {
            paintText.typeface = value
            invalidate()
        }

}