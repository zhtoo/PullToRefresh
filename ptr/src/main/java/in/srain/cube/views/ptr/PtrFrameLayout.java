package in.srain.cube.views.ptr;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;

import in.srain.cube.views.ptr.indicator.PtrIndicator;
import in.srain.cube.views.ptr.util.PtrCLog;

/**
 * This layout view for "Pull to Refresh(Ptr)" support all of the view, you can contain everything you want.
 * support: pull to refresh / release to refresh / auto refresh / keep header view while refreshing / hide header view while refreshing
 * It defines {@link in.srain.cube.views.ptr.PtrUIHandler}, which allows you customize the UI easily.
 */
public class PtrFrameLayout extends ViewGroup {
    private byte mStatus;
    public static final int TimeInterval = 100;
    public static final byte PTR_STATUS_INIT = 1;
    public static final byte PTR_STATUS_PREPARE = 2;
    public static final byte PTR_STATUS_LOADING = 3;
    public static final byte PTR_STATUS_COMPLETE = 4;
    private static final boolean DEBUG_LAYOUT = true;
    public static boolean DEBUG = true;
    private static int ID = 1;
    protected final String LOG_TAG;
    private static byte FLAG_AUTO_REFRESH_AT_ONCE = 1;
    private static byte FLAG_AUTO_REFRESH_BUT_LATER = 2;
    private static byte FLAG_ENABLE_NEXT_PTR_AT_ONCE = 4;
    private static byte FLAG_PIN_CONTENT = 8;
    private static byte MASK_AUTO_REFRESH = 3;
    protected View mContent;
    private int mHeaderId;
    private int mContainerId;
    private int mFooterId;
    private PtrFrameLayout.Mode mMode;
    private int mDurationToClose;
    private int mDurationToCloseHeader;
    private boolean mKeepHeaderWhenRefresh;
    private boolean mPullToRefresh;
    private View mHeaderView;
    private View mFooterView;
    private PtrUIHandlerHolder mPtrUIHandlerHolder;
    private PtrHandler mPtrHandler;
    private PtrFrameLayout.ScrollChecker mScrollChecker;
    private int mTouchSlop;
    private int mPagingTouchSlop;
    private int mHeaderHeight;
    private int mFooterHeight;
    private boolean mDisableWhenHorizontalMove;
    private int mFlag;
    private boolean mPreventForHorizontal;
    private MotionEvent mLastMoveEvent;
    private PtrUIHandlerHook mRefreshCompleteHook;
    private long downTime;
    private int mLoadingMinTime;
    private long mLoadingStartTime;
    private PtrIndicator mPtrIndicator;
    private boolean mHasSendCancelEvent;
    private Runnable mPerformRefreshCompleteDelay;

    public PtrFrameLayout(Context context) {
        this(context, (AttributeSet)null);
    }

