package com.example.myapplication2.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import java.util.HashMap;
import java.util.List;


public class MapOverlay extends View implements MapView.OnMapEventListener {

    private MapView mMapView;

    private List<LocationAPI.CalibrationPoint> mCalibrationPoints;
    private Paint mPaint;
    private Bitmap mCalibrationDot;

    public MapOverlay(Context context) {
        super(context);
        init(null, 0);
    }

    public MapOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MapOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mCalibrationDot = BitmapFactory.decodeResource(getResources(), R.drawable.ic_directions_form_destination);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mMapView != null && mCalibrationPoints != null) {
            for(LocationAPI.CalibrationPoint calibrationPoint : mCalibrationPoints) {
                PointF point = calibrationPoint.getPoint();
                PointF pointMapped = mMapView.mapPointFromMap(point, true);
                canvas.drawBitmap(mCalibrationDot,
                        pointMapped.x - (mCalibrationDot.getWidth()/2.0f),
                        pointMapped.y - (mCalibrationDot.getHeight()/2.0f), mPaint);
            }
        }
    }

    public void setMapView(MapView mapView) {
        mMapView = mapView;
        mMapView.addListener(this);
    }

    @Override
    public void onMapClick(double touchX, double touchY) { }

    @Override
    public void onMapLongClick(PointF touchPoint) { }

    @Override
    public void onMapMatrixUpdate(Matrix matrix) {

        // update the view as  well
        postInvalidate();
    }

    public void setCalibrationPoints(List<LocationAPI.CalibrationPoint> calibrationPoints) {
        this.mCalibrationPoints = calibrationPoints;
    }
}
