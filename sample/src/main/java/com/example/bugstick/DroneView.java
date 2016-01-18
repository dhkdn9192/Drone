package com.example.bugstick;

import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;

/**
 * A simple toy view for demo purposes.
 * Displays a circle and a trail that can be controlled via {@link #setVelocity(float, float)}.
 */
public class DroneView extends View implements TimeAnimator.TimeListener {

    private static final float DRONE_TRAIL_DP = 200f;
    private static final float MAX_DRONE_SPEED_DP_PER_S = 300f;

    private static final float MAX_RAD = 30f;
    private static final float MIN_RAD = 5f;

    private float[] rad = {MIN_RAD, MIN_RAD, MIN_RAD, MIN_RAD};

    private Paint paint;
    private Paint[] paints = new Paint[4];
    private TimeAnimator animator;

    private float density;
    private float differ;
    private int width, height;
    private PointF position;
    private PointF[] positions = new PointF[4];
    private PointF velocity;
    private PointF rotation;
    private Path path;
    private Path[] paths = new Path[4];
    private PathMeasure pathMeasure;

    private int contSwitch = 1;

    public DroneView(Context context) {
        super(context);
        init(context);
    }

    public DroneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DroneView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DroneView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

        animator = new TimeAnimator();
        animator.setTimeListener(this);

        paint = new Paint();
        path = new Path();
        position = new PointF();

        for (int i=0;i<4;i++) {
            paints[i] = new Paint();
            paths[i] = new Path();
            positions[i] = new PointF();
        }

        paint.setColor(Color.WHITE);
        paint.setTextSize(100f);
        paint.setTextAlign(Paint.Align.CENTER);
        paints[0].setColor(Color.MAGENTA);
        paints[1].setColor(Color.GREEN);
        paints[2].setColor(Color.YELLOW);
        paints[3].setColor(Color.BLUE);

        density = getResources().getDisplayMetrics().density;
        differ = (MAX_RAD - MIN_RAD) / (MAX_DRONE_SPEED_DP_PER_S * density);
        pathMeasure = new PathMeasure();

        velocity = new PointF();
        rotation = new PointF();
    }

    /**
     * Start applying velocity as soon as view is on-screen.
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator.start();
    }

    /**
     * Stop animations when the view hierarchy is torn down.
     */
    @Override
    public void onDetachedFromWindow() {
        animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        position.set(width / 2, height / 2);
        positions[0].set(width / 3, height / 3);
        positions[1].set(width * 2 / 3, height / 3);
        positions[2].set(width / 3, height * 2 / 3);
        positions[3].set(width * 2 / 3, height * 2 / 3);

        path.rewind();
        path.moveTo(position.x, position.y);
        for (int i=0;i<4;i++){
            paths[i].rewind();
            paths[i].moveTo(positions[i].x, positions[i].y);
        }

    }

    public void setVelocity(float vxDps, float vyDps) {
        velocity.set(vxDps * density, vyDps * density);
        Log.d("velocity", " " + velocity.x + ", " + velocity.y);
        contSwitch = 1;
    }

    public void setRotation(float vxDps, float vyDps) {
        rotation.set(vxDps * density, vyDps * density);
        Log.d("rotation", " " + rotation.x + ", " + rotation.y);
        contSwitch = 2;
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        final float dt = deltaTime / 1000f; // seconds


        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        if (contSwitch == 1) {
            rad[0] = (velocity.x + velocity.y) * differ + (MIN_RAD + MAX_RAD) / 2;
            rad[1] = (- velocity.x + velocity.y) * differ + (MIN_RAD + MAX_RAD) / 2;
            rad[2] = (velocity.x - velocity.y) * differ + (MIN_RAD + MAX_RAD) / 2;
            rad[3] = (- velocity.x - velocity.y) * differ + (MIN_RAD + MAX_RAD) / 2;
        } else {
            rad[0] = (rotation.x - rotation.y) * differ + (MIN_RAD + MAX_RAD) / 2;
            rad[1] = (- rotation.x - rotation.y) * differ + (MIN_RAD + MAX_RAD) / 2;
            rad[2] = rad[1];
            rad[3] = rad[0];

        }

        limitRad();
        bound();
        path.lineTo(position.x, position.y);

        invalidate();
    }

    /**
     * Bound position.
     */
    private void bound() {
        if (position.x > width) {
            position.x = width;
        } else if (position.x < 0) {
            position.x = 0;
        }
        if (position.y > height) {
            position.y = height;
        } else if (position.y < 0) {
            position.y = 0;
        }
    }

    private void limitRad(){
        for (int i=0; i<4; i++){
            if (rad[i] < MIN_RAD) {
                rad[i] = MIN_RAD;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.GRAY);

        pathMeasure.setPath(path, false);
        float length = pathMeasure.getLength();

        if (length > DRONE_TRAIL_DP * density) {
            // Note - this is likely a poor way to accomplish the result. Just for demo purposes.
            @SuppressLint("DrawAllocation")
            PathEffect effect = new DashPathEffect(new float[]{length, length}, -length + DRONE_TRAIL_DP * density);
            paint.setPathEffect(effect);
        }

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.FILL);
        //canvas.drawCircle(position.x, position.y, DRONE_RADIUS_DP * density, paint);
        //canvas.rotate(30, position.x, position.y);
        canvas.drawText("U", position.x, position.y, paint);


        for (int i = 0 ; i<4 ; i++){
            paints[i].setStyle(Paint.Style.FILL);
            canvas.drawCircle(positions[i].x, positions[i].y, rad[i] * density, paints[i]);
        }


    }
}
