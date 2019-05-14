package com.example.win10.pagenumberview.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.win10.pagenumberview.R;


public class PageNumberView extends FrameLayout implements View.OnClickListener,View.OnTouchListener ,View.OnLongClickListener {
    private Context  mContext;
    private TextView tvRight;
    private TextView tvCenter;
    private TextView tvLeft;
    private View view;
    private int number=1;//当前页码
    private int maxNumber=100;//最多页码
    OnTouchListener listener;
    private int mX;//按下的坐标点X
    private int mY;//按下的坐标点Y
    private boolean isMoved=false;//是否移动
    private boolean isLongClick=false;//是否执行过长按事件
    Runnable mLongPressRunnable; //需要发送长按事件的代码
    private int TOUCH_MOVE =50;//移动阈值
    public PageNumberView(@NonNull Context context) {
        this(context,null,0);
    }

    public PageNumberView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    @SuppressLint("Range")
    public PageNumberView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        LayoutInflater.from(context).inflate(R.layout.page_number_view, this, true);
        tvCenter=findViewById(R.id.tvCenter);
        tvLeft=findViewById(R.id.tvLeft);
        tvRight=findViewById(R.id.tvRight);

        if (attrs!=null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PageNumberView);
            int color = typedArray.getColor(R.styleable.PageNumberView_textColorTitle, Color.argb(0xff,0xff,0xff,0xff));
            tvCenter.setTextColor(color);
            typedArray.recycle();
        }
        mLongPressRunnable = new Runnable() {
            @Override
            public void run() {
                performLongClick();
                if (view!=null){
                    isLongClick=true;
                    onLongClick(view);
                }
            }
        };
        tvRight.setOnTouchListener(this);
        tvLeft.setOnTouchListener(this);
    }
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.tvRight:
                startAnim(number,maxNumber);
                break;
            case R.id.tvLeft:
                startAnim(number,0);
                break;
        }
        return true;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    public void nextNumber() {
        if (number<maxNumber){
            this.number++;
        }

        else Toast.makeText(mContext, "已到最后一页", Toast.LENGTH_SHORT).show();

    }
    public void lastNumber() {
        if (number>1)
            this.number--;
        else Toast.makeText(mContext, "已到第一页", Toast.LENGTH_SHORT).show();
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvRight:
                if (listener!=null)listener.OnRightClick(v);
                nextNumber();
                break;
            case R.id.tvLeft:
                if (listener!=null)listener.OnLeftClick(v);
                lastNumber();
                break;
        }
        tvCenter.setText(number+"/"+maxNumber);
    }

    ValueAnimator anim;
    boolean isDown=false;
    private void startAnim(int start, int end ) {

        anim = ObjectAnimator.ofInt(start, end);
        anim.setDuration(3000);
//        数值动画回调监听
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (isDown){
                    number =(int) valueAnimator.getAnimatedValue();
                    tvCenter.setText(number+"/"+maxNumber);
                }else {
                    listener.OnLongStopping(number);
                    if (anim!=null){
                        anim.cancel();
                    }
                }
            }
        });
        anim.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                记录按下坐标
                mX = x;
                mY = y;
//                初始化操作符
                isMoved = false;
                isLongClick=false;
                isDown=true;
//                记录按下的view
                view=v;
                postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
                break;
            case MotionEvent.ACTION_MOVE:
                if(isMoved) break;
                if(Math.abs(mX -x) > TOUCH_MOVE || Math.abs(mY -y) > TOUCH_MOVE) {
                    //移动超过阈值，则表示移动了
                    isMoved = true;
                    removeCallbacks(mLongPressRunnable);
                    view=null;
                }
                break;
            case MotionEvent.ACTION_UP:
                //释放了
                removeCallbacks(mLongPressRunnable);
                if (isDown&&!isLongClick&&!isMoved)onClick(v);
                if (anim!=null)anim.cancel();
                isDown=false;
                view=null;
                isMoved=false;
                break;
        }
        return true;
    }

    /**
     * 触摸事件包括左右点击和长按事件
     */
    interface OnTouchListener {
        void OnRightClick(View v);
        void OnLeftClick(View v);
        void OnLongStopping(int number);
    }
    public void setListener(OnTouchListener listener) {
        this.listener = listener;
    }
}
