package svs.meeting.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import svs.meeting.app.R;


/**
 * Created by Mersens on 2017/12/11 15:12
 * Email:626168564@qq.com
 */

public class LoadingView extends View {
    protected static final int LINE_COUNT = 12;
    protected static final int DEGREE_PER_LINE = 360 / LINE_COUNT;
    protected Paint mPaint;
    protected int mViewSize;
    protected int mViewColor;
    protected int mAnimateValue = 0;
    protected ValueAnimator mAnimator;
    protected int DEFAULT_VIEW_SIZE = 32;
    protected int DEFAULT_VIEW_COLOR = Color.WHITE;
    private ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mAnimateValue = (int) animation.getAnimatedValue();
            invalidate();
        }
    };

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingView);
        if (a.hasValue(R.styleable.LoadingView_view_size)) {
            mViewSize = (int) a.getDimension(R.styleable.LoadingView_view_size, dp2px(DEFAULT_VIEW_SIZE));
        } else {
            mViewSize = dp2px(DEFAULT_VIEW_SIZE);
        }
        if (a.hasValue(R.styleable.LoadingView_view_color)) {
            mViewColor = a.getColor(R.styleable.LoadingView_view_color, DEFAULT_VIEW_COLOR);
        } else {
            mViewColor = DEFAULT_VIEW_COLOR;
        }
        a.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(mViewColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mViewSize, mViewSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        drawLoading(canvas, mAnimateValue * DEGREE_PER_LINE);
        canvas.restoreToCount(saveCount);
    }

    private void drawLoading(Canvas canvas, int i) {
        int itemHeight = mViewSize / 6;
        int itemWidth = mViewSize / 12;
        mPaint.setStrokeWidth(itemWidth);
        canvas.rotate(i, mViewSize / 2, mViewSize / 2);
        canvas.translate(mViewSize / 2, mViewSize / 2);
        for (int j = 0; j < LINE_COUNT; j++) {
            canvas.rotate(DEGREE_PER_LINE);
            mPaint.setAlpha((int) 255f * (j + 1) / LINE_COUNT);
            canvas.translate(0, -mViewSize / 2 + itemWidth / 2);
            canvas.drawLine(0, 0, 0, itemHeight, mPaint);
            canvas.translate(0, mViewSize / 2 - itemWidth / 2);
        }
    }

    public void startAnimator() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofInt(0, LINE_COUNT-1);
            mAnimator.addUpdateListener(mUpdateListener);
            mAnimator.setDuration(600);
            mAnimator.setRepeatMode(ValueAnimator.RESTART);
            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.start();
        } else {
            if (!mAnimator.isStarted()) {
                mAnimator.start();
            }

        }
    }

    public void stopAnimator() {
        if (mAnimator != null) {
            mAnimator.removeUpdateListener(mUpdateListener);
            mAnimator.removeAllUpdateListeners();
            mAnimator.cancel();
            mAnimator = null;

        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimator();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startAnimator();

        } else {
            stopAnimator();
        }
    }

    public void setViewSize(int size){
        mViewSize=size;
        requestLayout();
    }

    public void setViewColor(int color){
        mViewColor=color;
        invalidate();
    }



    private int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());

    }
}
