package com.zht.ptr;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
* @ClassName: DensityUtil 
* @Description:屏幕尺寸转换工具类
* @author zht
* @date 2017-09-27 下午4:17:01
 */
public class DensityUtil {

	/**
	 * dp转px
	 */
	public static int dp2px(Context context, int dpVal) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
	}

	/**
	 * px转dp
	 */
	public static int px2dp(Context context, int pxVal) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(pxVal / scale + 0.5f);
	}

	/**
	 * sp转px
	 */
	public static int sp2px(Context context, int spVal) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, context.getResources().getDisplayMetrics());
	}

	/**
	 * px转sp
	 */
	public static float px2sp(Context context, int pxVal) {
		return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
	}

	/** 获取手机的密度*/
	public static float getDensity(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.density;
	}

	/**
	 * 获取屏幕宽度像素
	 *
	 * @return
	 */
	public static int getWidthPixels(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Configuration cf = context.getResources().getConfiguration();
		int ori = cf.orientation;
		if (ori == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
			return displayMetrics.heightPixels;
		} else if (ori == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
			return displayMetrics.widthPixels;
		}
		return 0;
	}

	/**
	 * 获取屏幕宽度像素
	 *
	 * @return
	 */
	public int getHeightPixels(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Configuration cf = context.getResources().getConfiguration();
		int ori = cf.orientation;
		if (ori == Configuration.ORIENTATION_LANDSCAPE) {// 横屏获取屏幕的宽度
			return displayMetrics.widthPixels;
		} else if (ori == Configuration.ORIENTATION_PORTRAIT) {// 竖屏获取屏幕的高度
			return displayMetrics.heightPixels;
		}
		return 0;
	}
}
