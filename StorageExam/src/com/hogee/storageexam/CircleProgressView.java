package com.hogee.storageexam;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * 
 * @author jianhua.he
 *
 */
public class CircleProgressView extends View{
    
    Paint mPaint,mTextpaint;
    RectF mArea;
    int mValue = 100;
    LinearGradient mShader;

    public CircleProgressView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CircleProgressView(Context context) {
        super(context);
        init();
    }
    
    public void setProgress(int value){
        if (value >= 0 && value <= 100) {
            this.mValue = value;
            invalidate();
        }
    }
    
    private void init() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(50f);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Style.STROKE);
        mPaint.setAntiAlias(true);
        
        mTextpaint = new Paint();
        mTextpaint.setTextSize(50f);
        mTextpaint.setColor(Color.WHITE);
        
        mArea = new RectF(100, 100, 500, 500);
        
        mShader = new LinearGradient(0, 0, 400, 0, new int[] {    
                 Color.BLUE, Color.WHITE}, null, Shader.TileMode.CLAMP); 
        
        mPaint.setShader(mShader);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GRAY);
        canvas.drawArc(mArea, 120, 360*mValue/100 , false, mPaint);
        canvas.drawText(mValue+"%", 270, 290, mTextpaint);
    }

}
