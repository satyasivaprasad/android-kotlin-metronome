package geva.oren.android_kotlin_metronome.views

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import geva.oren.android_kotlin_metronome.R
import kotlinx.android.synthetic.main.rotary_knob_view.view.*

class RotaryKnobView(
    context: Context,
    attrs: AttributeSet
) :
    RelativeLayout(context), GestureDetector.OnGestureListener {
    private val gestureDetector: GestureDetectorCompat
    private var mAngleDown = 0f
    private var mAngleUp = 0f
    private var maxValue = 99
    private var minValue = 0
    var listener: RotaryKnobListener? = null
    var value = 130
    var knobDrawable: Drawable? = null

    interface RotaryKnobListener {
        fun onRotate(value: Int)
    }

    init {
        this.maxValue = maxValue + 1
        this.minValue = minValue

        LayoutInflater.from(context)
            .inflate(R.layout.rotary_knob_view, this, true)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RotaryKnobView,
            0, 0).apply {

            try {
                minValue = getInt(R.styleable.RotaryKnobView_minValue, 40)
                maxValue = getInt(R.styleable.RotaryKnobView_maxValue, 220) + 1
                value = getInt(R.styleable.RotaryKnobView_initialValue, 130)
                knobDrawable = getDrawable(R.styleable.RotaryKnobView_knobDrawable)
                knobImageView.setImageDrawable(knobDrawable)
            } finally {
                recycle()
            }
        }

        // create stator
//        val ivBack = ImageView(context)
//        ivBack.setImageResource(backGround)
//        val backgroundLayoutParams = LayoutParams(
//            width, height
//        )
//        backgroundLayoutParams.addRule(CENTER_IN_PARENT)
//        addView(ivBack, backgroundLayoutParams)
        // load rotor images

//        // create rotor
//        knobImageView = ImageView(context)
//        knobImageView.setImageResource(
//
//        )
//        val knobLayoutParams = LayoutParams(
//            width, height
//        ) //LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        knobLayoutParams.addRule(CENTER_IN_PARENT)
//        addView(knobImageView, knobLayoutParams)

        // enable gesture detector
        gestureDetector = GestureDetectorCompat(context, this)
        Log.i("===KNOB===", "Initialized")
    }

    /**
     * math..
     * @param x
     * @param y
     * @return
     */
    private fun calculateAngle(x: Float, y: Float): Float {
        val angle = (-Math.toDegrees(
            Math.atan2(
                x - 0.5f.toDouble(),
                y - 0.5f.toDouble()
            )
        )).toFloat()
        Log.i("KNOb", "x: $x y:$y")

        Log.i("KNOb", "$angle")
        return angle
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) true else super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.i("===KNOB===", "attached!")
    }

    override fun onDown(event: MotionEvent): Boolean {
        val x = event.x / width.toFloat()
        val y = event.y / height.toFloat()
        mAngleDown = calculateAngle(1 - x, 1 - y) // 1- to correct our custom axis direction
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val x = e.x / width.toFloat()
        val y = e.y / height.toFloat()
        mAngleUp = calculateAngle(1 - x, 1 - y) // 1- to correct our custom axis direction
        return true
    }

    fun setRotorPosAngle(deg: Float) {
        var deg = deg
        if (deg >= 210 || deg <= 150) {
            if (deg > 180) deg = deg - 360
            val matrix = Matrix()
            knobImageView.scaleType = ScaleType.MATRIX
            matrix.postRotate(
                deg,
                width.toFloat() / 2,
                height.toFloat() / 2
            ) //getWidth()/2, getHeight()/2);
//            Log.i("Knob", width.toString())
//            Log.i("Knob", width.toString())
//            Log.i("Knob", knobImageView.drawable.bounds.width().toFloat().toString())


            knobImageView.imageMatrix = matrix
        }
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val x = e2.x / width.toFloat()
        val y = e2.y / height.toFloat()
        val rotDegrees =
            calculateAngle(1 - x, 1 - y) // 1- to correct our custom axis direction
        return if (!java.lang.Float.isNaN(rotDegrees)) {
            // instead of getting 0-> 180, -180 0 , we go for 0 -> 360
            var posDegrees = rotDegrees
            if (rotDegrees < 0) posDegrees = 360 + rotDegrees

            // deny full rotation, start start and stop point, and get a linear scale
            if (posDegrees > 210 || posDegrees < 150) {
                // rotate our imageview
                setRotorPosAngle(posDegrees)
                // get a linear scale
                val scaleDegrees =
                    rotDegrees + 150 // given the current parameters, we go from 0 to 300

                // Calculate rotary value
                val divider = 300f / (maxValue - minValue)
                value = ((scaleDegrees / divider) + minValue).toInt()
                if (listener != null) listener!!.onRotate(value)
                true //consumed
            } else false
        } else false // not consumed
    }

    override fun onShowPress(e: MotionEvent) {
        // TODO Auto-generated method stub
    }

    override fun onFling(
        arg0: MotionEvent,
        arg1: MotionEvent,
        arg2: Float,
        arg3: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

}
