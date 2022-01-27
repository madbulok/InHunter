package com.uzlov.inhunter.ui.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.uzlov.inhunter.R
import java.util.concurrent.Executors

class DraggableLockButton : View {

    private var drawable: Drawable?
    private var drawableResourceId: Int = 0
    private var isDraggable: Boolean = false
    private var isAccepted: Boolean = false

    private val TAG = javaClass.simpleName
    private var lockBitmap: Bitmap?
    private val executor = Executors.newFixedThreadPool(2)
    private val uiHandler = Handler(Looper.getMainLooper())

    private val padding: Float = 0F

    private val paintBackground = Paint().apply {
        color = resources.getColor(R.color.main_background)
        alpha = 150
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        color = resources.getColor(R.color.white)
        textSize = 48F
        isAntiAlias = true
    }

    private val paintButton = Paint().apply {
        color = resources.getColor(R.color.main_background)
        isAntiAlias = true
    }

    interface OnActionListener {
        fun ready()
        fun reject()
        fun accept()
    }

    private var actionListener: OnActionListener? = null

    fun setActionListener(listener: OnActionListener) {
        actionListener = listener
    }

    private var readyTitle: String = ""
    private var startTitle: String = ""
    private var endTitle: String = ""

    private var widthReadyTitleStr: Float = paintText.measureText(readyTitle)
    private var widthStartTitleStr: Float = paintText.measureText(startTitle)
    private var widthEndTitleStr: Float = paintText.measureText(endTitle)

    //    left, int top, int right, int bottom
    private val sizeIcon = 70
    private val sizeOval = 150
    private val paddingIcon = 24

    private val rectIconPosition =
        Rect(
            paddingIcon,
            paddingIcon,
            sizeOval - paddingIcon,
            sizeOval - paddingIcon
        )

    enum class StateButton {
        BUTTON_READY, // кнопка в исходном положении (без закрытия)
        BUTTON_LOCKED, // кнопка активирована
        BUTTON_ACCEPTED_AND_READY // кнопка в исходном положении после того как её закрыли
    }

