package in.srain.cube.views.ptr.indicator;

import android.graphics.PointF;

public class PtrIndicator {
    public static final int POS_START = 0;
    protected int mOffsetToRefresh = 0;
    protected int mOffsetToLoadMore = 0;
    private PointF mPtLastMove = new PointF();
    private float mOffsetX;
    private float mOffsetY;
    private int mCurrentPos = 0;
    private int mLastPos = 0;
    private int mHeaderHeight;
    private int mFooterHeight;
    private int mPressedPos = 0;
    private boolean isHeader = true;
    private float mRatioOfHeaderHeightToRefresh = 1.2F;
    private float mResistance = 1.7F;
    private boolean mIsUnderTouch = false;
    private int mOffsetToKeepHeaderWhileLoading = -1;
    private int mRefreshCompleteY = 0;

    public PtrIndicator() {
    }

    public boolean isHeader() {
        return this.isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public boolean isUnderTouch() {
        return this.mIsUnderTouch;
    }

    public float getResistance() {
        return this.mResistance;
    }

    public void setResistance(float resistance) {
        this.mResistance = resistance;
    }

    public void onRelease() {
        this.mIsUnderTouch = false;
    }

    public void onUIRefreshComplete() {
        this.mRefreshCompleteY = this.mCurrentPos;
    }

    public boolean goDownCrossFinishPosition() {
        return this.mCurrentPos >= this.mRefreshCompleteY;
    }

    protected void processOnMove(float currentX, float currentY, float offsetX, float offsetY) {
        this.setOffset(offsetX, offsetY / this.mResistance);
    }

    public void setRatioOfHeaderHeightToRefresh(float ratio) {
        this.mRatioOfHeaderHeightToRefresh = ratio;
        this.mOffsetToRefresh = (int)((float)this.mHeaderHeight * ratio);
        this.mOffsetToLoadMore = (int)((float)this.mFooterHeight * ratio);
    }

    public float getRatioOfHeaderToHeightRefresh() {
        return this.mRatioOfHeaderHeightToRefresh;
    }

    public int getOffstToLoadMore() {
        return this.mOffsetToLoadMore;
    }

    public int getOffsetToRefresh() {
        return this.mOffsetToRefresh;
    }

    public void setOffsetToRefresh(int offset) {
        this.mRatioOfHeaderHeightToRefresh = (float)this.mHeaderHeight * 1.0F / (float)offset;
        this.mOffsetToRefresh = offset;
        this.mOffsetToLoadMore = offset;
    }

    public void onPressDown(float x, float y) {
        this.mIsUnderTouch = true;
        this.mPressedPos = this.mCurrentPos;
        this.mPtLastMove.set(x, y);
    }

    public final void onMove(float x, float y) {
        float offsetX = x - this.mPtLastMove.x;
        float offsetY = y - this.mPtLastMove.y;
        this.processOnMove(x, y, offsetX, offsetY);
        this.mPtLastMove.set(x, y);
    }

    protected void setOffset(float x, float y) {
        this.mOffsetX = x;
        this.mOffsetY = y;
    }

    public float getOffsetX() {
        return this.mOffsetX;
    }

    public float getOffsetY() {
        return this.mOffsetY;
    }

    public int getLastPosY() {
        return this.mLastPos;
    }

    public int getCurrentPosY() {
        return this.mCurrentPos;
    }

    public final void setCurrentPos(int current) {
        this.mLastPos = this.mCurrentPos;
        this.mCurrentPos = current;
        this.onUpdatePos(current, this.mLastPos);
    }

    protected void onUpdatePos(int current, int last) {
    }

    public int getHeaderHeight() {
        return this.mHeaderHeight;
    }

    public void setHeaderHeight(int height) {
        this.mHeaderHeight = height;
        this.updateHeight();
    }

    public void setFooterHeight(int height) {
        this.mFooterHeight = height;
        this.updateHeight();
    }

    protected void updateHeight() {
        this.mOffsetToRefresh = (int)(this.mRatioOfHeaderHeightToRefresh * (float)this.mHeaderHeight);
        this.mOffsetToLoadMore = (int)(this.mRatioOfHeaderHeightToRefresh * (float)this.mFooterHeight);
    }

    public void convertFrom(PtrIndicator ptrSlider) {
        this.mCurrentPos = ptrSlider.mCurrentPos;
        this.mLastPos = ptrSlider.mLastPos;
        this.mHeaderHeight = ptrSlider.mHeaderHeight;
    }

    public boolean hasLeftStartPosition() {
        return this.mCurrentPos > 0;
    }

    public boolean hasJustLeftStartPosition() {
        return this.mLastPos == 0 && this.hasLeftStartPosition();
    }

    public boolean hasJustBackToStartPosition() {
        return this.mLastPos != 0 && this.isInStartPosition();
    }

    public boolean isOverOffsetToRefresh() {
        return this.mCurrentPos >= this.getOffsetToRefresh();
    }

    public boolean hasMovedAfterPressedDown() {
        return this.mCurrentPos != this.mPressedPos;
    }

    public boolean isInStartPosition() {
        return this.mCurrentPos == 0;
    }

    public boolean crossRefreshLineFromTopToBottom() {
        return this.mLastPos < this.getOffsetToRefresh() && this.mCurrentPos >= this.getOffsetToRefresh();
    }

    public boolean hasJustReachedHeaderHeightFromTopToBottom() {
        return this.mLastPos < this.mHeaderHeight && this.mCurrentPos >= this.mHeaderHeight;
    }

    public boolean isOverOffsetToKeepHeaderWhileLoading() {
        return this.mCurrentPos > this.getOffsetToKeepHeaderWhileLoading();
    }

    public void setOffsetToKeepHeaderWhileLoading(int offset) {
        this.mOffsetToKeepHeaderWhileLoading = offset;
    }

    public int getOffsetToKeepHeaderWhileLoading() {
        return this.mOffsetToKeepHeaderWhileLoading >= 0?this.mOffsetToKeepHeaderWhileLoading:this.mHeaderHeight;
    }

    public boolean isAlreadyHere(int to) {
        return this.mCurrentPos == to;
    }

    public float getLastPercent() {
        float oldPercent = this.mHeaderHeight == 0?0.0F:(float)this.mLastPos * 1.0F / (float)this.mHeaderHeight;
        return oldPercent;
    }

    public float getCurrentPercent() {
        float currentPercent = this.mHeaderHeight == 0?0.0F:(float)this.mCurrentPos * 1.0F / (float)this.mHeaderHeight;
        return currentPercent;
    }

    public boolean willOverTop(int to) {
        return to < 0;
    }
}
