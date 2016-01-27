package dkapps.throttleseekbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.SeekBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by davidkim on 1.02.16
 */
public class ThrottleSeekbar extends SeekBar {
    Context mContext;

    // Orientation and Direction values
    int mOrientation, mDirection;
    // Data values
    int mMaxValue, mMinValue, mOffset, mResetSpeed;
    // Bar dimensions
    int mBarLength, mBarWidth, mBarStroke, mBarRadius;
    // Thumb dimensions
    int mThumbWidth, mThumbHeight, mThumbStroke;
    // Color values
    int mStartColor, mMidColor, mEndColor, mBarSolidColor, mBarStrokeColor, mThumbSolidColor, mThumbStrokeColor;

    // Drawables
    GradientDrawable mLeftBG, mLeftFG, mRightBG, mRightFG;
    ClipDrawable mClipFG;
    LayerDrawable mLayerBG, mLayerFG;

    Timer timer;
    boolean cancelTimer;

    public ThrottleSeekbar(Context context) {
        this(context, null);
    }

    public ThrottleSeekbar(Context context, AttributeSet attrs) {
        this(context,attrs, 0);
    }

    public ThrottleSeekbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ThrottleSeekbar, 0, 0);
            try {
                // Orientation and Direction
                mOrientation = a.getInt(R.styleable.ThrottleSeekbar_orientation, Throttle.HORIZONAL);
                mDirection = a.getInt(R.styleable.ThrottleSeekbar_direction, Throttle.DIR_NORMAL);

                // Min, Max, Offset, Reset Speed
                mMaxValue = a.getInt(R.styleable.ThrottleSeekbar_maxValue, Throttle.MAX_VALUE);
                mMinValue = a.getInt(R.styleable.ThrottleSeekbar_minValue, Throttle.MIN_VALUE);
                mOffset = a.getInt(R.styleable.ThrottleSeekbar_offset, Throttle.OFFSET);
                mResetSpeed = a.getInt(R.styleable.ThrottleSeekbar_resetSpeed, Throttle.RESET_TIME);

                // Bar Dimension
                mBarLength = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_barLength,Throttle.BAR_LENGTH);
                mBarWidth = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_barWidth, Throttle.BAR_WIDTH);
                mBarStroke = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_barStroke, Throttle.BAR_STROKE);
                mBarRadius = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_barRadius, Throttle.BAR_RADIUS);

                // Thumb Dimension
                mThumbWidth = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_thumbWidth, Throttle.THUMB_WIDTH);
                mThumbHeight = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_thumbHeight, Throttle.THUMB_HEIGHT);
                mThumbStroke = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_thumbStroke, Throttle.THUMB_STROKE);

                // Bar color
                mStartColor = a.getColor(R.styleable.ThrottleSeekbar_startColor, Throttle.FLAT_GREEN);
                mMidColor = a.getColor(R.styleable.ThrottleSeekbar_middleColor, Throttle.FLAT_YELLOW);
                mEndColor = a.getColor(R.styleable.ThrottleSeekbar_endColor, Throttle.FLAT_RED);
                mBarSolidColor = a.getColor(R.styleable.ThrottleSeekbar_barSolidColor, Throttle.FLAT_BLUE);
                mBarStrokeColor = a.getColor(R.styleable.ThrottleSeekbar_barStrokeColor, Throttle.FLAT_BLUE_TRANSPARENT);

                // Thumb color
                mThumbSolidColor = a.getColor(R.styleable.ThrottleSeekbar_thumbSolidColor, Throttle.FLAT_WHITE);
                mThumbStrokeColor = a.getColor(R.styleable.ThrottleSeekbar_thumbStrokeColor, Throttle.FLAT_GRAY);
            } finally {
                a.recycle();
            }
        }

        // Initialize ThottleBar Drawables
        initializeThrottleBar();

        setThumb(getResources().getDrawable(R.drawable.shape_thumb));
        setThumbOffset(0);
        setMax(mMaxValue);
