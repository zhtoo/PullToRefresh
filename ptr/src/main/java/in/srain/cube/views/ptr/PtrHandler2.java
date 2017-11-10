package in.srain.cube.views.ptr;

import android.view.View;

/**
 * 作者：zhanghaitao on 2017/11/9 18:07
 * 邮箱：820159571@qq.com
 *
 * @describe:
 */

public interface PtrHandler2 extends PtrHandler {
    boolean checkCanDoLoad(PtrFrameLayout var1, View var2, View var3);

    void onLoadBegin(PtrFrameLayout var1);

}