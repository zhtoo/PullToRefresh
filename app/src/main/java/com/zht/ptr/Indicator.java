package com.zht.ptr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 作者：zhanghaitao on 2017/11/8 13:18
 * 邮箱：820159571@qq.com
 *
 * @describe:
 */

public class Indicator extends View {

    private static final String TAG = "Indicator";

    //第一个小方块的画笔
    private Paint firatPaint;
    //第二个小方块的画笔
    private Paint secondPaint;
    //view的宽度
    private int width;
    //view的高度
    private int height;
    //view的中心点
    private int centerWidth;
    //移动的偏移量
    private float offset = 0;
    private int firstPaintColor = Color.argb(255, 23, 140, 219);
    //  private int orage = Color.argb(255, 239, 128, 0);
    private int secondPaintColor = Color.argb(255, 204, 204, 204);
    private float dynamicLengthFirat;
    private float dynamicLengthSecond;

    //该方法在我们java代码添加控件时回调
    public Indicator(Context context) {
        super(context);
        initPaint();
    }

    //该方法在我们XML文件里添加控件时回调
    public Indicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        centerWidth = width / 2;
        dynamicLengthFirat = offset * centerWidth / 2;
        dynamicLengthSecond = 3 * width / 4 + dynamicLengthFirat;
    }

    //参数就是画板，可以直接使用
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //方形
        RectF firatRect = new RectF(dynamicLengthFirat,//变化值
                0, centerWidth - 10, height);
        RectF secondRect = new RectF(centerWidth + 10, 0,
                dynamicLengthSecond,//变化值
                height);
        //绘画圆角方形
        canvas.drawRoundRect(firatRect, 10, 10, firatPaint);
        canvas.drawRoundRect(secondRect, 10, 10, secondPaint);
    }

    //初始化画笔对象
    private void initPaint() {
        //创建画笔的对象,用于画第一个小方块
        firatPaint = new Paint();
        firatPaint.setAntiAlias(true);//设置抗锯齿
        firatPaint.setStyle(Paint.Style.FILL);//设置画笔的样式,为实心
        firatPaint.setColor(firstPaintColor);//设置画笔的颜色
        firatPaint.setStrokeWidth(2);//设置画笔的宽度
        //创建画笔的对象,用于画空心圆
        secondPaint = new Paint();
        secondPaint.setAntiAlias(true);
        secondPaint.setStyle(Paint.Style.FILL);
        secondPaint.setColor(secondPaintColor);
        secondPaint.setStrokeWidth(2);
    }


    public void setoffset(int position, float positionOffset) {
        //只要当前的positionOffset发生变化就记录当前的偏移量
        if (positionOffset != 0) {
            if (positionOffset - offset > 0) {//向左
                //动态改变画笔的颜色
                firstPaintColor = Color.argb(255,
                        (int) (23 + 181 * positionOffset),
                        (int) (140 + 64 * positionOffset),
                        (int) (219 - 15 * positionOffset)
                );
                secondPaintColor = Color.argb(255,
                        (int) (204 + 35 * positionOffset),
                        (int) (204 - 76 * positionOffset),
                        (int) (204 - 204 * positionOffset)
                );
            } else if (positionOffset - offset < 0) {//向右
                //动态改变画笔的颜色
                firstPaintColor = Color.argb(255,
                        (int) (204 - 181 * (1 - positionOffset)),
                        (int) (204 - 64 * (1 - positionOffset)),
                        (int) (204 + 15 * (1 - positionOffset))
                );
                secondPaintColor = Color.argb(255,
                        (int) (239 - 35 * (1 - positionOffset)),
                        (int) (128 + 76 * (1 - positionOffset)),
                        (int) (204 * (1 - positionOffset))
                );
            }
            firatPaint.setColor(firstPaintColor);
            secondPaint.setColor(secondPaintColor);
            offset = positionOffset;
        }
        //重新计算空间宽度
        dynamicLengthFirat = offset * centerWidth / 2;
        dynamicLengthSecond = 3 * width / 4 + dynamicLengthFirat;
        //关键：重新绘制自定义view的方法，十分常用
        invalidate();
    }
}