//        setProgress(mOffset);
        timer = new Timer();
        initializeThrottleBar();
    }

    private void initializeThrottleBar() {
        float radiusValue = getPixelDp(mBarRadius);
        // Left Background Gradient
        mLeftBG = new GradientDrawable();
        mLeftBG.setCornerRadii(new float[] {
                0, 0,
                radiusValue, radiusValue,
                radiusValue, radiusValue,
                0, 0});
        mLeftBG.setColor(mBarSolidColor);
        mLeftBG.setStroke(mBarStroke, mBarStrokeColor);

        // Right Background Gradient
        mRightBG = new GradientDrawable();
        mRightBG.setCornerRadii(new float[] {
                radiusValue, radiusValue,
                0, 0,
                0, 0,
                radiusValue, radiusValue});
        mRightBG.setColors(new int[] {mStartColor, mMidColor, mEndColor});
        mRightBG.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        // Left Foreground Gradient
        mLeftFG = new GradientDrawable();
        mLeftFG.setCornerRadii(new float[] {
                0, 0,
                radiusValue, radiusValue,
                radiusValue, radiusValue,
                0, 0 });
        mLeftFG.setColors(new int[] {mStartColor, mMidColor, mEndColor});
        mLeftFG.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        // Right Foreground Gradient
        mRightFG = new GradientDrawable();
        mRightFG.setCornerRadii(new float[] {
                radiusValue, radiusValue,
                0, 0,
                0, 0,
                radiusValue, radiusValue});
        mRightFG.setColor(mBarSolidColor);
        mRightFG.setStroke(mBarStroke, mBarStrokeColor);

        // Foreground Layer Drawable
        Drawable[] layersFG = {mLeftFG, mRightFG};
        mLayerFG = new LayerDrawable(layersFG);
//        mLayerFG.setLayerInset(0, 0, 0, getPixelDp(mOffset), 0);
//        mLayerFG.setLayerInset(1, getPixelDp(mBarLength - mOffset), 0, 0, 0);

        // Foreground Clip Drawable
        // TODO CHECK ORIENTATION
        mClipFG = new ClipDrawable(mLayerFG, Gravity.START, ClipDrawable.HORIZONTAL);

        // Background Layer Drawable
        Drawable[] layersBG = {mLeftBG, mRightBG, mClipFG};
        mLayerBG = new LayerDrawable(layersBG);
//        mLayerBG.setLayerInset(0, 0, 0, getPixelDp(mOffset), 0);
//        mLayerBG.setLayerInset(1, getPixelDp(mBarLength - mOffset), 0, 0, 0);

        // Set Layer Drawable as Progress Bar Drawable
        setProgressDrawable(mLayerBG);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height, width;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(mBarLength, widthSize);
        } else {
            //Be whatever you want
            width = mBarWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(mThumbHeight, heightSize);
        } else {
            //Be whatever you want
            height = mThumbHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);

//        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
//        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());

    }

    // Sets seekbar orientation vertial or horizontal
    @Override
    protected void onDraw(Canvas c) {
        if (mOrientation == 1) {
            c.rotate(-90);
            c.translate(-getHeight(), 0);
        }
        super.onDraw(c);
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
//        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                cancelTimer = true;
                break;
            case MotionEvent.ACTION_MOVE:
                cancelTimer = true;
                if (mOrientation == 1) {
                    setProgress(getMax() - (int) (getMax() * event.getX() / getWidth()));
                    onSizeChanged(getWidth(), getHeight(), 0, 0);
                } else {
                    setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                    onSizeChanged(getWidth(), getHeight(), 0, 0);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                cancelTimer = true;
                break;
            case MotionEvent.ACTION_UP:
                cancelTimer = false;
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getProgress() == mOffset || cancelTimer)
                                    cancel();
                                else if (getProgress() < mOffset)
                                    setProgress(getProgress()+1);
                                else
                                    setProgress(getProgress()-1);
                            }
                        });
                    }
                }, 0, 5);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;

        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mOrientation == 0) {
            super.onSizeChanged(w, h, oldw, oldh);
        } else {
            super.onSizeChanged(h, w, oldh, oldw);
        }
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(l);
    }

    public int getPixelDp(float dp) {
        return (int)(getResources().getDisplayMetrics().density * dp + 0.5f);
    }
}
