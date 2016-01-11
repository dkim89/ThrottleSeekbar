package dkapps.throttleseekbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by davidkim on 1.02.16
 */
public class ThrottleSeekbar extends SeekBar {
    static final int DEFAULT_MAX = 100;
    static final int DEFAULT_MIN = -100;
    static final int DEFAULT_OFFSET = 0;
    static final int DEFAULT_RESET = 1000;  // Milliseconds
    static final int DEFAULT_ORIENTATION = 0;
    static final int DEFAULT_DIRECTION = 0;

    Context mContext;
    // Attribute variables
    int mOrientation;
    int mDirection;
    int mOffset, mMaxValue, mMinValue;
    int mResetSpeed;
    int mStartColor, mMidColor, mEndColor, mBackgroundColor, mStrokeColor;

    // Layer-List Drawables
    LayerDrawable mSeekbarLayerlist;
    GradientDrawable mLeftBackground, mRightBackground, mLeftForeground, mRightForeground;

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
                mOrientation = a.getInt(R.styleable.ThrottleSeekbar_orientation, DEFAULT_ORIENTATION);
                mDirection = a.getInt(R.styleable.ThrottleSeekbar_direction, DEFAULT_DIRECTION);
                // Range, Offset, Min, Max, Reset Speed
                mOffset = a.getInt(R.styleable.ThrottleSeekbar_offset, DEFAULT_OFFSET);
                mMaxValue = a.getInt(R.styleable.ThrottleSeekbar_maxValue, DEFAULT_MAX);
                mMinValue = a.getInt(R.styleable.ThrottleSeekbar_minValue, DEFAULT_MIN);
                mResetSpeed = a.getInt(R.styleable.ThrottleSeekbar_resetTime, DEFAULT_RESET);
                // Color and Stroke
                mStartColor = a.getColor(R.styleable.ThrottleSeekbar_startProgressColor, getResources().getColor(R.color.flat_green));
                mMidColor = a.getColor(R.styleable.ThrottleSeekbar_middleProgressColor, getResources().getColor(R.color.flat_yellow));
                mEndColor = a.getColor(R.styleable.ThrottleSeekbar_endProgressColor, getResources().getColor(R.color.flat_red));
                mBackgroundColor = a.getColor(R.styleable.ThrottleSeekbar_backgroundColor, getResources().getColor(R.color.flat_white));
                mStrokeColor = a.getColor(R.styleable.ThrottleSeekbar_strokeColor, getResources().getColor(R.color.flat_gray));
            } finally {
                a.recycle();
            }
        }
        timer = new Timer();
        initializeProgressBar();
    }

    private void initializeProgressBar() {
        // Get Layer-List drawable
        mSeekbarLayerlist = (LayerDrawable) getResources().getDrawable(R.drawable.layerlist_seekbar);
        ClipDrawable clipDrawable = (ClipDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.progressbar_clip);
        if (mSeekbarLayerlist != null) {
            mLeftBackground = (GradientDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.seekbar_left);
            mLeftForeground = (GradientDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.progressbar_left);
            mRightBackground = (GradientDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.seekbar_right);
//            mRightForeground = (GradientDrawable) clipDrawable.(R.id.progressbar_right);
        }

        setBarBackgroundColor(mBackgroundColor, mStrokeColor);
        setProgressDrawable(getResources().getDrawable(R.drawable.layerlist_seekbar));
        initializeThumb();
        setMax(mMaxValue);
        setProgress(mOffset);
    }

    private void initializeThumb() {
        setThumb(getResources().getDrawable(R.drawable.shape_thumb));
        setThumbOffset(0);
    }

    public void setBarBackgroundColor(int backgroundColor, int strokeColor) {
        mLeftBackground.setColor(backgroundColor);
        mRightBackground.setColor(strokeColor);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    // Sets seekbar orientation vertial or horizontal
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
        onSizeChanged(getWidth(), getHeight(), 0, 0);
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
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                onSizeChanged(getWidth(), getHeight(), 0, 0);
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

    public void onActionMove(MotionEvent event) {
        setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(l);
    }
}
