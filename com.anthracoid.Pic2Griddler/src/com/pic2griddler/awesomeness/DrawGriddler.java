
package com.pic2griddler.awesomeness;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawGriddler extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    int brush = Color.WHITE;
    String values[] = new String[2];

    public DrawGriddler(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        Log.d("MAKE", "1");
    }

    public DrawGriddler(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        Log.d("MAKE", "2");
        Thread drawThread = new Thread(this);
        drawThread.start();
    }

    public DrawGriddler(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        Log.d("MAKE", "3");
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (canvas != null) {
            canvas.drawColor(Color.BLACK);

            Paint p = new Paint();
            p.setColor(brush);

            // Draw game board.
            p.setColor(Color.RED);
            for (int i = 0; i < values[0].length(); i++) {
                canvas.drawText(values[0].charAt(i) + "", 25 + 50 * i, 10, p);
            }
            for (int i = 0; i <= values[0].length(); i++) {
                canvas.drawLine(i * 50 + 10, 10, i * 50 + 10, 10 + (values[1].length() * 50), p);
            }

            for (int i = 0; i < values[1].length(); i++) {
                canvas.drawText(values[1].charAt(i) + "", 0, 30 + 50 * i, p);
            }
            for (int i = 0; i <= values[1].length(); i++) {
                canvas.drawLine(10, i * 50 + 10, 10 + (values[0].length() * 50), i * 50 + 10, p);
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        Log.d("LOCATION", me.getX() + " " + me.getY());
        return true;
    }

    public void run() {
        Canvas c;
        while (true) {
            c = null;
            SurfaceHolder surface = getHolder();
            try {
                c = surface.lockCanvas(surface.getSurfaceFrame());
                synchronized (surface) {
                    onDraw(c);
                }
            } finally {
                if (c != null) {
                    surface.unlockCanvasAndPost(c);
                }
            }
        }

    }

    public void setColor(int c) {
        // TODO Auto-generated method stub
        brush = c;
    }

    public void setValues(String one, String two) {
        values[0] = one;
        values[1] = two;
    }

    public void surfaceChanged(SurfaceHolder sh, int format, int width, int height) {
        // TODO Auto-generated method stub
        Log.d("STATUS", "Changing");

    }

    public void surfaceCreated(SurfaceHolder sh) {
        Log.d("STATUS", "Creating");
    }

    public void surfaceDestroyed(SurfaceHolder sh) {
        // TODO Auto-generated method stub
        Log.d("STATUS", "Dieing");

    }

}
