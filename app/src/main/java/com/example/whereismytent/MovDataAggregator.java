package com.example.whereismytent;

public class MovDataAggregator {

    public static final double FULL_CIRCLE = Math.PI * 2;
    public static final int DIM_NUM = 3;
    private float[] data = new float[DIM_NUM];
    private M pm;
    private long prevTs;
    private RotDataAggregator rda;

    public MovDataAggregator(RotDataAggregator rda) {
        this.rda = rda;
    }

    public void aggregate(M m) {
        if (pm == null) {
            pm = m;
        } else if (m.date > pm.date) {
            float dt = (float)(m.date - pm.date) / 1000;
            float[] angles = rda.getAggregated();

            float[] rm = new float[DIM_NUM];
            for (int i = 0; i < DIM_NUM; i++) {
                rm[i] = m.d[i];
            }
            rm[0] = rm[0] * (float)Math.cos(angles[1]) + rm[2] * (float)Math.sin(angles[1]);
            rm[2] = rm[2] * (float)Math.cos(angles[1]) + rm[0] * (float)Math.sin(angles[1]);
            
            rm[1] = rm[1] * (float)Math.cos(angles[0]) + rm[2] * (float)Math.sin(angles[0]);
            rm[2] = rm[2] * (float)Math.cos(angles[0]) + rm[1] * (float)Math.sin(angles[0]);

            rm[1] = rm[1] * (float)Math.cos(angles[2]) + rm[0] * (float)Math.sin(angles[2]);
            rm[0] = rm[0] * (float)Math.cos(angles[2]) + rm[1] * (float)Math.sin(angles[2]);

            for (int i = 0; i < DIM_NUM; i++) {
                data[i] += rm[i] * dt;
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
