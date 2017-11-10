package com.zht.ptr;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;


public class MainActivity extends AppCompatActivity implements PullToRefresh.PullToRefreshListener {

    private PtrClassicFrameLayout ptrFrame;
    private ViewPager viewPager;
    private Indicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //下拉刷新，上拉加载
        ptrFrame = (PtrClassicFrameLayout) findViewById(R.id.main_ptr);




        List<MainViewPagerDataBean> list = new ArrayList<>();

        MainViewPagerDataBean bean = new MainViewPagerDataBean();
        bean.setTitle("贷款个数最近7天统计");
        bean.setTitleY("个数(个)");
        bean.setRefreshNumber("1301每个月");
        bean.setRefreshDate("最近更新：今天");
        bean.setDate(new String[]{"10-31","11-01","11-02","11-03","11-04","11-05","11-06"});
        bean.setValues(new int[]{12,15,86,94,53,27,65});

        MainViewPagerDataBean bean1 = new MainViewPagerDataBean();
        bean1.setTitle("贷款金额最近7天统计");
        bean1.setTitleY("金额(万元)");
        bean1.setRefreshNumber("1031每个月");
        bean1.setRefreshDate("最近更新：昨天");
        bean1.setDate(new String[]{"10-31","11-01","11-02","11-03","11-04","11-05","11-06"});
        bean1.setValues(new int[]{56,48,65,35,48,95,55});

        list.add(bean);
        list.add(bean1);

        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        indicator = (Indicator) findViewById(R.id.indicator);
        //设置ViewPager适配器
        viewPager.setAdapter(new MainPagerAdapter(this,list));
        //设置ViewPager的监听器
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                indicator.setoffset(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        initPtrClassicFrameLayout();

    }




    /**
     * 初始化上拉加载下拉刷新的布局
     * 注意：adapter的初始化在 PullToRefresh 之前
     */
    private void initPtrClassicFrameLayout() {
        //创建PtrClassicFrameLayout的包装类对象
        PullToRefresh refresh = new PullToRefresh();
        //初始化PtrClassicFrameLayout
        refresh.initPTR(this, ptrFrame,viewPager);
        //设置监听
        refresh.setPullToRefreshListener(this);
    }

    @Override
    public void pullToRefresh() {

    }

    @Override
    public void pullToLoadMore() {

    }
}
