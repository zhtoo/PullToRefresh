package in.srain.cube.views.ptr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

/**
 * 作者：zhanghaitao on 2017/11/10 09:29
 * 邮箱：820159571@qq.com
 *
 * @describe:
 */

public class PtrClassicDefaultFooter extends PtrClassicDefaultHeader {
    public PtrClassicDefaultFooter(Context context) {
        this(context, (AttributeSet)null, 0);
    }

    public PtrClassicDefaultFooter(Context context, AttributeSet attrs) {
        this(context, (AttributeSet)null, 0);
    }

    public PtrClassicDefaultFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void initData() {
        this.mIsShowLastUpdate = false;
        this.mTexts = this.getResources().getStringArray(R.array.cube_ptr_pull_up);
        Bitmap bitmap = ((BitmapDrawable)((BitmapDrawable)this.getResources().getDrawable(R.drawable.ptr_rotate_arrow))).getBitmap();
        Matrix matrix = new Matrix();
        matrix.setRotate(180.0F);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        this.mRotateView.setImageBitmap(bitmap);
        this.mLastUpdateTextView.setVisibility(GONE);
    }
}