package com.example.whereismytent;

import java.util.ArrayList;
import java.util.List;

public class MovDataAggregator {

    public static final double FULL_CIRCLE = Math.PI * 2;
    public static final int DIM_NUM = 3;
    private float[] data = new float[DIM_NUM];
    private M pm;
    private long prevTs;
    private float[] g = new float[DIM_NUM];
    private float[] sigmaG = new float[DIM_NUM];
    private RotDataAggregator rda;
    private List<M> array = new ArrayList<>(100);


    public MovDataAggregator(RotDataAggregator rda) {
        this.rda = rda;
    }

    public float[] getG() {
        return g;
    }

    public void aggregate(M m) {
        if (pm == null) {
            array.add(m);
            if (array.size() == 10) {
                for (M mv: array) {
                    for (int i = 0; i < DIM_NUM; i++) {
                        g[i] += mv.d[i];
                    }
                }
                for (int i = 0; i < DIM_NUM; i++) {
                    g[i] /= 100;
                }
                pm = m;
            }
        } else if (m.date > pm.date) {
            vector = m.d;
            float dt = (float) (m.date - pm.date) / 1000;
            float[] angles = rda.getAggregated();

            float[] rm = new float[DIM_NUM];
            for (int i = 0; i < DIM_NUM; i++) {
                rm[i] = m.d[i];
            }
            float[] aangles = new float[3];
            for (int i = 0; i < DIM_NUM; i++) {
                aangles[i] = angles[i];
            }
            float rmv = rm[0] * (float) Math.cos(aangles[1]) + (rm[2] * (float) Math.sin(aangles[1])) * getPos(angles[1]);
            rm[2] = rm[2] * (float) Math.cos(aangles[1]) + (rm[0] * (float) Math.sin(aangles[1])) * getPos(angles[1]);
            rm[0] = rmv;

            rmv = rm[1] * (float) Math.cos(aangles[0]) * -1 + rm[2] * (float) Math.sin(aangles[0]) ;
            rm[2] = rm[2] * (float) Math.cos(aangles[0]) + rm[1] * (float) Math.sin(aangles[0]) ;
            rm[1] = rmv;

            rmv = rm[1] * (float) Math.cos(aangles[2]) + rm[0] * (float) Math.sin(aangles[2]) ;
            rm[0] = rm[0] * (float) Math.cos(aangles[2]) + rm[1] * (float) Math.sin(aangles[2]);
            rm[1] = rmv;

            rotatedVector = rm;

            for (int i = 0; i < DIM_NUM; i++) {
                data[i] += (rm[i] - g[i]) * dt;
            }
            pm = m;
        }
    }

    public static int getPos(float v) {
        if (v < 0) {
            return 1;
        } else {
            return 1;
        }
    }

    private float[] rotatedVector;
    private float[] vector;

    public float[] getVector() {
        return vector;
    }

    public float[] getRotatedVector() {
        return rotatedVector;
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