    public PtrFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PtrFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mStatus = 1;
        this.LOG_TAG = "ptr-frame-" + ++ID;
        this.mHeaderId = 0;
        this.mContainerId = 0;
        this.mFooterId = 0;
        this.mMode = PtrFrameLayout.Mode.BOTH;
        this.mDurationToClose = 200;
        this.mDurationToCloseHeader = 1000;
        this.mKeepHeaderWhenRefresh = true;
        this.mPullToRefresh = false;
        this.mPtrUIHandlerHolder = PtrUIHandlerHolder.create();
        this.mDisableWhenHorizontalMove = false;
        this.mFlag = 0;
        this.mPreventForHorizontal = false;
        this.mLoadingMinTime = 500;
        this.mLoadingStartTime = 0L;
        this.mHasSendCancelEvent = false;
        this.mPerformRefreshCompleteDelay = new Runnable() {
            public void run() {
                PtrFrameLayout.this.performRefreshComplete();
            }
        };
        this.mPtrIndicator = new PtrIndicator();
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PtrFrameLayout, 0, 0);
        if(arr != null) {
            this.mHeaderId = arr.getResourceId(R.styleable.PtrFrameLayout_ptr_header, this.mHeaderId);
            this.mContainerId = arr.getResourceId(R.styleable.PtrFrameLayout_ptr_content, this.mContainerId);
            this.mFooterId = arr.getResourceId(R.styleable.PtrFrameLayout_ptr_footer, this.mFooterId);
            this.mPtrIndicator.setResistance(arr.getFloat(R.styleable.PtrFrameLayout_ptr_resistance, this.mPtrIndicator.getResistance()));
            this.mDurationToClose = arr.getInt(R.styleable.PtrFrameLayout_ptr_duration_to_close, this.mDurationToClose);
            this.mDurationToCloseHeader = arr.getInt(R.styleable.PtrFrameLayout_ptr_duration_to_close_header, this.mDurationToCloseHeader);
            float conf = this.mPtrIndicator.getRatioOfHeaderToHeightRefresh();
            conf = arr.getFloat(R.styleable.PtrFrameLayout_ptr_ratio_of_header_height_to_refresh, conf);
            this.mPtrIndicator.setRatioOfHeaderHeightToRefresh(conf);
            this.mKeepHeaderWhenRefresh = arr.getBoolean(R.styleable.PtrFrameLayout_ptr_keep_header_when_refresh, this.mKeepHeaderWhenRefresh);
            this.mPullToRefresh = arr.getBoolean(R.styleable.PtrFrameLayout_ptr_pull_to_fresh, this.mPullToRefresh);
            this.mMode = this.getModeFromIndex(arr.getInt(R.styleable.PtrFrameLayout_ptr_mode, 4));
            arr.recycle();
        }

        this.mScrollChecker = new PtrFrameLayout.ScrollChecker();
        ViewConfiguration var6 = ViewConfiguration.get(this.getContext());
        this.mTouchSlop = var6.getScaledTouchSlop();
        this.mPagingTouchSlop = this.mTouchSlop * 2;
    }

    private PtrFrameLayout.Mode getModeFromIndex(int index) {
        switch(index) {
            case 0:
                return PtrFrameLayout.Mode.NONE;
            case 1:
                return PtrFrameLayout.Mode.REFRESH;
            case 2:
                return PtrFrameLayout.Mode.LOAD_MORE;
            case 3:
                return PtrFrameLayout.Mode.BOTH;
            default:
                return PtrFrameLayout.Mode.BOTH;
        }
    }

    protected void onFinishInflate() {
        int childCount = this.getChildCount();
        if(childCount > 3) {
            throw new IllegalStateException("PtrFrameLayout only can host 2 elements");
        } else {
            final View errorView;
            final View child2;
            if(childCount == 3) {
                if(this.mHeaderId != 0 && this.mHeaderView == null) {
                    this.mHeaderView = this.findViewById(this.mHeaderId);
                }

                if(this.mContainerId != 0 && this.mContent == null) {
                    this.mContent = this.findViewById(this.mContainerId);
                }

                if(this.mFooterId != 0 && this.mFooterView == null) {
                    this.mFooterView = this.findViewById(this.mFooterId);
                }

                if(this.mContent == null || this.mHeaderView == null || this.mFooterView == null) {
                    errorView = this.getChildAt(0);
                    child2 = this.getChildAt(1);
                    final View child3 = this.getChildAt(2);
                    if(this.mContent == null && this.mHeaderView == null && this.mFooterView == null) {
                        this.mHeaderView = errorView;
                        this.mContent = child2;
                        this.mFooterView = child3;
                    } else {
                        ArrayList view = new ArrayList(3) {
                            {
                                this.add(errorView);
                                this.add(child2);
                                this.add(child3);
                            }
                        };
                        if(this.mHeaderView != null) {
                            view.remove(this.mHeaderView);
                        }

                        if(this.mContent != null) {
                            view.remove(this.mContent);
                        }

                        if(this.mFooterView != null) {
                            view.remove(this.mFooterView);
                        }

                        if(this.mHeaderView == null && view.size() > 0) {
                            this.mHeaderView = (View)view.get(0);
                            view.remove(0);
                        }

                        if(this.mContent == null && view.size() > 0) {
                            this.mContent = (View)view.get(0);
                            view.remove(0);
                        }

                        if(this.mFooterView == null && view.size() > 0) {
                            this.mFooterView = (View)view.get(0);
                            view.remove(0);
                        }
                    }
                }
            } else if(childCount == 2) {
                if(this.mHeaderId != 0 && this.mHeaderView == null) {
                    this.mHeaderView = this.findViewById(this.mHeaderId);
                }

                if(this.mContainerId != 0 && this.mContent == null) {
                    this.mContent = this.findViewById(this.mContainerId);
                }

                if(this.mContent == null || this.mHeaderView == null) {
                    errorView = this.getChildAt(0);
                    child2 = this.getChildAt(1);
                    if(errorView instanceof PtrUIHandler) {
                        this.mHeaderView = errorView;
                        this.mContent = child2;
                    } else if(child2 instanceof PtrUIHandler) {
                        this.mHeaderView = child2;
                        this.mContent = errorView;
                    } else if(this.mContent == null && this.mHeaderView == null) {
                        this.mHeaderView = errorView;
                        this.mContent = child2;
                    } else if(this.mHeaderView == null) {
                        this.mHeaderView = this.mContent == errorView?child2:errorView;
                    } else {
                        this.mContent = this.mHeaderView == errorView?child2:errorView;
                    }
                }
            } else if(childCount == 1) {
                this.mContent = this.getChildAt(0);
            } else {
                TextView errorView1 = new TextView(this.getContext());
                errorView1.setClickable(true);
                errorView1.setTextColor(0xffff6600);
                errorView1.setGravity(17);
                errorView1.setTextSize(20.0F);
                errorView1.setText("The content view in PtrFrameLayout is empty. Do you forget to specify its id in xml layout file?");
                this.mContent = errorView1;
                this.addView(this.mContent);
            }

            if(this.mHeaderView != null) {
                this.mHeaderView.bringToFront();
            }

            super.onFinishInflate();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(this.mScrollChecker != null) {
            this.mScrollChecker.destroy();
        }

        if(this.mPerformRefreshCompleteDelay != null) {
            this.removeCallbacks(this.mPerformRefreshCompleteDelay);
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(DEBUG) {
            PtrCLog.d(this.LOG_TAG, "onMeasure frame: width: %s, height: %s, padding: %s %s %s %s", new Object[]{Integer.valueOf(this.getMeasuredHeight()), Integer.valueOf(this.getMeasuredWidth()), Integer.valueOf(this.getPaddingLeft()), Integer.valueOf(this.getPaddingRight()), Integer.valueOf(this.getPaddingTop()), Integer.valueOf(this.getPaddingBottom())});
        }

        MarginLayoutParams lp;
        if(this.mHeaderView != null) {
            this.measureChildWithMargins(this.mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            lp = (MarginLayoutParams)this.mHeaderView.getLayoutParams();
            this.mHeaderHeight = this.mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            this.mPtrIndicator.setHeaderHeight(this.mHeaderHeight);
        }

        if(this.mContent != null) {
            this.measureContentView(this.mContent, widthMeasureSpec, heightMeasureSpec);
            if(DEBUG) {
                lp = (MarginLayoutParams)this.mContent.getLayoutParams();
                PtrCLog.d(this.LOG_TAG, "onMeasure content, width: %s, height: %s, margin: %s %s %s %s", new Object[]{Integer.valueOf(this.getMeasuredWidth()), Integer.valueOf(this.getMeasuredHeight()), Integer.valueOf(lp.leftMargin), Integer.valueOf(lp.topMargin), Integer.valueOf(lp.rightMargin), Integer.valueOf(lp.bottomMargin)});
                PtrCLog.d(this.LOG_TAG, "onMeasure, currentPos: %s, lastPos: %s, top: %s", new Object[]{Integer.valueOf(this.mPtrIndicator.getCurrentPosY()), Integer.valueOf(this.mPtrIndicator.getLastPosY()), Integer.valueOf(this.mContent.getTop())});
            }
        }

        if(this.mFooterView != null) {
            this.measureChildWithMargins(this.mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            lp = (MarginLayoutParams)this.mFooterView.getLayoutParams();
            this.mFooterHeight = this.mFooterView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            this.mPtrIndicator.setFooterHeight(this.mFooterHeight);
        }

    }

    private void measureContentView(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        MarginLayoutParams lp = (MarginLayoutParams)child.getLayoutParams();
        int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom() + lp.topMargin, lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    protected void onLayout(boolean flag, int i, int j, int k, int l) {
        this.layoutChildren();
    }

    private void layoutChildren() {
        int offsetHeaderY;
        int offsetFooterY;
        if(this.mPtrIndicator.isHeader()) {
            offsetHeaderY = this.mPtrIndicator.getCurrentPosY();
            offsetFooterY = 0;
        } else {
            offsetHeaderY = 0;
            offsetFooterY = this.mPtrIndicator.getCurrentPosY();
        }

        int paddingLeft = this.getPaddingLeft();
        int paddingTop = this.getPaddingTop();
        int contentBottom = 0;
        if(DEBUG) {
            PtrCLog.d(this.LOG_TAG, "onLayout offset: %s %s %s %s", new Object[]{Integer.valueOf(offsetHeaderY), Integer.valueOf(offsetFooterY), Boolean.valueOf(this.isPinContent()), Boolean.valueOf(this.mPtrIndicator.isHeader())});
        }

        MarginLayoutParams lp;
        int left;
        int top;
        int right;
        int bottom;
        if(this.mHeaderView != null) {
            lp = (MarginLayoutParams)this.mHeaderView.getLayoutParams();
            left = paddingLeft + lp.leftMargin;
            top = paddingTop + lp.topMargin + offsetHeaderY - this.mHeaderHeight;
            right = left + this.mHeaderView.getMeasuredWidth();
            bottom = top + this.mHeaderView.getMeasuredHeight();
            this.mHeaderView.layout(left, top, right, bottom);
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "onLayout header: %s %s %s %s %s", new Object[]{Integer.valueOf(left), Integer.valueOf(top), Integer.valueOf(right), Integer.valueOf(bottom), Integer.valueOf(this.mHeaderView.getMeasuredHeight())});
            }
        }

        if(this.mContent != null) {
            lp = (MarginLayoutParams)this.mContent.getLayoutParams();
            if(this.mPtrIndicator.isHeader()) {
                left = paddingLeft + lp.leftMargin;
                top = paddingTop + lp.topMargin + (this.isPinContent()?0:offsetHeaderY);
                right = left + this.mContent.getMeasuredWidth();
                bottom = top + this.mContent.getMeasuredHeight();
            } else {
                left = paddingLeft + lp.leftMargin;
                top = paddingTop + lp.topMargin - (this.isPinContent()?0:offsetFooterY);
                right = left + this.mContent.getMeasuredWidth();
                bottom = top + this.mContent.getMeasuredHeight();
            }

            contentBottom = bottom;
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "onLayout content: %s %s %s %s %s", new Object[]{Integer.valueOf(left), Integer.valueOf(top), Integer.valueOf(right), Integer.valueOf(bottom), Integer.valueOf(this.mContent.getMeasuredHeight())});
            }

            this.mContent.layout(left, top, right, bottom);
        }

        if(this.mFooterView != null) {
            lp = (MarginLayoutParams)this.mFooterView.getLayoutParams();
            left = paddingLeft + lp.leftMargin;
            top = paddingTop + lp.topMargin + contentBottom - (this.isPinContent()?offsetFooterY:0);
            right = left + this.mFooterView.getMeasuredWidth();
            bottom = top + this.mFooterView.getMeasuredHeight();
            this.mFooterView.layout(left, top, right, bottom);
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "onLayout footer: %s %s %s %s %s", new Object[]{Integer.valueOf(left), Integer.valueOf(top), Integer.valueOf(right), Integer.valueOf(bottom), Integer.valueOf(this.mFooterView.getMeasuredHeight())});
            }
        }

    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }


    private int startX;
    private int startY;
    public boolean dispatchTouchEvent(MotionEvent e) {
        if(this.isEnabled() && this.mContent != null && this.mHeaderView != null) {
            int action = e.getAction();
            switch(action) {
                case MotionEvent.ACTION_DOWN:
                    this.downTime = System.currentTimeMillis();
                    this.mHasSendCancelEvent = false;
                    this.mPtrIndicator.onPressDown(e.getX(), e.getY());
                    this.mScrollChecker.abortIfWorking();
                    this.mPreventForHorizontal = false;
                    this.dispatchTouchEventSupper(e);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    this.mPtrIndicator.onRelease();
                    if(this.mPtrIndicator.hasLeftStartPosition()) {
                        if(DEBUG) {
                            PtrCLog.d(this.LOG_TAG, "call onRelease when user release");
                        }

                        this.onRelease(false);
                        if(this.mPtrIndicator.hasMovedAfterPressedDown()) {
                            this.sendCancelEvent();
                            return true;
                        }

                        return this.dispatchTouchEventSupper(e);
                    }

                    return this.dispatchTouchEventSupper(e);
                case MotionEvent.ACTION_MOVE:
                    this.mLastMoveEvent = e;
                    this.mPtrIndicator.onMove(e.getX(), e.getY());
                    float offsetX = this.mPtrIndicator.getOffsetX();
                    float offsetY = this.mPtrIndicator.getOffsetY();
                    if(this.mDisableWhenHorizontalMove && !this.mPreventForHorizontal && Math.abs(offsetX) > (float)this.mPagingTouchSlop && Math.abs(offsetX) > Math.abs(offsetY) && this.mPtrIndicator.isInStartPosition()) {
                        this.mPreventForHorizontal = true;
                    }

                    if(this.mPreventForHorizontal) {
                        return this.dispatchTouchEventSupper(e);
                    } else {
                        boolean moveDown = offsetY > 0.0F;
                        boolean moveUp = !moveDown;
                        boolean canMoveUp = this.mPtrIndicator.isHeader() && this.mPtrIndicator.hasLeftStartPosition();
                        boolean canMoveDown = this.mFooterView != null && !this.mPtrIndicator.isHeader() && this.mPtrIndicator.hasLeftStartPosition();
                        boolean canHeaderMoveDown = this.mPtrHandler != null && this.mPtrHandler.checkCanDoRefresh(this, this.mContent, this.mHeaderView) && (this.mMode.ordinal() & 1) > 0;
                        boolean canFooterMoveUp = this.mPtrHandler != null && this.mFooterView != null && this.mPtrHandler instanceof PtrHandler2 && ((PtrHandler2)this.mPtrHandler).checkCanDoLoad(this, this.mContent, this.mFooterView) && (this.mMode.ordinal() & 2) > 0;
                        if(DEBUG) {
                            PtrCLog.v(this.LOG_TAG, "ACTION_MOVE: offsetY:%s, currentPos: %s, moveUp: %s, canMoveUp: %s, moveDown: %s: canMoveDown: %s canHeaderMoveDown: %s canFooterMoveUp: %s", new Object[]{Float.valueOf(offsetY), Integer.valueOf(this.mPtrIndicator.getCurrentPosY()), Boolean.valueOf(moveUp), Boolean.valueOf(canMoveUp), Boolean.valueOf(moveDown), Boolean.valueOf(canMoveDown), Boolean.valueOf(canHeaderMoveDown), Boolean.valueOf(canFooterMoveUp)});
                        }

                        if(!canMoveUp && !canMoveDown) {
                            if(moveDown && !canHeaderMoveDown) {
                                return this.dispatchTouchEventSupper(e);
                            }

                            if(moveUp && !canFooterMoveUp) {
                                return this.dispatchTouchEventSupper(e);
                            }

                            if(moveDown) {
                                this.moveHeaderPos(offsetY);
                                return true;
                            }

                            if(moveUp) {
                                this.moveFooterPos(offsetY);
                                return true;
                            }
                        }

                        if(canMoveUp) {
                            this.moveHeaderPos(offsetY);
                            return true;
                        } else if(canMoveDown && this.mStatus != 4) {
                            this.moveFooterPos(offsetY);
                            return true;
                        }
                    }
                default:
                    return this.dispatchTouchEventSupper(e);
            }
        } else {
            return this.dispatchTouchEventSupper(e);
        }
    }

    private void moveFooterPos(float deltaY) {
        this.mPtrIndicator.setIsHeader(false);
        this.mHeaderView.setVisibility(View.GONE);
        this.mFooterView.setVisibility(View.VISIBLE);
        this.movePos(-deltaY);
    }

    private void moveHeaderPos(float deltaY) {
        this.mPtrIndicator.setIsHeader(true);
        this.mHeaderView.setVisibility(View.VISIBLE);
        this.mFooterView.setVisibility(View.GONE);
        this.movePos(deltaY);
    }

    private void movePos(float deltaY) {
        if(deltaY < 0.0F && this.mPtrIndicator.isInStartPosition()) {
            if(DEBUG) {
                PtrCLog.e(this.LOG_TAG, String.format("has reached the top", new Object[0]));
            }

        } else {
            int to = this.mPtrIndicator.getCurrentPosY() + (int)deltaY;
            if(this.mPtrIndicator.willOverTop(to)) {
                if(DEBUG) {
                    PtrCLog.e(this.LOG_TAG, String.format("over top", new Object[0]));
                }

                to = 0;
            }

            this.mPtrIndicator.setCurrentPos(to);
            int change = to - this.mPtrIndicator.getLastPosY();
            this.updatePos(this.mPtrIndicator.isHeader()?change:-change);
        }
    }

    private void updatePos(int change) {
        if(change != 0) {
            boolean isUnderTouch = this.mPtrIndicator.isUnderTouch();
            if(isUnderTouch && !this.mHasSendCancelEvent && this.mPtrIndicator.hasMovedAfterPressedDown()) {
                this.mHasSendCancelEvent = true;
                this.sendCancelEvent();
            }

            if(this.mPtrIndicator.hasJustLeftStartPosition() && this.mStatus == 1 || this.mPtrIndicator.goDownCrossFinishPosition() && this.mStatus == 4 && this.isEnabledNextPtrAtOnce()) {
                this.mStatus = 2;
                this.mPtrUIHandlerHolder.onUIRefreshPrepare(this);
                if(DEBUG) {
                    PtrCLog.i(this.LOG_TAG, "PtrUIHandler: onUIRefreshPrepare, mFlag %s", new Object[]{Integer.valueOf(this.mFlag)});
                }
            }

            if(this.mPtrIndicator.hasJustBackToStartPosition()) {
                this.tryToNotifyReset();
                if(isUnderTouch) {
                    this.sendDownEvent();
                }
            }

            if(this.mStatus == 2) {
                if(isUnderTouch && !this.isAutoRefresh() && this.mPullToRefresh && this.mPtrIndicator.crossRefreshLineFromTopToBottom()) {
                    this.tryToPerformRefresh();
                }

                if(this.performAutoRefreshButLater() && this.mPtrIndicator.hasJustReachedHeaderHeightFromTopToBottom()) {
                    this.tryToPerformRefresh();
                }
            }

            if(DEBUG) {
                PtrCLog.v(this.LOG_TAG, "updatePos: change: %s, current: %s last: %s, top: %s, headerHeight: %s", new Object[]{Integer.valueOf(change), Integer.valueOf(this.mPtrIndicator.getCurrentPosY()), Integer.valueOf(this.mPtrIndicator.getLastPosY()), Integer.valueOf(this.mContent.getTop()), Integer.valueOf(this.mHeaderHeight)});
            }

            if(this.mPtrIndicator.isHeader()) {
                this.mHeaderView.offsetTopAndBottom(change);
            } else {
                this.mFooterView.offsetTopAndBottom(change);
            }

            if(!this.isPinContent()) {
                this.mContent.offsetTopAndBottom(change);
            }

            this.invalidate();
            if(this.mPtrUIHandlerHolder.hasHandler()) {
                this.mPtrUIHandlerHolder.onUIPositionChange(this, isUnderTouch, this.mStatus, this.mPtrIndicator);
            }

            this.onPositionChange(isUnderTouch, this.mStatus, this.mPtrIndicator);
        }
    }

    protected void onPositionChange(boolean isInTouching, byte status, PtrIndicator mPtrIndicator) {
    }

    public int getHeaderHeight() {
        return this.mHeaderHeight;
    }

    public int getFooterHeight() {
        return this.mFooterHeight;
    }

    private void onRelease(boolean stayForLoading) {
        this.tryToPerformRefresh();
        if(this.mStatus == 3) {
            if(this.mKeepHeaderWhenRefresh) {
                if(this.mPtrIndicator.isOverOffsetToKeepHeaderWhileLoading() && !stayForLoading) {
                    this.mScrollChecker.tryToScrollTo(this.mPtrIndicator.getOffsetToKeepHeaderWhileLoading(), this.mDurationToClose);
                }
            } else {
                this.tryScrollBackToTopWhileLoading();
            }
        } else if(this.mStatus == 4) {
            this.notifyUIRefreshComplete(false);
        } else {
            this.tryScrollBackToTopAbortRefresh();
        }

    }

    public void setRefreshCompleteHook(PtrUIHandlerHook hook) {
        this.mRefreshCompleteHook = hook;
        hook.setResumeAction(new Runnable() {
            public void run() {
                if(PtrFrameLayout.DEBUG) {
                    PtrCLog.d(PtrFrameLayout.this.LOG_TAG, "mRefreshCompleteHook resume.");
                }

                PtrFrameLayout.this.notifyUIRefreshComplete(true);
            }
        });
    }

    private void tryScrollBackToTop() {
        if(!this.mPtrIndicator.isUnderTouch() && this.mPtrIndicator.hasLeftStartPosition()) {
            this.mScrollChecker.tryToScrollTo(0, this.mDurationToCloseHeader);
        }

    }

    private void tryScrollBackToTopWhileLoading() {
        this.tryScrollBackToTop();
    }

    private void tryScrollBackToTopAfterComplete() {
        this.tryScrollBackToTop();
    }

    private void tryScrollBackToTopAbortRefresh() {
        this.tryScrollBackToTop();
    }

    private boolean tryToPerformRefresh() {
        if(this.mStatus != 2) {
            return false;
        } else {
            if(this.mPtrIndicator.isOverOffsetToKeepHeaderWhileLoading() && this.isAutoRefresh() || this.mPtrIndicator.isOverOffsetToRefresh()) {
                this.mStatus = 3;
                this.performRefresh();
            }

            return false;
        }
    }

    private void performRefresh() {
        this.mLoadingStartTime = System.currentTimeMillis();
        if(this.mPtrUIHandlerHolder.hasHandler()) {
            this.mPtrUIHandlerHolder.onUIRefreshBegin(this);
            if(DEBUG) {
                PtrCLog.i(this.LOG_TAG, "PtrUIHandler: onUIRefreshBegin");
            }
        }

        if(this.mPtrHandler != null) {
            if(this.mPtrIndicator.isHeader()) {
                this.mPtrHandler.onRefreshBegin(this);
            } else if(this.mPtrHandler instanceof PtrHandler2) {
                ((PtrHandler2)this.mPtrHandler).onLoadBegin(this);
            }
        }

    }

    private boolean tryToNotifyReset() {
        if((this.mStatus == 4 || this.mStatus == 2) && this.mPtrIndicator.isInStartPosition()) {
            if(this.mPtrUIHandlerHolder.hasHandler()) {
                this.mPtrUIHandlerHolder.onUIReset(this);
                if(DEBUG) {
                    PtrCLog.i(this.LOG_TAG, "PtrUIHandler: onUIReset");
                }
            }

            this.mStatus = 1;
            this.clearFlag();
            return true;
        } else {
            return false;
        }
    }

    protected void onPtrScrollAbort() {
        if(this.mPtrIndicator.hasLeftStartPosition() && this.isAutoRefresh()) {
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "call onRelease after scroll abort");
            }

            this.onRelease(true);
        }

    }

    protected void onPtrScrollFinish() {
        if(this.mPtrIndicator.hasLeftStartPosition() && this.isAutoRefresh()) {
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "call onRelease after scroll finish");
            }

            this.onRelease(true);
        }

    }

    public boolean isRefreshing() {
        return this.mStatus == 3;
    }

    public final void refreshComplete() {
        if(DEBUG) {
            PtrCLog.i(this.LOG_TAG, "refreshComplete");
        }

        if(this.mRefreshCompleteHook != null) {
            this.mRefreshCompleteHook.reset();
        }

        int delay = (int)((long)this.mLoadingMinTime - (System.currentTimeMillis() - this.mLoadingStartTime));
        if(delay <= 0) {
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "performRefreshComplete at once");
            }

            this.performRefreshComplete();
        } else {
            this.postDelayed(this.mPerformRefreshCompleteDelay, (long)delay);
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "performRefreshComplete after delay: %s", new Object[]{Integer.valueOf(delay)});
            }
        }

    }

    private void performRefreshComplete() {
        this.mStatus = 4;
        if(this.mScrollChecker.mIsRunning && this.isAutoRefresh()) {
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "performRefreshComplete do nothing, scrolling: %s, auto refresh: %s", new Object[]{Boolean.valueOf(this.mScrollChecker.mIsRunning), Integer.valueOf(this.mFlag)});
            }

        } else {
            this.notifyUIRefreshComplete(false);
        }
    }

    private void notifyUIRefreshComplete(boolean ignoreHook) {
        if(this.mPtrIndicator.hasLeftStartPosition() && !ignoreHook && this.mRefreshCompleteHook != null) {
            if(DEBUG) {
                PtrCLog.d(this.LOG_TAG, "notifyUIRefreshComplete mRefreshCompleteHook run.");
            }

            this.mRefreshCompleteHook.takeOver();
        } else {
            if(this.mPtrUIHandlerHolder.hasHandler()) {
                if(DEBUG) {
                    PtrCLog.i(this.LOG_TAG, "PtrUIHandler: onUIRefreshComplete");
                }

                this.mPtrUIHandlerHolder.onUIRefreshComplete(this);
            }

            this.mPtrIndicator.onUIRefreshComplete();
            this.tryScrollBackToTopAfterComplete();
            this.tryToNotifyReset();
        }
    }

    public void autoRefresh() {
        this.autoRefresh(true, this.mDurationToCloseHeader);
    }

    public void autoRefresh(boolean atOnce) {
        this.autoRefresh(atOnce, this.mDurationToCloseHeader);
    }

    private void clearFlag() {
        this.mFlag &= ~MASK_AUTO_REFRESH;
    }

    public void autoLoadMore() {
        this.autoRefresh(true, this.mDurationToCloseHeader, false);
    }

    public void autoLoadMore(boolean atOnce) {
        this.autoRefresh(atOnce, this.mDurationToCloseHeader, false);
    }

    public void autoRefresh(boolean atOnce, int duration) {
        this.autoRefresh(atOnce, duration, true);
    }

    public void autoRefresh(boolean atOnce, int duration, boolean isHeader) {
        if(this.mStatus != 1) {
            if(this.isEnabledNextPtrAtOnce()) {
                this.tryToNotifyReset();
            } else if(this.mStatus != 1) {
                return;
            }
        }

        this.mFlag |= atOnce?FLAG_AUTO_REFRESH_AT_ONCE:FLAG_AUTO_REFRESH_BUT_LATER;
        this.mStatus = 2;
        if(this.mPtrUIHandlerHolder.hasHandler()) {
            this.mPtrUIHandlerHolder.onUIRefreshPrepare(this);
            if(DEBUG) {
                PtrCLog.i(this.LOG_TAG, "PtrUIHandler: onUIRefreshPrepare, mFlag %s", new Object[]{Integer.valueOf(this.mFlag)});
            }
        }

        this.mPtrIndicator.setIsHeader(isHeader);
        this.mScrollChecker.tryToScrollTo(this.mPtrIndicator.getOffsetToRefresh(), duration);
        if(atOnce) {
            this.mStatus = 3;
            this.performRefresh();
        }

    }

    public boolean isAutoRefresh() {
        return (this.mFlag & MASK_AUTO_REFRESH) > 0;
    }

    private boolean performAutoRefreshButLater() {
        return (this.mFlag & MASK_AUTO_REFRESH) == FLAG_AUTO_REFRESH_BUT_LATER;
    }

    public boolean isEnabledNextPtrAtOnce() {
        return (this.mFlag & FLAG_ENABLE_NEXT_PTR_AT_ONCE) > 0;
    }

    public void setEnabledNextPtrAtOnce(boolean enable) {
        if(enable) {
            this.mFlag |= FLAG_ENABLE_NEXT_PTR_AT_ONCE;
        } else {
            this.mFlag &= ~FLAG_ENABLE_NEXT_PTR_AT_ONCE;
        }

    }

    public boolean isPinContent() {
        return (this.mFlag & FLAG_PIN_CONTENT) > 0;
    }

    public void setPinContent(boolean pinContent) {
        if(pinContent) {
            this.mFlag |= FLAG_PIN_CONTENT;
        } else {
            this.mFlag &= ~FLAG_PIN_CONTENT;
        }

    }

    public void disableWhenHorizontalMove(boolean disable) {
        this.mDisableWhenHorizontalMove = disable;
    }

    public void setLoadingMinTime(int time) {
        this.mLoadingMinTime = time;
    }

    /** @deprecated */
    @Deprecated
    public void setInterceptEventWhileWorking(boolean yes) {
    }

    public View getContentView() {
        return this.mContent;
    }

    public void setPtrHandler(PtrHandler ptrHandler) {
        this.mPtrHandler = ptrHandler;
    }

    public void addPtrUIHandler(PtrUIHandler ptrUIHandler) {
        PtrUIHandlerHolder.addHandler(this.mPtrUIHandlerHolder, ptrUIHandler);
    }

    public void removePtrUIHandler(PtrUIHandler ptrUIHandler) {
        this.mPtrUIHandlerHolder = PtrUIHandlerHolder.removeHandler(this.mPtrUIHandlerHolder, ptrUIHandler);
    }

    public void setPtrIndicator(PtrIndicator slider) {
        if(this.mPtrIndicator != null && this.mPtrIndicator != slider) {
            slider.convertFrom(this.mPtrIndicator);
        }

        this.mPtrIndicator = slider;
    }

    public void setMode(PtrFrameLayout.Mode mode) {
        this.mMode = mode;
    }

    public PtrFrameLayout.Mode getMode() {
        return this.mMode;
    }

    public float getResistance() {
        return this.mPtrIndicator.getResistance();
    }

    public void setResistance(float resistance) {
        this.mPtrIndicator.setResistance(resistance);
    }

    public float getDurationToClose() {
        return (float)this.mDurationToClose;
    }

    public void setDurationToClose(int duration) {
        this.mDurationToClose = duration;
    }

    public long getDurationToCloseHeader() {
        return (long)this.mDurationToCloseHeader;
    }

    public void setDurationToCloseHeader(int duration) {
        this.mDurationToCloseHeader = duration;
    }

    public void setRatioOfHeaderHeightToRefresh(float ratio) {
        this.mPtrIndicator.setRatioOfHeaderHeightToRefresh(ratio);
    }

    public int getOffsetToRefresh() {
        return this.mPtrIndicator.getOffsetToRefresh();
    }

    public void setOffsetToRefresh(int offset) {
        this.mPtrIndicator.setOffsetToRefresh(offset);
    }

    public float getRatioOfHeaderToHeightRefresh() {
        return this.mPtrIndicator.getRatioOfHeaderToHeightRefresh();
    }

    public int getOffsetToKeepHeaderWhileLoading() {
        return this.mPtrIndicator.getOffsetToKeepHeaderWhileLoading();
    }

    public void setOffsetToKeepHeaderWhileLoading(int offset) {
        this.mPtrIndicator.setOffsetToKeepHeaderWhileLoading(offset);
    }

    public boolean isKeepHeaderWhenRefresh() {
        return this.mKeepHeaderWhenRefresh;
    }

    public void setKeepHeaderWhenRefresh(boolean keepOrNot) {
        this.mKeepHeaderWhenRefresh = keepOrNot;
    }

    public boolean isPullToRefresh() {
        return this.mPullToRefresh;
    }

    public void setPullToRefresh(boolean pullToRefresh) {
        this.mPullToRefresh = pullToRefresh;
    }

    public View getHeaderView() {
        return this.mHeaderView;
    }

    public void setHeaderView(View header) {
        if(this.mHeaderView != null && header != null && this.mHeaderView != header) {
            this.removeView(this.mHeaderView);
        }

        android.view.ViewGroup.LayoutParams lp = header.getLayoutParams();
        if(lp == null) {
            PtrFrameLayout.LayoutParams lp1 = new PtrFrameLayout.LayoutParams(-1, -2);
            header.setLayoutParams(lp1);
        }

        this.mHeaderView = header;
        this.addView(header);
    }

    public void setFooterView(View footer) {
        if(this.mFooterView != null && footer != null && this.mFooterView != footer) {
            this.removeView(this.mFooterView);
        }

        android.view.ViewGroup.LayoutParams lp = footer.getLayoutParams();
        if(lp == null) {
            PtrFrameLayout.LayoutParams lp1 = new PtrFrameLayout.LayoutParams(-1, -2);
            footer.setLayoutParams(lp1);
        }

        this.mFooterView = footer;
        this.addView(footer);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p != null && p instanceof PtrFrameLayout.LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new PtrFrameLayout.LayoutParams(-1, -1);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new PtrFrameLayout.LayoutParams(p);
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new PtrFrameLayout.LayoutParams(this.getContext(), attrs);
    }

    private void sendCancelEvent() {
        if(DEBUG) {
            PtrCLog.d(this.LOG_TAG, "send cancel event");
        }

        if(this.mLastMoveEvent != null) {
            MotionEvent last = this.mLastMoveEvent;
            MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + (long)ViewConfiguration.getLongPressTimeout(), 3, last.getX(), last.getY(), last.getMetaState());
            this.dispatchTouchEventSupper(e);
        }
    }

    private void sendDownEvent() {
        if(DEBUG) {
            PtrCLog.d(this.LOG_TAG, "send down event");
        }

        MotionEvent last = this.mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), 0, last.getX(), last.getY(), last.getMetaState());
        this.dispatchTouchEventSupper(e);
    }

    class ScrollChecker implements Runnable {
        private int mLastFlingY;
        private Scroller mScroller = new Scroller(PtrFrameLayout.this.getContext());
        private boolean mIsRunning = false;
        private int mStart;
        private int mTo;

        public ScrollChecker() {
        }

        public void run() {
            boolean finish = !this.mScroller.computeScrollOffset() || this.mScroller.isFinished();
            int curY = this.mScroller.getCurrY();
            int deltaY = curY - this.mLastFlingY;
            if(PtrFrameLayout.DEBUG && deltaY != 0) {
                PtrCLog.v(PtrFrameLayout.this.LOG_TAG, "scroll: %s, start: %s, to: %s, currentPos: %s, current :%s, last: %s, delta: %s", new Object[]{Boolean.valueOf(finish), Integer.valueOf(this.mStart), Integer.valueOf(this.mTo), Integer.valueOf(PtrFrameLayout.this.mPtrIndicator.getCurrentPosY()), Integer.valueOf(curY), Integer.valueOf(this.mLastFlingY), Integer.valueOf(deltaY)});
            }

            if(!finish) {
                this.mLastFlingY = curY;
                if(PtrFrameLayout.this.mPtrIndicator.isHeader()) {
                    PtrFrameLayout.this.moveHeaderPos((float)deltaY);
                } else {
                    PtrFrameLayout.this.moveFooterPos((float)(-deltaY));
                }

                PtrFrameLayout.this.post(this);
            } else {
                this.finish();
            }

        }

        public boolean isRunning() {
            return this.mScroller.isFinished();
        }

        private void finish() {
            if(PtrFrameLayout.DEBUG) {
                PtrCLog.v(PtrFrameLayout.this.LOG_TAG, "finish, currentPos:%s", new Object[]{Integer.valueOf(PtrFrameLayout.this.mPtrIndicator.getCurrentPosY())});
            }

            this.reset();
            PtrFrameLayout.this.onPtrScrollFinish();
        }

        private void reset() {
            this.mIsRunning = false;
            this.mLastFlingY = 0;
            PtrFrameLayout.this.removeCallbacks(this);
        }

        private void destroy() {
            this.reset();
            if(!this.mScroller.isFinished()) {
                this.mScroller.forceFinished(true);
            }

        }

        public void abortIfWorking() {
            if(this.mIsRunning) {
                if(!this.mScroller.isFinished()) {
                    this.mScroller.forceFinished(true);
                }

                PtrFrameLayout.this.onPtrScrollAbort();
                this.reset();
            }

        }

        public void tryToScrollTo(int to, int duration) {
            if(!PtrFrameLayout.this.mPtrIndicator.isAlreadyHere(to)) {
                this.mStart = PtrFrameLayout.this.mPtrIndicator.getCurrentPosY();
                this.mTo = to;
                int distance = to - this.mStart;
                if(PtrFrameLayout.DEBUG) {
                    PtrCLog.d(PtrFrameLayout.this.LOG_TAG, "tryToScrollTo: start: %s, distance:%s, to:%s", new Object[]{Integer.valueOf(this.mStart), Integer.valueOf(distance), Integer.valueOf(to)});
                }

                PtrFrameLayout.this.removeCallbacks(this);
                this.mLastFlingY = 0;
                if(!this.mScroller.isFinished()) {
                    this.mScroller.forceFinished(true);
                }

                this.mScroller.startScroll(0, 0, 0, distance, duration);
                PtrFrameLayout.this.post(this);
                this.mIsRunning = true;
            }
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public static enum Mode {
        NONE,
        REFRESH,
        LOAD_MORE,
        BOTH;

        private Mode() {
        }
    }
}
