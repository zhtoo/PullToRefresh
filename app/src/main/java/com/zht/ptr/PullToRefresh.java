package com.zht.ptr;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler2;

/**
 * Created by zhanghaitao on 2017/7/14.
 *
 * @description：下拉刷新，上拉加载的包装类
 */

public class PullToRefresh {

    public <T> void initPTR(Context context, final PtrClassicFrameLayout ptrFrame) {
        this.initPTR(context, ptrFrame, null,null);
    }

    public <T> void initPTR(Context context, final PtrClassicFrameLayout ptrFrame,ViewPager viewPager) {
        this.initPTR(context, ptrFrame, null,viewPager);
    }



    public <T> void initPTR(Context context, final PtrClassicFrameLayout ptrFrame, PtrFrameLayout.Mode mode,ViewPager viewPager) {
        ptrFrame.setEnabled(true);
        ptrFrame.disableWhenHorizontalMove(true);
        if (viewPager != null) {
            ptrFrame.setView(viewPager);
        }


        ptrFrame.setPtrHandler(new PtrHandler2() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                //Toast.makeText(Activity.this, "下拉刷新", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.pullToRefresh();
                    ptrFrame.refreshComplete();
                }
            }

            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler2.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onLoadBegin(PtrFrameLayout frame) {
                //Toast.makeText(Activity.this, "上拉加载", Toassst.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.pullToLoadMore();
                    ptrFrame.refreshComplete();
                }
            }

            public boolean checkCanDoLoad(PtrFrameLayout frame, View content, View footer) {
                return PtrDefaultHandler2.checkContentCanBePulledUp(frame, content, footer);
            }
        });
        ptrFrame.disableWhenHorizontalMove(true);
        // 初始化view
        ptrFrame.setLastUpdateTimeRelateObject(ptrFrame);
        if (mode == null) {
            ptrFrame.setMode(PtrFrameLayout.Mode.BOTH);
        } else {
            ptrFrame.setMode(mode);
        }
    }

    private PullToRefreshListener listener;

    public void setPullToRefreshListener(PullToRefreshListener listener) {
        this.listener = listener;
    }

    public interface PullToRefreshListener {
        void pullToRefresh();

        void pullToLoadMore();
    }

}