    private var stateButton: StateButton = StateButton.BUTTON_READY

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context,
        attrs,
        defStyle) {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DraggableLockButton)

        readyTitle = typedArray.getString(R.styleable.DraggableLockButton_lockTitleReady) ?: "Ready!"
        startTitle = typedArray.getString(R.styleable.DraggableLockButton_lockTitle) ?: "Title"
        endTitle = typedArray.getString(R.styleable.DraggableLockButton_lockTitleEnd) ?: "End"

        widthReadyTitleStr = paintText.measureText(readyTitle)
        widthStartTitleStr = paintText.measureText(startTitle)
        widthEndTitleStr = paintText.measureText(endTitle)

        drawableResourceId = typedArray.getResourceId(R.styleable.DraggableLockButton_lockIcon, 0)
        drawable = ResourcesCompat.getDrawable(resources, drawableResourceId, null)

        val tintColor = typedArray.getColor(R.styleable.DraggableLockButton_lockIconTint,
            resources.getColor(android.R.color.white))
        drawable?.setTint(tintColor)

        lockBitmap = drawable?.toBitmap(sizeIcon, sizeIcon, Bitmap.Config.ARGB_8888)!!

        typedArray.recycle()

    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    /*
    * Этот @onMeasure метод означает, что наш Custom View-компонент находится на стадии определения
    * собственного размера. Это очень важный метод, так как в большинстве случаев вам
    * нужно определить специфичный размер для вашего View-компонента,
    * чтобы поместиться на вашем макете.

    При переопределении этого метода,
    * всё что вам нужно сделать, это установить setMeasuredDimension(int width, int height)
    * */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desireWidth = width + paddingLeft + paddingRight
        val desiredHeight = height + paddingTop + paddingBottom

        val resolveWidth = resolveSize(desireWidth, widthMeasureSpec)
        val resolveHeight = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(resolveWidth, resolveHeight)
    }

    private val rectOvalPosition = RectF(
        0 + padding,
        0 + padding,
        sizeOval.toFloat(),
        sizeOval.toFloat()
    )
    private val rectOvalPositionSource  = RectF(rectOvalPosition)

    fun setStateButton(state: StateButton){
        stateButton = state
        if (state == StateButton.BUTTON_ACCEPTED_AND_READY /*|| state == StateButton.BUTTON_READY*/){
            actionListener?.accept()
        }
        invalidate()
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        when (stateButton) {
            StateButton.BUTTON_READY -> {
                canvas?.drawRoundRect(0F + padding,
                    0F + padding,
                    measuredWidth.toFloat() - padding,
                    measuredHeight.toFloat(),
                    125F, 125F, paintButton)

                // ready text
                canvas?.drawText(readyTitle,
                    (measuredWidth - widthReadyTitleStr) / 2,
                    (measuredHeight.toFloat() + paintText.textSize) / 2F,
                    paintText)
            }
            StateButton.BUTTON_LOCKED -> {
                // background
                canvas?.drawRoundRect(0F + padding,
                    0F + padding,
                    measuredWidth.toFloat() - padding,
                    measuredHeight.toFloat(),
                    125F,
                    125F,
                    paintBackground)


                // text
                canvas?.drawText(startTitle,
                    (measuredWidth - widthStartTitleStr) / 2,
                    (measuredHeight.toFloat() + paintText.textSize) / 2F,
                    paintText)

                // button draggable
                rectOvalPosition.bottom = measuredHeight.toFloat()
                canvas?.drawRoundRect(rectOvalPosition, 125F, 125F, paintButton)

                //draw lock icon
                rectIconPosition.bottom = measuredHeight - paddingIcon
                lockBitmap?.let { bitmap ->
                    canvas?.drawBitmap(bitmap,
                        null, rectIconPosition,
                        paintText)
                }
            }
            StateButton.BUTTON_ACCEPTED_AND_READY -> {
                // background
                canvas?.drawRoundRect(0F + padding,
                    0F + padding,
                    measuredWidth.toFloat() - padding,
                    measuredHeight.toFloat(),
                    125F,
                    125F,
                    paintButton
                )
                // text
                canvas?.drawText(endTitle,
                    (measuredWidth - widthEndTitleStr) / 2,
                    (measuredHeight.toFloat() + paintText.textSize) / 2F,
                    paintText)
            }
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val positionX = event?.rawX?.toInt() ?: 0
        val positionY = event?.rawY?.toInt() ?: 0

        when (event?.action) {

            // пользователь коснулся
            MotionEvent.ACTION_DOWN -> {
                isDraggable = rectOvalPosition.right > positionX.toFloat()
                if (stateButton == StateButton.BUTTON_ACCEPTED_AND_READY ||
                        stateButton == StateButton.BUTTON_READY
                ){
                    stateButton = StateButton.BUTTON_LOCKED
                    actionListener?.ready()
                    invalidate()
                }
            }

            // пользователь двигает пальцем по экрану
            MotionEvent.ACTION_MOVE -> {
                if (!isDraggable && !isAccepted) return false

                if (positionX < rectOvalPosition.right + 50 && positionX < measuredWidth - sizeOval/2 && positionX > sizeOval) {
                    rectOvalPosition.right = positionX.toFloat() + sizeOval/2

                    rectIconPosition.left =
                        (positionX - sizeIcon - (sizeOval - sizeIcon) / 2 - paddingIcon + sizeOval/2)
                    rectIconPosition.right = (positionX - (sizeOval - sizeIcon) / 2 + paddingIcon + sizeOval/2)
                    invalidate()
                }
            }

            // пользователь поднял палец с экрана
            MotionEvent.ACTION_UP -> {
                if (!isDraggable && !isAccepted) return false

                if (positionX in 1 until (measuredWidth-padding).toInt()) {
                    // пользователь не дотянул кнопку, нужно анимировать воврат в исходное положение
                    val range = rectOvalPosition.right.toInt() downTo 150
                    executor.execute {
                        for (x in range) {
                            Thread.sleep(1) //remove it. why postDelayed dot work?
                            uiHandler.post {
                                rectOvalPosition.right = x.toFloat()

                                rectIconPosition.left =
                                    (x - sizeIcon - (sizeOval - sizeIcon) / 2 - paddingIcon)
                                rectIconPosition.right =
                                    (x - (sizeOval - sizeIcon) / 2 + paddingIcon)
                                invalidate()
                            }
                        }
                    }

                    actionListener?.reject()


                } else if (positionX > measuredWidth - padding) {
                    // свайп успешен, нужно показать что все ок и снова показать кнопку
                    rectOvalPosition.right = measuredWidth - padding
                    rectIconPosition.left = measuredWidth - (150 / 2 + 70 - 24)
                    rectIconPosition.right = (measuredWidth - 24 - padding).toInt()
                    invalidate()
                    actionListener?.accept()

                    stateButton = StateButton.BUTTON_ACCEPTED_AND_READY

                } else if (positionX < paddingStart && positionX > measuredWidth - paddingEnd) {
                    // пользователь вышел за пределы, нужно ИГНОРИРОВАТЬ

                }
                isDraggable = false
            }
        }
        return true
    }

    fun destroy() {
        executor.shutdown()
        actionListener = null
    }
}