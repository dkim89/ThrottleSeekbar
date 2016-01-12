package dkapps.throttleseekbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
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
    LayerDrawable mSeekbarLayerlist;
    InsetDrawable mLeftBGInset, mRightBGInset, mLeftFGInset, mRightFGInset;
    GradientDrawable mLeftBG, mRightBG, mLeftFG, mRightFG;

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
        // Get Layer-List drawable
        mSeekbarLayerlist = (LayerDrawable) getResources().getDrawable(R.drawable.layerlist_seekbar);
        setProgressDrawable(mSeekbarLayerlist);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ThrottleSeekbar, 0, 0);
            try {
                // Orientation and Direction
                mOrientation = a.getInt(R.styleable.ThrottleSeekbar_orientation, getResources().getInteger(R.integer.orientation_horizontal));
                mDirection = a.getInt(R.styleable.ThrottleSeekbar_direction, getResources().getInteger(R.integer.orientation_vertical));

                // Min, Max, Offset, Reset Speed
                mMaxValue = a.getInt(R.styleable.ThrottleSeekbar_maxValue, getResources().getInteger(R.integer.value_max));
                mMinValue = a.getInt(R.styleable.ThrottleSeekbar_minValue, getResources().getInteger(R.integer.value_min));
                mOffset = a.getInt(R.styleable.ThrottleSeekbar_offset, getResources().getInteger(R.integer.value_offset));
                mResetSpeed = a.getInt(R.styleable.ThrottleSeekbar_resetSpeed, getResources().getInteger(R.integer.value_reset));

                // Bar Dimension
                mBarLength = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_barLength, (int) getResources().getDimension(R.dimen.bar_length));
                mBarWidth = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_barWidth, (int) getResources().getDimension(R.dimen.bar_width));
                mBarStroke = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_barStroke, (int) getResources().getDimension(R.dimen.bar_stroke));
                mBarRadius = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_barRadius, (int) getResources().getDimension(R.dimen.bar_radius));

                // Thumb Dimension
                mThumbWidth = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_thumbWidth, (int) getResources().getDimension(R.dimen.thumb_width));
                mThumbHeight = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_thumbHeight, (int) getResources().getDimension(R.dimen.thumb_height));
                mThumbStroke = a.getDimensionPixelSize(R.styleable.ThrottleSeekbar_thumbStroke, (int) getResources().getDimension(R.dimen.thumb_stroke));

                // Bar color
                mStartColor = a.getColor(R.styleable.ThrottleSeekbar_startColor, getResources().getColor(R.color.flat_green));
                mMidColor = a.getColor(R.styleable.ThrottleSeekbar_middleColor, getResources().getColor(R.color.flat_yellow));
                mEndColor = a.getColor(R.styleable.ThrottleSeekbar_endColor, getResources().getColor(R.color.flat_red));
                mBarSolidColor = a.getColor(R.styleable.ThrottleSeekbar_barSolidColor, getResources().getColor(R.color.flat_blue));
                mBarStrokeColor = a.getColor(R.styleable.ThrottleSeekbar_barStrokeColor, getResources().getColor(R.color.flat_blue_transparent));

                // Thumb color
                mThumbSolidColor = a.getColor(R.styleable.ThrottleSeekbar_thumbSolidColor, getResources().getColor(R.color.flat_white));
                mThumbStrokeColor = a.getColor(R.styleable.ThrottleSeekbar_thumbStrokeColor, getResources().getColor(R.color.flat_gray));
            } finally {
                a.recycle();
            }
        }
        timer = new Timer();
        initializeThrottleBar();
    }

    private void initializeThrottleBar() {
        if (mSeekbarLayerlist != null) {
//            // Inset
//            mLeftBGInset = (InsetDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.seekbar_left_inset);
//            mLeftFGInset = (InsetDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.progressbar_left_inset);
//            mRightBGInset = (InsetDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.progressbar_right_inset);
//            mRightFGInset = (InsetDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.seekbar_left_inset);
            // Gradient
            mLeftBG = (GradientDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.seekbar_left);
            mLeftFG = (GradientDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.progressbar_left);
            mRightBG = (GradientDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.progressbar_right);
            mRightFG = (GradientDrawable) mSeekbarLayerlist.findDrawableByLayerId(R.id.seekbar_right);


            setBarDimens();
            setBarStyle();
            initializeThumb();
        }

        setMax(mMaxValue);
        setProgress(mOffset);
    }

    private void setBarDimens(){

    }

    private void setBarStyle() {
        // Left-Right offset
        // TODO NEED TO CONVERT IT TO DP RATIO
//        mLeftBGInset.setBounds(mOffset, 0, 0, 0);
//        mLeftFGInset.setBounds(mOffset, 0, 0, 0);
//        mRightBGInset.setBounds(0, 0, mOffset, 0);
//        mRightFGInset.setBounds(0, 0, mOffset, 0);

        // Colors
        mLeftBG.setColor(mBarSolidColor);
        mLeftBG.setStroke(mBarStroke, mBarStrokeColor);
        mRightBG.setColor(mBarSolidColor);
        mRightBG.setStroke(mBarStroke, mBarStrokeColor);

        mLeftFG.setColors(new int[] {mStartColor, mMidColor, mEndColor});
        mRightFG.setColors(new int[] {mStartColor, mMidColor, mEndColor});

        // Radius
        mLeftBG.setCornerRadii(new float[] {0, mBarRadius, mBarRadius, 0});
        mLeftFG.setCornerRadii(new float[] {0, mBarRadius, mBarRadius, 0});
        mRightBG.setCornerRadii(new float[] {mBarRadius, 0, 0, mBarRadius});
        mRightFG.setCornerRadii(new float[] {mBarRadius, 0, 0, mBarRadius});
    }

    private void initializeThumb() {
        setThumb(getResources().getDrawable(R.drawable.shape_thumb));
        setThumbOffset(0);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
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
