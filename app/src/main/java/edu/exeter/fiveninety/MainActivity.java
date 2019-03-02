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
    float[][] spectrum = new float[spectrumLength][spectrumLength];
    SoundPool sp;
    int violin;
    int first = 0;
    Context context;
    
    int[] p1, p2;
    int[][] diff;
    private static final String TAG = "CapturePicture";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState){
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
        setContentView(R.layout.activity_main);
        int id = 1;
        while(true){
            first++;
            analyzeImage();
            playSound();
        }
    }
  
    public void playSound() {
        sp = new SoundPool(spectrumLength, AudioManager.STREAM_MUSIC, 0);
        violin = sp.load(this, R.raw.piano_a2, 0);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                sounds(spectrum, 0.5f, 2.0f);
            }
        });
        setContentView(R.layout.activity_main);
    }

    protected void sounds(float[] spectrum, float min, float max) {
        for (int i = 0; i < spectrumLength; i++) {
            float freq = min * (float) Math.pow(max / min, (float) i / (spectrumLength - 1));
            sp.play(violin, spectrum[i], spectrum[i], 100, -1, freq);
        }
    }
  
    protected void analyzeImage() {
        String root = Environment.getExternalStorageDirectory().toString() + "/Android/data/edu.exeter.fiveninety/files";
        Bitmap b1, b2;
        int W, H;
        int t1, t2;
        int h = 23, m = 0, s = 0;
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
            pattern = "/2019-03-01_" + h + "_" + m + "_" + s + ".jpg";
            test = new File(root + pattern);
            if (test.exists())
                break;
        }
        b1 = BitmapFactory.decodeFile(root + pattern);
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
            pattern = "/2019-03-01_" + h + "_" + m + "_" + s + ".jpg";
            test = new File(root + pattern);
            if (test.exists())
                break;
        }
        b2 = BitmapFactory.decodeFile(root + pattern);
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
                spectrum[i*spectrumLength/W][i*spectrumLength/H] += Math.abs(Color.red(t1) - Color.red(t2)) +
                        Math.abs(Color.green(t1) - Color.green(t2)) +
                        Math.abs(Color.blue(t1) - Color.blue(t2));
            }
        }
        for (int i = 0; i < spectrumLength; i++) {
            for (int j = 0; j < spectrumLength; j++) {
                Log.wtf("FiveNinety", "analyzeImage: " + spectrum[i][j]);
            }
        }
    }
}
