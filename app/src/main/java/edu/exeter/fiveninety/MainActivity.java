package edu.exeter.fiveninety;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    float[] Spectrum = new float[2048];
    int[] p1, p2;
    int[][] diff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
            Log.wtf("FiveNinety", "Why");
        } else {
            // Permission has already been granted
            Log.wtf("FiveNinety", "Hello");
            analyzeImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        int okay = 0;
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    okay++;
                }
                break;
            }
        }
        if (okay == 1)
            analyzeImage();
    }

    protected void analyzeImage() {
        String root = Environment.getExternalStorageDirectory().toString() + "/ripple/";
        Bitmap b1, b2;
        int W, H;
        int t1, t2;
        Log.wtf("FiveNinety", root);
        b1 = BitmapFactory.decodeFile(root + "01.jpg");
        b2 = BitmapFactory.decodeFile(root + "02.jpg");
        W = b1.getWidth();
        H = b1.getHeight();
        p1 = new int[W*H];
        b1.getPixels(p1, 0, W, 0, 0, W, H);

        p2 = new int[W*H];
        b2.getPixels(p2, 0, W, 0, 0, W, H);

        diff = new int[W][H];
        for (int j = 0; j < 2048; j++)
            Spectrum[j] = 0;
        for (int i = 0; i < W; i++)
        {
            for (int j = 0; j < H; j++) {
                t1 = p1[j*W + i];
                t2 = p2[j*W + i];
//                diff[i][j] = t1^t2;
                diff[i][j] = Math.abs(Color.red(t1) - Color.red(t2)) +
                        Math.abs(Color.green(t1) - Color.green(t2)) +
                        Math.abs(Color.blue(t1) - Color.blue(t2));
                Spectrum[i*2048/W] += diff[i][j];
//                Log.wtf("FiveNinety", "" + i + j);
            }
        }
        for (int j = 0; j < 2048; j++) {
            Spectrum[j] /= 255 * 3 * H * W / 2048;
            Log.wtf("FiveNinety", "analyzeImage: " + Spectrum[j]);
        }
    }
}
