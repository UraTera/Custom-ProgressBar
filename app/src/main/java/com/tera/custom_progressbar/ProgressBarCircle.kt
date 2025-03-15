package com.tera.custom_progressbar

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.*


class ProgressBarCircle(
    context: Context,
    attrs: AttributeSet?,
    defStyleRes: Int
) : View(context, attrs, defStyleRes) {

    constructor(context: Context, attributesSet: AttributeSet?) :
            this(context, attributesSet, 0)

    constructor(context: Context) : this(context, null)

    companion object {
        const val RADIUS = 131f // 50dp
        const val DASH_HEIGHT = 26 // 10dp
        const val DASH_WIDTH = 8 // 3dp
        const val TEXT_SIZE = 52
        const val TEXT_COLOR = Color.BLACK
        const val MAX = 40
        const val PROGRESS_COLOR = Color.BLACK

        //const val PROGRESS_COLOR = -1086706
        const val GROUND_COLOR = -3683893

        const val ARC_WIDTH = 27 // 10dp
        const val W_COLOR1 = -2306296
        const val W_COLOR2 = Color.RED
        const val W_VALUE1 = 80
        const val W_VALUE2 = 90
    }

    private val mPaintCircle = Paint()
    private val mPaintDash = Paint()
    private val mPaintText = Paint()
    private val mPaintArc = Paint()
    private val mPaintGround = Paint()

    private var mRadius = RADIUS
    private var mRadiusFull = 0f
    private var mRadiusGround = 0f
    private var mCx = 0f
    private var mCy = 0f
    private var mText: String? = null
    private var mDensity = 0f
    private var mOffsetCircle = 0f
    private var mOffsetGround = 0f
    private var mOffsetBlur = 0f

    // Dash Params
    private var mDx1 = 0f
    private var mDx2 = 0f
    private var mDy1 = 0f
    private var mDy2 = 0f

    // Arc, Solid
    private var mShaderArc: Shader? = null
    private var mRectArc = RectF()
    private var mAngleStart = 0f
    private var mAngleEnd = 0f
    private var mTextNew: String? = null

    // Attributes
    private var mCircleStroke = 0f
    private var mMax = 0f
    private var mValue = 0f
    private var mPercentShow = false

    private var mDashWidth = 0f
    private var mDashHeight = 0f
    private var mDashRadius = 0f
    private var mProgressColor = 0 // Цвет прогресса Start
    private var mGroundColor = 0  // Цвет фоновой дуги

    private var mTextSize = 0f
    private var mTextColor = 0
    private var mFontFamily: Typeface? = null

    private var mArcGradientStyle = 0
    private var mProgressStyle = 0
    private var mArcEndColor = 0         // Цвет прогресса End
    private var mArcProgressWidth = 0f   // Толщина линии прогресса
    private var mArcGroundWidth = 0f     // Толщина фоновой дуги
    private var mArcRoundShow = false    // Показать шапку
    private var mArcBlurWidth = 0        // Толщина размытия
    private var mBlurStyle: Blur? = null // Стиль размытия

    private var mWarningShow = false
    private var mWarningColor1 = 0
    private var mWarningColor2 = 0
    private var mWarningValue1 = 0
    private var mWarningValue2 = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBarCircle, 0, defStyleRes)

        mValue = a.getInt(R.styleable.ProgressBarCircle_pbc_value, 0).toFloat()
        mMax = a.getInt(R.styleable.ProgressBarCircle_pbc_valueMax, MAX).toFloat()

        mCircleStroke =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_pbc_circleStroke, 0).toFloat()

        mDashHeight =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_pbc_dashHeight, DASH_HEIGHT).toFloat()
        mDashWidth =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_pbc_dashWidth, DASH_WIDTH).toFloat()
        mDashRadius =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_pbc_dashRadius, 0).toFloat()
        mFontFamily = a.getFont(R.styleable.ProgressBarCircle_pbc_fontFamily)
        mGroundColor = a.getColor(R.styleable.ProgressBarCircle_pbc_groundColor, GROUND_COLOR)

        mPercentShow = a.getBoolean(R.styleable.ProgressBarCircle_pbc_percentShow, true)
        mProgressColor = a.getColor(R.styleable.ProgressBarCircle_pbc_progressColor, PROGRESS_COLOR)
        mProgressStyle = a.getInt(R.styleable.ProgressBarCircle_pbc_progressStyle, 0)

        mTextColor = a.getColor(R.styleable.ProgressBarCircle_pbc_textColor, TEXT_COLOR)
        mTextSize =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_pbc_textSize, TEXT_SIZE).toFloat()

        mArcBlurWidth = a.getDimensionPixelSize(R.styleable.ProgressBarCircle_pbc_arcBlurWidth, 0)
        val blurStyle = a.getInt(R.styleable.ProgressBarCircle_pbc_arcBlurStyle, 0)
        mArcEndColor =
            a.getColor(R.styleable.ProgressBarCircle_pbc_arcEndColor, PROGRESS_COLOR)
        mArcGroundWidth =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_pbc_arcGroundWidth, ARC_WIDTH)
                .toFloat()
        mArcProgressWidth =
            a.getDimensionPixelSize(R.styleable.ProgressBarCircle_pbc_arcProgressWidth, ARC_WIDTH)
                .toFloat()
        mArcRoundShow = a.getBoolean(R.styleable.ProgressBarCircle_pbc_arcRoundShow, false)
        mArcGradientStyle = a.getInt(R.styleable.ProgressBarCircle_pbc_arcGradientStyle, 0)

        mWarningShow = a.getBoolean(R.styleable.ProgressBarCircle_pbc_warningShow, false)
        mWarningColor1 = a.getColor(R.styleable.ProgressBarCircle_pbc_warningColor1, W_COLOR1)
        mWarningColor2 = a.getColor(R.styleable.ProgressBarCircle_pbc_warningColor2, W_COLOR2)
        mWarningValue1 = a.getInt(R.styleable.ProgressBarCircle_pbc_warningValue1, W_VALUE1)
        mWarningValue2 = a.getInt(R.styleable.ProgressBarCircle_pbc_warningValue2, W_VALUE2)

        a.recycle()

        mBlurStyle = if (blurStyle == 0) Blur.NORMAL
        else Blur.SOLID

        initPaints()
        getDensity()
    }

    private fun initPaints() {
        mPaintCircle.color = Color.GRAY
        mPaintCircle.strokeWidth = mCircleStroke
        mPaintCircle.style = Paint.Style.STROKE

        mPaintText.color = mTextColor
        mPaintText.textSize = mTextSize
        mPaintText.typeface = mFontFamily

        mPaintDash.color = GROUND_COLOR
        mPaintDash.isAntiAlias = true

        mPaintArc.color = mProgressColor
        mPaintArc.isAntiAlias = true
        setBlur()

        mPaintGround.strokeWidth = mArcGroundWidth
        mPaintGround.color = mGroundColor
        mPaintGround.isAntiAlias = true
    }

    private fun setBlur() {
        if (mBlurStyle != null && mArcBlurWidth > 0) {
            setLayerType(LAYER_TYPE_SOFTWARE, mPaintArc)
            mPaintArc.setMaskFilter(BlurMaskFilter(mArcBlurWidth.toFloat(), mBlurStyle))
        } else {
            mPaintArc.setMaskFilter(null)
        }
    }

    // Screen density
    private fun getDensity() {
        val screen = resources.displayMetrics
        mDensity = screen.density
        mOffsetCircle = (mDensity * 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mWarningShow)
            setWarningArc()
        if (mCircleStroke > 0)
            canvas.drawCircle(mCx, mCy, mRadius, mPaintCircle)

        if (mProgressStyle == 0) {
            drawDash(canvas)
        } else if (mProgressStyle == 1) {
            canvas.drawCircle(mCx, mCy, mRadiusGround, mPaintGround)
            canvas.save()
            canvas.rotate(-90f, mCx, mCy)
            drawArc(canvas)
            canvas.restore()
        } else {
            canvas.drawCircle(mCx, mCy, mRadiusGround, mPaintGround)
            canvas.save()
            canvas.rotate(-90f, mCx, mCy)
            drawSolid(canvas)
            canvas.restore()
        }
        drawText(canvas)
    }

    // Warning
    private fun setWarningArc() {
        val percent = getPercent()
        if (percent > mWarningValue1 && percent < mWarningValue2) {
            mPaintGround.color = mWarningColor1
        } else if (percent >= mWarningValue2) {
            mPaintGround.color = mWarningColor2
        } else {
            mPaintGround.color = mGroundColor
        }
    }

    // Warning
    private fun setWarningDash() {
        val percent = getPercent()
        if (percent in (mWarningValue1 + 1)..<mWarningValue2) {
            mPaintDash.color = mWarningColor1
        } else if (percent >= mWarningValue2) {
            mPaintDash.color = mWarningColor2
        } else {
            mPaintDash.color = mGroundColor
        }
    }

    // Штрихи Style dash
    private fun drawDash(canvas: Canvas) {
        val r = mDashRadius
        val angle = 360 / mMax
        canvas.save()
        for (i in 0..<mMax.toInt()) {
            if (i < mValue) mPaintDash.color = mProgressColor
            else if (mWarningShow) setWarningDash()
            else mPaintDash.color = mGroundColor
            canvas.drawRoundRect(mDx1, mDy1, mDx2, mDy2, r, r, mPaintDash)
            canvas.rotate(angle, mCx, mCy)
        }
        canvas.restore()
        setText()
    }

    // Дуга прогресса Style arc
    private fun drawArc(canvas: Canvas) {
        val angleEnd = mValue * 360 / mMax
        canvas.drawArc(mRectArc, mAngleStart, angleEnd, false, mPaintArc)
        setText()
    }

    // Дуга прогресса Style solid
    private fun drawSolid(canvas: Canvas) {
        val angleEnd = mValue * 360 / mMax
        canvas.drawArc(mRectArc, 0f, angleEnd, true, mPaintArc)
        setText()
    }

    private fun drawText(canvas: Canvas) {
        val rect = Rect()
        mPaintText.getTextBounds(mText, 0, mText!!.length, rect)
        val x = rect.exactCenterX()
        val y = rect.exactCenterY()
        canvas.drawText(mText!!, mCx - x, mCy - y, mPaintText)
    }

    private fun setText() {
        if (mTextNew == null) {
            if (mPercentShow) {
                mText = "${getPercent()}%"
            } else {
                val str = mValue.toInt().toString()
                mText = str
            }
        } else mText = mTextNew
    }

    private fun getPercent(): Int {
        val percent = (mValue * 100 / mMax).toInt()
        return percent
    }

    // Параметры Dash из onSizeChanged
    private fun setDashParams() {
        mDx1 = mCx - mDashWidth / 2
        mDx2 = mCx + mDashWidth / 2
        mDy1 = if (mCircleStroke == 0f)
            mCy - mRadius + mOffsetCircle
        else
            mCy - mRadius + mOffsetCircle + mCircleStroke + 5
        mDy2 = mCy - mRadius + mDashHeight + (mOffsetCircle + mCircleStroke)
    }

    // Параметры дуги
    private fun setArcParams() {
        mPaintArc.style = Paint.Style.STROKE

        if (mArcProgressWidth > mArcGroundWidth)
            mOffsetGround = (mArcProgressWidth - mArcGroundWidth) / 2
        else {
            mOffsetGround = 0f
            mArcProgressWidth = mArcGroundWidth
        }

        val wArc = if (mArcProgressWidth > mRadius) mRadius
        else mArcProgressWidth
        mPaintArc.strokeWidth = wArc

        val wMax = mRadius * 0.5

        val r = mRadiusFull - wArc / 2 - mArcBlurWidth
        val x1 = mCx - r
        val y1 = mCy - r
        val x2 = mCx + r
        val y2 = mCy + r
        mRectArc = RectF(x1, y1, x2, y2)

        // Cap
        if (wArc > wMax) mArcRoundShow = false
        if (mArcRoundShow)
            mPaintArc.strokeCap = Cap.ROUND

        // Угол начала дуги при включении шапки
        if (mArcRoundShow) {
            mAngleStart = Math.toDegrees(asin(wArc / 2 / r.toDouble())).toFloat()
            mAngleEnd = 360 - mAngleStart * 2
        } else {
            mAngleStart = 0f
            mAngleEnd = 360f
        }

        setGradient()
        //-----------
        // Фон
        mPaintGround.style = Paint.Style.STROKE
        mPaintGround.strokeWidth = mArcGroundWidth
        mRadiusGround = mRadius - mArcGroundWidth / 2 - mOffsetGround
    }

    // Параметры Solid
    private fun setSolidParams() {
        mPaintArc.style = Paint.Style.FILL
        val r = mRadiusFull - mArcBlurWidth
        val x1 = mCx - r
        val y1 = mCy - r
        val x2 = mCx + r
        val y2 = mCy + r
        mRectArc = RectF(x1, y1, x2, y2)

        setGradient()
        // Фон
        mPaintGround.style = Paint.Style.FILL
        mRadiusGround = mRadius
    }

    private fun setGradient() {
        if (mProgressColor != mArcEndColor) {
            if (mArcGradientStyle == 0)
                mShaderArc = SweepGradient(mCx, mCy, mProgressColor, mArcEndColor)
            else
                mShaderArc = RadialGradient(
                    mCx,
                    mCy,
                    mRadius,
                    mProgressColor,
                    mArcEndColor,
                    Shader.TileMode.CLAMP
                )
            mPaintArc.setShader(mShaderArc)
        } else {
            mPaintArc.setShader(null)
            mPaintArc.color = mProgressColor
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wC = ((mRadius + mOffsetBlur) * 2).toInt() + mArcBlurWidth
        setMeasuredDimension(
            resolveSize(wC, widthMeasureSpec),
            resolveSize(wC, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadiusFull = min(w, h) / 2f
        mRadius = mRadiusFull - mOffsetBlur - mArcBlurWidth // 2
        mCx = w / 2f
        mCy = h / 2f

        if (mProgressStyle == 0)
            setDashParams()
        else if (mProgressStyle == 1)
            setArcParams()
        else
            setSolidParams()
    }

    var text: String = ""
        set(value) {
            field = value
            mTextNew = value
            invalidate()
        }
    var valueMax: Float
        get() = mMax
        set(value) {
            mMax = value
            invalidate()
        }
    var value: Float
        get() = mValue
        set(value) {
            mValue = value
            invalidate()
        }
    var progressColor: Int = 0
        set(value) {
            field = value
            mProgressColor = value
            invalidate()
        }
    var groundColor: Int = 0
        set(value) {
            field = value
            mGroundColor = value
            invalidate()
        }
    var fontFamily: Typeface? = Typeface.DEFAULT
        set(value) {
            field = value
            mPaintText.typeface = value
            invalidate()
        }

}