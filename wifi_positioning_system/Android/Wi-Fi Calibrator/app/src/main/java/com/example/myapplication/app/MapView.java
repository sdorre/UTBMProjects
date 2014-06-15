package com.example.myapplication.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class MapView extends View {

    ArrayList<OnMapEventListener> mListeners;

    private SparseArray<PointF> mActivePointers;

    private final float SCROLL_THRESHOLD = 20;
    private final int LONG_PRESS_DELAY = 1000;

    private PointF lastFirstPoint, lastSecondPoint;
    private int lastFirstPointerID = -1;
    private int lastSecondPointerID = -1;
    private int lastPointersCount;

    private double mLastDeltaX, mLastDeltaY, mLastDeltaRotation, mLastDeltaDistance;

    private boolean mClickStarted = false;
    private int mClickID = -1;
    private float mClickX, mClickY;
    private double mClickStatedTime;

    private Handler mLongPressHandler;
    private Runnable mLongPressedRunnable;

    private Matrix mMatrix;

    private Paint mPaint;
    private Bitmap mMapImage;

    public MapView(Context context) {
        super(context);
        init(null, 0);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // initialize the list of listeners
        mListeners = new ArrayList<OnMapEventListener>();

        // initialize the list of tracked pointers
        mActivePointers = new SparseArray<PointF>();

        // initialize the canvas painter
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // initialize the canvas matrix
        mMatrix = new Matrix();

        // init the long press event runnable
        mLongPressHandler = new Handler();
        mLongPressedRunnable = new Runnable() {
            public void run() {
                onLongPress();
            }
        };
    }

    /*
     * Public methods
     */

    public void setMapImage(Bitmap mapImage) {
        mMapImage = mapImage;
    }

    public void moveToCenter() {

        float centerX = -(mMapImage.getWidth() / 2.0f) + (getWidth() / 2.0f);
        float centerY = -(mMapImage.getHeight() / 2.0f) + (getHeight() / 2.0f);

        mMatrix.reset();
        mMatrix.postTranslate(centerX, centerY);

        postInvalidate();
    }


    /*
     * Pointers tracking
     */

    private boolean isNewFirstPointer(boolean resetOnNew) {
        if(lastFirstPointerID != mActivePointers.keyAt(0)) {
            if(resetOnNew)
                resetLastFirstPointer();
            return true;
        }
        return false;
    }

    private boolean isNewSecondPointer(boolean resetOnNew) {
        if(lastSecondPointerID != mActivePointers.keyAt(1)) {
            if(resetOnNew)
                resetLastSecondPointer();
            return true;
        }
        return false;
    }

    private void resetLastFirstPointer() {
        lastFirstPointerID = mActivePointers.keyAt(0);
        PointF newPoint = mActivePointers.get(lastFirstPointerID);
        lastFirstPoint = new PointF(newPoint.x, newPoint.y);
    }

    private void resetLastSecondPointer() {
            lastSecondPointerID = mActivePointers.keyAt(1);
            PointF newPoint = mActivePointers.get(lastSecondPointerID);
            lastSecondPoint = new PointF(newPoint.x, newPoint.y);

    }


    /*
     * Canvas matrix and drawing
     */

    private void updateMatrix() {
        int currentPointersCount = mActivePointers.size();

        if(currentPointersCount != 0) {

            double deltaX, deltaY, deltaRotation, deltaDistance;

            if (currentPointersCount >= 2) {

                PointF lastMiddlePoint, currentMiddlePoint;
                float lastDistance, currentDistance;

                if (currentPointersCount != lastPointersCount) {
                    resetLastFirstPointer();
                    resetLastSecondPointer();
                    mLastDeltaX = 0.0f;
                    mLastDeltaY = 0.0f;
                    mLastDeltaRotation = 0.0f;
                    mLastDeltaDistance = 0.0f;
                }

                if (isNewFirstPointer(true) || isNewSecondPointer(true)) {
                    mLastDeltaX = 0.0f;
                    mLastDeltaY = 0.0f;
                    mLastDeltaRotation = 0.0f;
                    mLastDeltaDistance = 0.0f;
                } else {

                    lastMiddlePoint = middlePointOf(lastFirstPoint, lastSecondPoint);
                    currentMiddlePoint = middlePointOf(mActivePointers.get(lastFirstPointerID), mActivePointers.get(lastSecondPointerID));

                    lastDistance = spacingBetween(lastFirstPoint, lastSecondPoint);
                    currentDistance = spacingBetween(mActivePointers.get(lastFirstPointerID), mActivePointers.get(lastSecondPointerID));

                    deltaX = currentMiddlePoint.x - lastMiddlePoint.x;
                    deltaY = currentMiddlePoint.y - lastMiddlePoint.y;
                    deltaDistance = currentDistance / lastDistance;
                    deltaRotation = angleBetween(mActivePointers.get(lastFirstPointerID), mActivePointers.get(lastSecondPointerID), lastFirstPoint, lastSecondPoint);

                    mMatrix.postTranslate((float) (deltaX - mLastDeltaX), (float) (deltaY - mLastDeltaY));
                    mMatrix.postRotate((float) (deltaRotation - mLastDeltaRotation), currentMiddlePoint.x, currentMiddlePoint.y);
                    if (mLastDeltaDistance != 0.0f)
                        mMatrix.postScale((float) (deltaDistance / mLastDeltaDistance), (float) (deltaDistance / mLastDeltaDistance), currentMiddlePoint.x, currentMiddlePoint.y);

                    mLastDeltaX = deltaX;
                    mLastDeltaY = deltaY;
                    mLastDeltaRotation = deltaRotation;
                    mLastDeltaDistance = deltaDistance;
                }
            } else if (currentPointersCount == 1) {

                if (isNewFirstPointer(false) || currentPointersCount != lastPointersCount) {
                    resetLastFirstPointer();
                    mLastDeltaX = 0.0f;
                    mLastDeltaY = 0.0f;
                }

                PointF currentPoint = mActivePointers.get(lastFirstPointerID);
                deltaX = currentPoint.x - lastFirstPoint.x;
                deltaY = currentPoint.y - lastFirstPoint.y;

                mMatrix.postTranslate((float) (deltaX - mLastDeltaX), (float) (deltaY - mLastDeltaY));

                mLastDeltaX = deltaX;
                mLastDeltaY = deltaY;

            }

        }

        lastPointersCount = currentPointersCount;

        if(mListeners != null && mListeners.size() > 0)
            for(OnMapEventListener listener : mListeners)
                listener.onMapMatrixUpdate(mMatrix);
    }

    private Matrix getFixedMatrix() {
        Matrix fixedMatrix = new Matrix(mMatrix);

        // fix the matrix as the view may not be at the screen origin
        int viewPosition[] = new int[2];
        getLocationOnScreen(viewPosition);
        fixedMatrix.postTranslate(viewPosition[0], viewPosition[1]);

        return fixedMatrix;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updateMatrix();
        canvas.setMatrix(getFixedMatrix());

        if(mMapImage != null)
            canvas.drawBitmap(mMapImage, 0.0f, 0.0f, mPaint);
    }


    /*
     * Touch interaction
     */

    public void onLongPress() {
        if(mListeners != null && mListeners.size() > 0) {

            // get the normalized click coordinates in the map reference
            PointF mappedPoint = mapPointToMap(new PointF(mClickX, mClickY), true);

            // if the click coordinates is inside the map
            if((mappedPoint.x >= 0.0f && mappedPoint.x <= 1.0f) && (mappedPoint.y >= 0.0f && mappedPoint.y <= 1.0f)) {

                // haptic feedback, vibrate the device
                Context context = getContext();
                if(context != null) {
                    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    if(v != null)
                        v.vibrate(50);
                }

                // trigger the event
                for(OnMapEventListener listener : mListeners)
                    listener.onMapLongClick(mappedPoint);
            }
        }

        // reset the click event
        mClickStarted = false;
        mClickID = -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);

                if(!mClickStarted) {

                    // initialize a click event
                    mClickStarted = true;
                    mClickStatedTime = System.currentTimeMillis();

                    // set click pointer information
                    mClickID = pointerIndex;
                    mClickX = f.x;
                    mClickY = f.y;

                    // setup a delayed event for a long press
                    mLongPressHandler.postDelayed(mLongPressedRunnable, LONG_PRESS_DELAY);
                }

                // start tracking the new pointer element
                mActivePointers.put(pointerId, f);
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                if (mClickStarted) {

                    // filter small pointer's position variations,
                    // check if the user wants to move the map
                    if(Math.abs(mClickX - event.getX(mClickID)) > SCROLL_THRESHOLD
                    || Math.abs(mClickY - event.getY(mClickID)) > SCROLL_THRESHOLD) {

                        // reset the click event
                        mClickStarted = false;
                        mClickID = -1;

                        // remove the long press future event
                        mLongPressHandler.removeCallbacks(mLongPressedRunnable);
                    }
                }

                if(!mClickStarted) {

                    // update the tracked pointer's information
                    for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                        PointF point = mActivePointers.get(event.getPointerId(i));
                        if (point != null) {
                            point.x = event.getX(i);
                            point.y = event.getY(i);
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {

                // stop tracking the pointer
                mActivePointers.remove(pointerId);

                if (mClickStarted) {
                    if(mListeners != null && mListeners.size() > 0) {

                        // get the normalized click coordinates in the map reference
                        PointF mappedPoint = mapPointToMap(new PointF(mClickX, mClickY), true);

                        // if the click coordinates is inside the map
                        if((mappedPoint.x >= 0.0f && mappedPoint.x <= 1.0f) && (mappedPoint.y >= 0.0f && mappedPoint.y <= 1.0f)) {

                            // remove the long press future event
                            mLongPressHandler.removeCallbacks(mLongPressedRunnable);

                            // trigger the event
                            for(OnMapEventListener listener : mListeners)
                                listener.onMapClick(mappedPoint.x, mappedPoint.y);
                        }
                    }

                    // Reset the click event
                    mClickStarted = false;
                    mClickID = -1;
                }

                if(pointerId == lastFirstPointerID)
                    lastFirstPointerID = -1;
                else if(pointerId == lastSecondPointerID)
                    lastSecondPointerID = -1;
                break;
            }
        }

        invalidate();

        return true;
    }


    /*
     * Utils
     */

    private float spacingBetween(PointF point, PointF otherPoint) {
        float x = point.x - otherPoint.x;
        float y = point.y - otherPoint.y;
        return FloatMath.sqrt(x * x + y * y);
    }

    private PointF middlePointOf(PointF point, PointF otherPoint) {
        float x = point.x + otherPoint.x;
        float y = point.y + otherPoint.y;
        PointF retPoint = new PointF();
        retPoint.set(x/2, y/2);
        return retPoint;
    }

    private double angleBetween(PointF lineFirstPoint, PointF lineSecondPoint, PointF otherLineFirstPoint, PointF otherLineSecondPoint) {
        double angle1 = Math.atan2(lineFirstPoint.y - lineSecondPoint.y,
                lineFirstPoint.x - lineSecondPoint.x);
        double angle2 = Math.atan2(otherLineFirstPoint.y - otherLineSecondPoint.y,
                otherLineFirstPoint.x - otherLineSecondPoint.x);
        return Math.toDegrees(angle1-angle2);
    }

    public PointF mapPointToMap(PointF point, boolean normalize) {

        if(mMapImage == null || mMatrix == null || point == null)
            return null;

        Matrix inverse = new Matrix();
        mMatrix.invert(inverse);

        float[] p = new float[]{point.x, point.y};
        inverse.mapPoints(p);

        if(normalize) {
            p[0] /= (1.0f * mMapImage.getWidth());
            p[1] /= (1.0f * mMapImage.getHeight());
        }

        return new PointF(p[0], p[1]);
    }

    public PointF mapPointFromMap(PointF point, boolean normalized) {

        if(mMapImage == null || mMatrix == null || point == null)
            return null;

        float[] p = new float[]{point.x, point.y};

        if(normalized) {
            p[0] *= mMapImage.getWidth();
            p[1] *= mMapImage.getHeight();
        }

        mMatrix.mapPoints(p);

        return new PointF(p[0], p[1]);
    }


    /*
     * Events interface
     */

    public interface OnMapEventListener {
        public void onMapClick(double touchX, double touchY);
        public void onMapLongClick(PointF touchPoint);
        public void onMapMatrixUpdate(Matrix matrix);
    }

    public void addListener(Object listener) {
        try {
            if(!mListeners.contains(listener))
                mListeners.add((OnMapEventListener) listener);
        } catch (ClassCastException e) {
            throw new ClassCastException(listener.toString()
                    + " must implement OnMapEventListener");
        }
    }
}
