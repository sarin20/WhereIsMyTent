package com.example.whereismytent;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class QueuingSensorListener implements SensorEventListener {

    private float[] lastData;
    private AtomicInteger sec;
    private char litera;
    private TextView[] tDataa;
    private ArrayBlockingQueue<M> queue;

    public QueuingSensorListener(AtomicInteger sec, char litera, TextView[] tDataa, ArrayBlockingQueue<M> queue) {
        this.sec = sec;
        this.litera = litera;
        this.tDataa = tDataa;
        this.queue = queue;
    }

    private boolean isChanged(float[] d) {
        if (lastData == null && d != null) {
            return true;
        }
        if (d != null) {
            if (d.length != lastData.length) {
                return true;
            }
            for (int i = 0; i < d.length; i++) {
                if (lastData[i] != d[i]) {
                    lastData = null;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long ts = System.currentTimeMillis();
        int n = sec.incrementAndGet();
        if (isChanged(sensorEvent.values)) {
            queue.add(new M(litera, sensorEvent.values, n, ts));
            lastData = new float[sensorEvent.values.length];
            for (int i = 0; i < lastData.length; i++) {
                lastData[i] = sensorEvent.values[i];
            }
            if (tDataa != null ) {
                for (int i = 0; i < tDataa.length; i++) {
                    tDataa[i].setText(Float.toString(sensorEvent.values[i]));
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}