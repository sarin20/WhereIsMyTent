package com.example.whereismytent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.whereismytent.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    public static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private SensorManager sm;
    private Sensor s;
    private Sensor s2;
    //    private TextView[] tDataa;
    //   private TextView[] tDatar;
    private boolean active;
    private Uri uri;
    private ArrayBlockingQueue<M> q = new ArrayBlockingQueue<M>(10000);
    private AtomicInteger numa = new AtomicInteger();
    private AtomicInteger numr = new AtomicInteger();
    private DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
    private DateFormat fnDf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
    private Switch mode;
    private RotDataAggregator rda = new RotDataAggregator();
    private MovDataAggregator mda = new MovDataAggregator(rda);

    private Runnable dataWriter = new Runnable() {
        @Override
        public void run() {

            List<M> buff = new ArrayList<>(100);

            while (true) {
                try {
                    M m = q.take();
                    putMeasure(buff, m);
                    while (!q.isEmpty()) {
                        putMeasure(buff, q.poll());
                    }
                    final float[] aggRot = rda.getAggregated();
                    if (aggRot != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < 3; i++) {
                                    tDatar[i].setText(Float.toString(aggRot[i]));
                                }
                            }
                        });
                    }
                    final float[] aggMov = mda.getAggregated();
                    if (aggRot != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < 3; i++) {
                                    tDataa[i].setText(Float.toString(aggRot[i]));
                                }
                            }
                        });
                    }
                    //        if (buff.size() > 0) {
                    if (uri != null) {
                        try (OutputStream os = getContentResolver().openOutputStream(uri, "wa")) {
                            try (PrintWriter pw = new PrintWriter(new BufferedOutputStream(os))) {

                                for (M mm : buff) {
                                    pw.println(String.format("%s\t%d\t%d\t%s\t:\t%e\t%e\t%e", mm.type, mm.n, mm.date, df.format(new Date(mm.date)), mm.d[0], mm.d[1], mm.d[2]));
                                }
                            } finally {

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    buff.clear();
                    //     }
                } catch (InterruptedException e) {
                    break;
                }
            }


            if (uri != null) {

            }
        }

        private void putMeasure(List<M> buff, M m) {
            if (m.type == 'A') {
                if (uri != null){
                    buff.add(m);
                }
                mda.aggregate(m);
            } else if (m.type == 'R') {
                rda.aggregate(m);
            }
        }

    };


    private SensorEventListener listener;

    private SensorEventListener listener2;

    private TextView[] tDatar;
    private TextView[] tDataa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread dwt = new Thread(dataWriter);
        dwt.setDaemon(true);
        dwt.start();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        s = sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        s2 = sm.getSensorList(Sensor.TYPE_GYROSCOPE).get(0);
        tDataa = new TextView[]{
                findViewById(R.id.motx),
                findViewById(R.id.moty),
                findViewById(R.id.motz)
        };
        listener = new QueuingSensorListener(numa, 'A', null, q);
        tDatar = new TextView[]{
                findViewById(R.id.rotx),
                findViewById(R.id.roty),
                findViewById(R.id.rotz)
        };
        listener2 = new QueuingSensorListener(numr, 'R', null, q);
        mode = findViewById(R.id.mode);

        createButton();
    }

    private void createButton() {
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                active = !active;
                if (active) {
                    try {
                        Switch sw = findViewById(R.id.save);
                        if (sw.isChecked()) {
                            createFile();
                        } else {
                            start();
                        }
                    } catch (Exception e) {

                    }
                } else {
                    stop();
                }
            }
        });
    }

    private static final int CREATE_FILE = 1;

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "data_xx_" + fnDf.format(new Date()) + (mode.isChecked() ? 'f' : 'n') + "_.txt");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        // intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, CREATE_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == CREATE_FILE
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                TextView v = findViewById(R.id.u);
                v.setText(uri.toString());
                this.uri = uri;
                start();
                // Perform operations on the document using its URI.
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    private void start() {
        if (sm != null && s != null && active) {
            sm.registerListener(listener, s, getSensorDelay());
            sm.registerListener(listener2, s2, getSensorDelay());
        }
    }

    private int getSensorDelay() {

        return mode.isChecked() ? SensorManager.SENSOR_DELAY_FASTEST : SensorManager.SENSOR_DELAY_NORMAL;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    private void stop() {
        if (sm != null && s != null) {
            sm.unregisterListener(listener);
            sm.unregisterListener(listener2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


}