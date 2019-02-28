package com.yinghe.whiteboardlib.bean;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;

public class StrokeRecord {
    public static final int STROKE_TYPE_ERASER = 1;
    public static final int STROKE_TYPE_DRAW = 2;
    public static final int STROKE_TYPE_LINE = 3;
    public static final int STROKE_TYPE_CIRCLE = 4;
    public static final int STROKE_TYPE_RECTANGLE = 5;
    public static final int STROKE_TYPE_TEXT = 6;

    public int type;//记录类型
    public Paint paint;//笔类
    public Path path;//画笔路径数据
    public PointF[] linePoints; //线数据
    public RectF rect; //圆、矩形区域
    public String text;//文字
    public TextPaint textPaint;//笔类
    public List<PointEntity> points=new ArrayList<>();//线条数据
    public List<PointEntity> erasers=new ArrayList<>();//橡皮擦数据
    public int textOffX;
    public int textOffY;
    public int textWidth;//文字位置
    public int downX;
    public int downY;
    public int preX;
    public int preY;
    public int strokeColor;
    @Override
    public String toString() {

        return "StrokeRecord{" +
                "type=" + type +
                ", text='" + text + '\'' +
                ", textOffX=" + textOffX +
                ", textOffY=" + textOffY +
                ", textWidth=" + textWidth +
                ", downX=" + downX +
                ", downY=" + downY +
                ", preX=" + preX +
                ", preY=" + preY +
                '}';
    }



    public StrokeRecord(int type) {
        this.type = type;
    }
}