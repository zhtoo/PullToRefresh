package in.srain.cube.views.ptr;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import in.srain.cube.views.ptr.indicator.PtrIndicator;

public class PtrClassicDefaultHeader extends FrameLayout implements PtrUIHandler {
    private static final String KEY_SharedPreferences = "cube_ptr_classic_last_update";
    private static SimpleDateFormat sDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int mRotateAniTime;
    protected RotateAnimation mFlipAnimation;
    protected RotateAnimation mReverseFlipAnimation;
    protected TextView mTitleTextView;
    protected TextView mLastUpdateTextView;
    protected ImageView mRotateView;
    private ProgressBar mProgressBar;
    private long mLastUpdateTime;
    private String mLastUpdateTimeKey;
    private boolean mShouldShowLastUpdate;
    protected boolean mIsShowLastUpdate;
    protected String[] mTexts;
    private PtrClassicDefaultHeader.LastUpdateTimeUpdater mLastUpdateTimeUpdater;

    public PtrClassicDefaultHeader(Context context) {
        this(context, (AttributeSet)null, 0);
    }

    public PtrClassicDefaultHeader(Context context, AttributeSet attrs) {
        this(context, (AttributeSet)null, 0);
    }

    public PtrClassicDefaultHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRotateAniTime = 150;
        this.mLastUpdateTime = -1L;
        this.mLastUpdateTimeUpdater = new PtrClassicDefaultHeader.LastUpdateTimeUpdater();
        this.initViews(attrs);
    }

    protected void initViews(AttributeSet attrs) {
        TypedArray arr = this.getContext().obtainStyledAttributes(attrs, R.styleable.PtrClassicHeader, 0, 0);
        if(arr != null) {
            this.mRotateAniTime = arr.getInt(R.styleable.PtrClassicHeader_ptr_rotate_ani_time, this.mRotateAniTime);
        }

        View header = LayoutInflater.from(this.getContext()).inflate(R.layout.cube_ptr_classic_default_header, this);
        this.mRotateView = (ImageView)header.findViewById(R.id.ptr_classic_header_rotate_view);
        this.mTitleTextView = (TextView)header.findViewById(R.id.ptr_classic_header_rotate_view_header_title);
        this.mLastUpdateTextView = (TextView)header.findViewById(R.id.ptr_classic_header_rotate_view_header_last_update);
        this.mProgressBar = (ProgressBar)header.findViewById(R.id.ptr_classic_header_rotate_view_progressbar);
        this.initData();
        this.buildAnimation();
        this.resetView();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(this.mLastUpdateTimeUpdater != null) {
            this.mLastUpdateTimeUpdater.stop();
        }

    }

    public void setRotateAniTime(int time) {
        if(time != this.mRotateAniTime && time != 0) {
            this.mRotateAniTime = time;
            this.buildAnimation();
        }
    }

    public void setLastUpdateTimeKey(String key) {
        if(!TextUtils.isEmpty(key)) {
            this.mLastUpdateTimeKey = key;
        }
    }

    public void setLastUpdateTimeRelateObject(Object object) {
        this.setLastUpdateTimeKey(object.getClass().getName());
    }

    protected void initData() {
        this.mIsShowLastUpdate = true;
        this.mTexts = this.getResources().getStringArray(R.array.cube_ptr_pull_down);
    }

    private void buildAnimation() {
        this.mFlipAnimation = new RotateAnimation(0.0F, -180.0F, 1, 0.5F, 1, 0.5F);
        this.mFlipAnimation.setInterpolator(new LinearInterpolator());
        this.mFlipAnimation.setDuration((long)this.mRotateAniTime);
        this.mFlipAnimation.setFillAfter(true);
        this.mReverseFlipAnimation = new RotateAnimation(-180.0F, 0.0F, 1, 0.5F, 1, 0.5F);
        this.mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        this.mReverseFlipAnimation.setDuration((long)this.mRotateAniTime);
        this.mReverseFlipAnimation.setFillAfter(true);
    }

    private void resetView() {
        this.hideRotateView();
        this.mProgressBar.setVisibility(INVISIBLE);
    }

    private void hideRotateView() {
        this.mRotateView.clearAnimation();
        this.mRotateView.setVisibility(INVISIBLE);
    }

    public void onUIReset(PtrFrameLayout frame) {
        this.resetView();
        this.mTitleTextView.setVisibility(INVISIBLE);
        this.mShouldShowLastUpdate = true;
        this.tryUpdateLastUpdateTime();
    }

    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        this.mShouldShowLastUpdate = true;
        this.tryUpdateLastUpdateTime();
        this.mLastUpdateTimeUpdater.start();
        this.mProgressBar.setVisibility(INVISIBLE);
        this.mRotateView.setVisibility(VISIBLE);
        this.mTitleTextView.setVisibility(VISIBLE);
        if(frame.isPullToRefresh()) {
            this.mTitleTextView.setText(this.mTexts[0]);
        } else {
            this.mTitleTextView.setText(this.mTexts[0]);
        }

    }

    public void onUIRefreshBegin(PtrFrameLayout frame) {
        this.mShouldShowLastUpdate = false;
        this.hideRotateView();
        this.mProgressBar.setVisibility(VISIBLE);
        this.mTitleTextView.setVisibility(VISIBLE);
        this.mTitleTextView.setText(this.mTexts[2]);
        this.tryUpdateLastUpdateTime();
        this.mLastUpdateTimeUpdater.stop();
    }

    public void onUIRefreshComplete(PtrFrameLayout frame) {
        this.hideRotateView();
        this.mProgressBar.setVisibility(INVISIBLE);
        this.mTitleTextView.setVisibility(VISIBLE);
        this.mTitleTextView.setText(this.mTexts[3]);
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("cube_ptr_classic_last_update", 0);
        if(!TextUtils.isEmpty(this.mLastUpdateTimeKey)) {
            this.mLastUpdateTime = (new Date()).getTime();
            sharedPreferences.edit().putLong(this.mLastUpdateTimeKey, this.mLastUpdateTime).commit();
        }

    }

    private void tryUpdateLastUpdateTime() {
        if(!TextUtils.isEmpty(this.mLastUpdateTimeKey) && this.mShouldShowLastUpdate && this.mIsShowLastUpdate) {
            String time = this.getLastUpdateTime();
            if(TextUtils.isEmpty(time)) {
                this.mLastUpdateTextView.setVisibility(GONE);
            } else {
                this.mLastUpdateTextView.setVisibility(VISIBLE);
                this.mLastUpdateTextView.setText(time);
            }
        } else {
            this.mLastUpdateTextView.setVisibility(GONE);
        }

    }

    private String getLastUpdateTime() {
        if(this.mLastUpdateTime == -1L && !TextUtils.isEmpty(this.mLastUpdateTimeKey)) {
            this.mLastUpdateTime = this.getContext().getSharedPreferences("cube_ptr_classic_last_update", 0).getLong(this.mLastUpdateTimeKey, -1L);
        }

        if(this.mLastUpdateTime == -1L) {
            return null;
        } else {
            long diffTime = (new Date()).getTime() - this.mLastUpdateTime;
            int seconds = (int)(diffTime / 1000L);
            if(diffTime < 0L) {
                return null;
            } else if(seconds <= 0) {
                return null;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(this.mTexts[4]);
                if(seconds < 60) {
                    sb.append(seconds + this.getContext().getString(R.string.cube_ptr_seconds_ago));
                } else {
                    int minutes = seconds / 60;
                    if(minutes > 60) {
                        int hours = minutes / 60;
                        if(hours > 24) {
                            Date date = new Date(this.mLastUpdateTime);
                            sb.append(sDataFormat.format(date));
                        } else {
                            sb.append(hours + this.getContext().getString(R.string.cube_ptr_hours_ago));
                        }
                    } else {
                        sb.append(minutes + this.getContext().getString(R.string.cube_ptr_minutes_ago));
                    }
                }

                return sb.toString();
            }
        }
    }

    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        int mOffsetToRefresh = frame.getOffsetToRefresh();
        int currentPos = ptrIndicator.getCurrentPosY();
        int lastPos = ptrIndicator.getLastPosY();
        if(currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if(isUnderTouch && status == 2) {
                this.crossRotateLineFromBottomUnderTouch(frame);
                if(this.mRotateView != null) {
                    this.mRotateView.clearAnimation();
                    this.mRotateView.startAnimation(this.mReverseFlipAnimation);
                }
            }
        } else if(currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh && isUnderTouch && status == 2) {
            this.crossRotateLineFromTopUnderTouch(frame);
            if(this.mRotateView != null) {
                this.mRotateView.clearAnimation();
                this.mRotateView.startAnimation(this.mFlipAnimation);
            }
        }

    }

    private void crossRotateLineFromTopUnderTouch(PtrFrameLayout frame) {
        if(!frame.isPullToRefresh()) {
            this.mTitleTextView.setVisibility(VISIBLE);
            this.mTitleTextView.setText(this.mTexts[1]);
        }

    }

    private void crossRotateLineFromBottomUnderTouch(PtrFrameLayout frame) {
        this.mTitleTextView.setVisibility(VISIBLE);
        if(frame.isPullToRefresh()) {
            this.mTitleTextView.setText(this.mTexts[0]);
        } else {
            this.mTitleTextView.setText(this.mTexts[0]);
        }

    }

    private class LastUpdateTimeUpdater implements Runnable {
        private boolean mRunning;

        private LastUpdateTimeUpdater() {
            this.mRunning = false;
        }

        private void start() {
            if(!TextUtils.isEmpty(PtrClassicDefaultHeader.this.mLastUpdateTimeKey)) {
                this.mRunning = true;
                this.run();
            }
        }

        private void stop() {
            this.mRunning = false;
            PtrClassicDefaultHeader.this.removeCallbacks(this);
        }

        public void run() {
            PtrClassicDefaultHeader.this.tryUpdateLastUpdateTime();
            if(this.mRunning) {
                PtrClassicDefaultHeader.this.postDelayed(this, 1000L);
            }

        }
    }
}


