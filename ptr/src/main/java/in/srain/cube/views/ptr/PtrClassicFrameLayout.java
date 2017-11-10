package in.srain.cube.views.ptr;

import android.content.Context;
import android.util.AttributeSet;

public class PtrClassicFrameLayout extends PtrHTFrameLayout {
    private PtrClassicDefaultHeader mPtrClassicHeader;
    private PtrClassicDefaultFooter mPtrClassicFooter;

    public PtrClassicFrameLayout(Context context) {
        super(context);
        this.initViews();
    }

    public PtrClassicFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initViews();
    }

    public PtrClassicFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initViews();
    }

    private void initViews() {
        this.mPtrClassicHeader = new PtrClassicDefaultHeader(this.getContext());
        this.setHeaderView(this.mPtrClassicHeader);
        this.addPtrUIHandler(this.mPtrClassicHeader);
        this.mPtrClassicFooter = new PtrClassicDefaultFooter(this.getContext());
        this.setFooterView(this.mPtrClassicFooter);
        this.addPtrUIHandler(this.mPtrClassicFooter);
    }

    public PtrClassicDefaultHeader getHeader() {
        return this.mPtrClassicHeader;
    }

    public void setLastUpdateTimeKey(String key) {
        this.setLastUpdateTimeHeaderKey(key);
        this.setLastUpdateTimeFooterKey(key);
    }

    public void setLastUpdateTimeHeaderKey(String key) {
        if(this.mPtrClassicHeader != null) {
            this.mPtrClassicHeader.setLastUpdateTimeKey(key);
        }
    }

    public void setLastUpdateTimeFooterKey(String key) {
        if(this.mPtrClassicFooter != null) {
            this.mPtrClassicFooter.setLastUpdateTimeKey(key);
        }
    }

    public void setLastUpdateTimeRelateObject(Object object) {
        this.setLastUpdateTimeHeaderRelateObject(object);
        this.setLastUpdateTimeFooterRelateObject(object);
    }

    public void setLastUpdateTimeHeaderRelateObject(Object object) {
        if(this.mPtrClassicHeader != null) {
            this.mPtrClassicHeader.setLastUpdateTimeRelateObject(object);
        }
    }

    public void setLastUpdateTimeFooterRelateObject(Object object) {
        if(this.mPtrClassicFooter != null) {
            this.mPtrClassicFooter.setLastUpdateTimeRelateObject(object);
        }
    }
}