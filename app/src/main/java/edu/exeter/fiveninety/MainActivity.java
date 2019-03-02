package edu.exeter.fiveninety;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    int spectrumLength = 25;
    int width = 25, height = 25;
    float[][] spectrum = new float[spectrumLength][spectrumLength];
    SoundPool sp;
    int violin, piano, violinStream;
    int first = 0;
    Context context;
    float[] violinBounds = {0.5f, 2.0f}; // min, max frequencies/rates
    float[] rowBounds = {0.5f, 2.0f};
    float[] colBounds = {0.5f, 2.0f};
    float freq, oldFreq; // currently only for ambientsounds
    int[] p1, p2;
    int[][] diff;
    private static final String TAG = "CapturePicture";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
            Log.wtf("FiveNinety", "Why");
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
            Log.wtf("FiveNinety", "Why");
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    0);
            Log.wtf("FiveNinety", "Why");
        }
        super.onCreate(savedInstanceState);
        sp = new SoundPool(7, AudioManager.STREAM_MUSIC, 0);
        violin = sp.load(this, R.raw.violin_a2, 0);
        piano = sp.load(this, R.raw.piano_a2, 0);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                oldSounds(spectrum, 0.5f, 2.0f);
                violinStream = sp.play(violin, 1.0f, 1.0f, 100, -1, 1.0f);
//                while (true) {
////                    sounds(sampleDiff);
//                    ambientSounds(spectrum, 3000);
////                    try {
////                        Thread.sleep(3000);
////                        Log.wtf("POG", "champ");
////                    } catch (InterruptedException w) {
////                        Log.wtf("POG", w);
////                    }
//                }
                while (true) {
                    first++;
                    analyzeImage();
//                    sounds(spectrum);
                    ambientSounds(spectrum, 50);
                }
            }
        });
        setContentView(R.layout.activity_main);
    }

    protected void oldSounds(float[] spectrum, float min, float max) { // piano with changing volumes
        for (int i = 0; i < spectrumLength; i++) {
            float freq = min * (float) Math.pow(max / min, (float) i / (spectrumLength - 1));
            sp.play(piano, f(spectrum[i]), f(spectrum[i]), 100, 0, freq);
        }
    }
  
    protected void analyzeImage() {
        String root = Environment.getExternalStorageDirectory().toString() + "/Android/data/edu.exeter.fiveninety/files";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        Bitmap b1, b2;
        int W, H;
        int t1, t2;
        int h = 10, m = 0, s = 0;
        Log.wtf("FiveNinety", root);
        String pattern;
        File test;
        while (true)
        {
            s--;
            if (s < 0)
            {
                s += 60;
                m--;
            }
            if (m < 0) {
                m += 60;
                h--;
            }
            pattern = "/2019-03-02_" + String.format("%02d", h) + "_" + String.format("%02d", m) + "_" + String.format("%02d", s) + ".jpg";
            test = new File(root + pattern);
            if (test.exists())
                break;
        }
        b1 = BitmapFactory.decodeFile(root + pattern, options);
        while (true)
        {
            s--;
            if (s < 0)
            {
                s += 60;
                m--;
            }
            if (m < 0) {
                m += 60;
                h--;
            }
            pattern = "/2019-03-02_" + String.format("%02d", h) + "_" + String.format("%02d", m) + "_" + String.format("%02d", s) + ".jpg";
            test = new File(root + pattern);
            if (test.exists())
                break;
        }
        b2 = BitmapFactory.decodeFile(root + pattern, options);
        W = b1.getWidth();
        H = b1.getHeight();
        if (first == 1)
            p1 = new int[W*H];
        b1.getPixels(p1, 0, W, 0, 0, W, H);
        if (first == 1)
            p2 = new int[W*H];
        b2.getPixels(p2, 0, W, 0, 0, W, H);
        if (first == 1)
        diff = new int[W/5 + 1][H/5 + 1];
        for (int i = 0; i < spectrumLength; i++)
            for (int j = 0; j < spectrumLength; j++)
                spectrum[i][j] = 0.0f;
        for (int i = 0; i < W; i += 5)
        {
            for (int j = 0; j < H; j += 5) {
                t1 = p1[j*W + i];
                t2 = p2[j*W + i];
                spectrum[i*spectrumLength/W][j*spectrumLength/H] += Math.abs(Color.red(t1) - Color.red(t2)) +
                        Math.abs(Color.green(t1) - Color.green(t2)) +
                        Math.abs(Color.blue(t1) - Color.blue(t2));
            }
        }
        for (int i = 0; i < spectrumLength; i++) {
            for (int j = 0; j < spectrumLength; j++) {
                spectrum[i][j] /= (255.0f*3*W*H)/(spectrumLength*spectrumLength);
                spectrum[i][j] = (float) Math.pow(spectrum[i][j], 0.1);
                Log.wtf("FiveNinety", "analyzeImage: " + spectrum[i][j]);
            }
        }
    }

    protected float f(float x) { // function on spectrum inputs f:[0,1]->[0,1]
        return (float) Math.pow(x, 2);
    }

    protected void sounds(float[][] diffArray) {
        // violin: pitch higher when disturbed more
        // row: first piano; plays a tone corresponding to the row with the greatest disturbance
        // col: second piano; same but with columns
        int width = diffArray[0].length;
        int height = diffArray.length;
        float avg = 0;
        float[] rowAvg = new float[height];
        float[] colAvg = new float[width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                avg += f(diffArray[i][j]) / (width * height);
                rowAvg[i] += f(diffArray[i][j]) / width;
                colAvg[j] += f(diffArray[i][j]) / height;
            }
        }
        int maxRow = 0;
        for (int i = 0; i < height; i++) {
            if (rowAvg[i] > rowAvg[maxRow])
                maxRow = i;
        }
        int maxCol = 0;
        for (int j = 0; j < width; j++) {
            if (colAvg[j] > colAvg[maxCol])
                maxCol = j;
        }
        float freq = violinBounds[0] * (float) Math.pow(violinBounds[1] / violinBounds[0], avg);
        float rowFreq = rowBounds[0] * (float) Math.pow(rowBounds[1] / rowBounds[0], maxRow / (height - 1));
        float colFreq = colBounds[0] * (float) Math.pow(colBounds[1] / colBounds[0], maxCol / (width - 1));
        sp.setRate(violinStream, freq);
        sp.play(piano, rowAvg[maxRow], rowAvg[maxRow], 100, 0, rowFreq);
        sp.play(piano, colAvg[maxCol], colAvg[maxCol], 100, 0, colFreq);
    }

    protected void ambientSounds (float[][] diffArray, long interval) {
        // right now just the violin layer with 3000ms smoothing applied
        int width = diffArray[0].length;
        int height = diffArray.length;
        float avg = 0.0f;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                avg += f(diffArray[i][j]) / (width * height);
            }
        }
        long oldTime = System.currentTimeMillis();
        Log.wtf("yes", oldTime + "");
        oldFreq = freq;
        freq = violinBounds[0] * (float) Math.pow(violinBounds[1] / violinBounds[0], avg);
        long currTime = 0;
        while (currTime < interval) {
            currTime = System.currentTimeMillis() - oldTime;
            Log.wtf("no", System.currentTimeMillis() + " " + oldTime + " " + currTime);
            float currFreq = oldFreq * (float) Math.pow(freq / oldFreq, ((float) currTime) / ((float) interval));
            Log.wtf("asdf", oldFreq + " " + freq + " " + currFreq + "");
            sp.setRate(violinStream, currFreq);
        }
    }
}
