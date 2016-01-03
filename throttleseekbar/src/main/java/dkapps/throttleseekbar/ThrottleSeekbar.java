package dkapps.throttleseekbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by davidkim on 1.02.16
 */
public class ThrottleSeekbar extends SeekBar{
    Context mContext;
    Timer timer;
    boolean cancelTimer;

    int mOrientation;
    int mResetTime;
    int mStartColor, mMidColor, mEndColor, mBackgroundColor;
    int mMinRange, mMaxRange, mDefault;

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
                mOrientation = a.getInt(R.styleable.ThrottleSeekbar_orientation, 0);
                mResetTime = a.getInt(R.styleable.ThrottleSeekbar_resetTime, 1000);
                mStartColor = a.getColor(R.styleable.ThrottleSeekbar_startProgressColor, getResources().getColor(R.color.flat_green));
                mMidColor = a.getColor(R.styleable.ThrottleSeekbar_middleProgressColor, getResources().getColor(R.color.flat_yellow));
                mEndColor = a.getColor(R.styleable.ThrottleSeekbar_endProgressColor, getResources().getColor(R.color.flat_red));
                mBackgroundColor = a.getColor(R.styleable.ThrottleSeekbar_backgroundColor, getResources().getColor(R.color.flat_white));
                mMaxRange = a.getInt(R.styleable.ThrottleSeekbar_maxRange, 100);
            } finally {
                a.recycle();
            }
        }
        timer = new Timer();
        initializeProgressBar();
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
                                if (getProgress() == mDefault || cancelTimer)
                                    cancel();
                                else if (getProgress() < mDefault)
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

    private void initializeProgressBar() {
        setProgressDrawable(getResources().getDrawable(R.drawable.layerlist_seekbar));
        setThumb(getResources().getDrawable(R.drawable.shape_thumb));
        setThumbOffset(0);
        setMax(mMaxRange);
        setProgress(mDefault);
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
