package com.example.whereismytent;

public class RotDataAggregator {

    public static final double FULL_CIRCLE = Math.PI * 2;
    public static final int DIM_NUM = 3;
    private float[] data = new float[DIM_NUM];
    private M pm;
    private long prevTs;

    public void aggregate(M m) {
        if (pm == null) {
            pm = m;
        } else if (m.date > pm.date) {
            float dt = (float)(m.date - pm.date) / 1000;
            for (int i = 0; i < DIM_NUM; i++) {
                data[i] += m.d[i] * dt;
                float abs = Math.abs(data[i]);
                if (abs >= FULL_CIRCLE) {
                    data[i] = (abs - (float)(Math.floor(abs / FULL_CIRCLE) * FULL_CIRCLE)) * (data[i] < 0 ? -1 : 1);
                }
            }
            pm = m;
        }
    }

    public float[] getAggregated() {
        if (data == null) {
            return null;
        }
        float[] r = new float[DIM_NUM];
        for (int i = 0; i < DIM_NUM; i++) {
            r[i] = data[i];
        }
        return r;
    }



}
