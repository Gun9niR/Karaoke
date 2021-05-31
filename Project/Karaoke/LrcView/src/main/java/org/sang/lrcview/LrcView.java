package org.sang.lrcview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.exoplayer2.SimpleExoPlayer;

import org.sang.lrcview.bean.LrcBean;
import org.sang.lrcview.util.LrcUtil;

import java.util.List;

/**
 * Created by 王松 on 2016/10/21.
 */

public class LrcView extends View {

    private List<LrcBean> list;
    private Paint gPaint;
    private Paint hPaint;
    private int width = 0, height = 0;
    private int currentPosition = 0;
    private SimpleExoPlayer player;
    private int lastPosition = 0;
    private int highLineColor;
    private int lrcColor;
    private int mode = 0;
    private boolean playerReleased = false;
    public final static int KARAOKE = 1;

    public void setHighLineColor(int highLineColor) {
        this.highLineColor = highLineColor;
        hPaint.setColor(highLineColor);
    }

    public void setLrcColor(int lrcColor) {
        this.lrcColor = lrcColor;
        gPaint.setColor(lrcColor);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPlayer(SimpleExoPlayer player) {
        this.player = player;
    }


    /**
     *
     * @param lrc .lrc文件中的字符串
     * @return 每句开始时间(ms)的列表
     */
    public List<LrcBean> setLrc(String lrc) {
        list = LrcUtil.parseStr2List(lrc);
        playerReleased = false;
        invalidate();

        return list;
    }

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
        highLineColor = ta.getColor(R.styleable.LrcView_hignLineColor, getResources().getColor(R.color.green));
        lrcColor = ta.getColor(R.styleable.LrcView_lrcColor, getResources().getColor(android.R.color.darker_gray));
        mode = ta.getInt(R.styleable.LrcView_lrcMode,mode);
        ta.recycle();
        gPaint = new Paint();
        gPaint.setAntiAlias(true);
        gPaint.setColor(lrcColor);
        gPaint.setTextSize(36);
        gPaint.setTextAlign(Paint.Align.CENTER);
        hPaint = new Paint();
        hPaint.setAntiAlias(true);
        hPaint.setColor(highLineColor);
        hPaint.setTextSize(36);
        hPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width == 0 || height == 0) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
        }
        if (list == null || list.size() == 0) {
            canvas.drawText("暂无歌词", width / 2, height / 2, gPaint);
            return;
        }

        if (!playerReleased) {
            getCurrentPosition();

            int currentMillis = (int) player.getCurrentPosition();
            drawLrc2(canvas, currentMillis);
            long start = list.get(currentPosition).getStart();
            float v = (currentMillis - start) > 500 ? currentPosition * 80 : lastPosition * 80 + (currentPosition - lastPosition) * 80 * ((currentMillis - start) / 500f);
            setScrollY((int) v);
            if (getScrollY() == currentPosition * 80) {
                lastPosition = currentPosition;
            }
            postInvalidateDelayed(500);
        }
    }

    private void drawLrc2(Canvas canvas, int currentMillis) {
        if (mode == 0) {
            for (int i = 0; i < list.size(); i++) {
                if (i == currentPosition) {
                    canvas.drawText(list.get(i).getLrc(), width / 2, height / 2 + 80 * i, hPaint);
                } else {
                    canvas.drawText(list.get(i).getLrc(), width / 2, height / 2 + 80 * i, gPaint);
                }
            }
        }else{
            for (int i = 0; i < list.size(); i++) {
                canvas.drawText(list.get(i).getLrc(), width / 2, height / 2 + 80 * i, gPaint);
            }
            String highLineLrc = list.get(currentPosition).getLrc();
            int highLineWidth = (int) gPaint.measureText(highLineLrc);
            int leftOffset = (width - highLineWidth) / 2;
            LrcBean lrcBean = list.get(currentPosition);
            long start = lrcBean.getStart();
            long end = lrcBean.getEnd();
            int i = (int) ((currentMillis - start) * 1.0f / (end - start) * highLineWidth);
            if (i > 0) {
                Bitmap textBitmap = Bitmap.createBitmap(i, 90, Bitmap.Config.ARGB_8888);
                Canvas textCanvas = new Canvas(textBitmap);
                textCanvas.drawText(highLineLrc, highLineWidth / 2, 80, hPaint);
                canvas.drawBitmap(textBitmap, leftOffset, height / 2 + 80 * (currentPosition - 1), null);
            }
        }
    }

    public void init() {
        currentPosition = 0;
        lastPosition = 0;
        playerReleased = false;
        setScrollY(0);
        invalidate();
    }

    /**
     * 获取当前播放到的LrcBean下标
     */
    private void getCurrentPosition() {
        try {
            int currentMillis = (int) player.getCurrentPosition();
            if (currentMillis < list.get(0).getStart()) {
                currentPosition = 0;
                return;
            }
            if (currentMillis > list.get(list.size() - 1).getStart()) {
                currentPosition = list.size() - 1;
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                if (currentMillis >= list.get(i).getStart() && currentMillis < list.get(i).getEnd()) {
                    currentPosition = i;
                    return;
                }
            }
        } catch (Exception e) {
            postInvalidateDelayed(500);
        }
    }

    public void alertPlayerReleased() {
        playerReleased = true;
    }
}
