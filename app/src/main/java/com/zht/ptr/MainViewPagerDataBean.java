package com.zht.ptr;

/**
 * 作者：zhanghaitao on 2017/11/9 14:05
 * 邮箱：820159571@qq.com
 *
 * @describe:viewpager的条目数据
 */

public class MainViewPagerDataBean {

    private String title;

    private String refreshDate;

    private String refreshNumber;

    private String titleY;

    private String[] date;

    private int[] values;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRefreshDate() {
        return refreshDate;
    }

    public void setRefreshDate(String refreshDate) {
        this.refreshDate = refreshDate;
    }

    public String getRefreshNumber() {
        return refreshNumber;
    }

    public void setRefreshNumber(String refreshNumber) {
        this.refreshNumber = refreshNumber;
    }

    public String getTitleY() {
        return titleY;
    }

    public void setTitleY(String titleY) {
        this.titleY = titleY;
    }

    public String[] getDate() {
        return date;
    }

    public void setDate(String[] date) {
        this.date = date;
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }
}
