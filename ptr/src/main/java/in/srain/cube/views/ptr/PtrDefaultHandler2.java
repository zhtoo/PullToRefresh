package in.srain.cube.views.ptr;

import android.os.Build;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

/**
 * 作者：zhanghaitao on 2017/11/9 18:08
 * 邮箱：820159571@qq.com
 *
 * @describe:
 */

public abstract class PtrDefaultHandler2 extends PtrDefaultHandler implements PtrHandler2 {
    public PtrDefaultHandler2() {
    }

    public static boolean canChildScrollDown(View view) {
        if(Build.VERSION.SDK_INT < 14) {
            if(!(view instanceof AbsListView)) {
                if(view instanceof ScrollView) {
                    ScrollView scrollView1 = (ScrollView)view;
                    return scrollView1.getChildCount() == 0?false:scrollView1.getScrollY() < scrollView1.getChildAt(0).getHeight() - scrollView1.getHeight();
                } else {
                    return false;
                }
            } else {
                AbsListView scrollView = (AbsListView)view;
                return scrollView.getChildCount() > 0 && (scrollView.getLastVisiblePosition() < scrollView.getChildCount() - 1 || scrollView.getChildAt(scrollView.getChildCount() - 1).getBottom() > scrollView.getPaddingBottom());
            }
        } else {
            return view.canScrollVertically(1);
        }
    }

    public static boolean checkContentCanBePulledUp(PtrFrameLayout frame, View content, View header) {
        return !canChildScrollDown(content);
    }

    public boolean checkCanDoLoad(PtrFrameLayout frame, View content, View footer) {
        return checkContentCanBePulledUp(frame, content, footer);
    }
}

