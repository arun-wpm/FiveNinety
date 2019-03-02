package edu.exeter.fiveninety;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.AudioManager;
import android.util.Log;

import java.io.File;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

//    static final int REQUEST_IMAGE_CAPTURE = 1;
    int spectrumLength = 25;
    float[] spectrum = new float[spectrumLength];
    int width = 25, height = 25;
    float[][] sampleDiff = new float[width][height];
    SoundPool sp;
    int violin, piano, violinStream;
//    Context context;
//    File dir;
//    MediaPlayer[] mps = new MediaPlayer[spectrumLength];
    float[] violinBounds = {0.5f, 2.0f}; // min, max frequencies/rates
    float[] rowBounds = {0.5f, 2.0f};
    float[] colBounds = {0.5f, 2.0f};
    float freq, oldFreq; // currently only for ambientsounds
    Random rng = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        context = getApplicationContext();
        sp = new SoundPool(7, AudioManager.STREAM_MUSIC, 0);
//        dir = new File(getFilesDir() + "/Justworkgoddammit");
//        boolean ok = dir.mkdirs();
//        Log.wtf("TAG", ok + "" + getFilesDir() + "/Justworkgoddammit");

//        for (int i = 0; i < spectrumLength; i++) {
//            spectrum[i] = (float) Math.pow(1, Math.abs(i - (spectrumLength-1) / 2));
//        }
        violin = sp.load(this, R.raw.violin_a2, 0);
        piano = sp.load(this, R.raw.piano_a2, 0);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                oldSounds(spectrum, 0.5f, 2.0f);
                violinStream = sp.play(violin, 1.0f, 1.0f, 100, -1, 1.0f);
                while (true) {
                    float random = rng.nextFloat();
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < height; j++) {
                            sampleDiff[i][j] = random;
                        }
                    }
//                    sounds(sampleDiff);
                    ambientSounds(sampleDiff, 3000);
//                    try {
//                        Thread.sleep(3000);
//                        Log.wtf("POG", "champ");
//                    } catch (InterruptedException w) {
//                        Log.wtf("POG", w);
//                    }
                }
            }
        });
//        Log.wtf("TAG", "" + soundID);



//        for (int i = 0; i < spectrumLength; i++) {
//            if (i % 2 == 0)
//                mps[i] = MediaPlayer.create(this, R.raw.violin_a2);
//            else
//                mps[i] = MediaPlayer.create(this, R.raw.synth_a0);
//        }
//        mps[1].start();
//        mps[0].start();
        setContentView(R.layout.activity_main);
//        sp.release();
    }

    protected void oldSounds(float[] spectrum, float min, float max) { // piano with changing volumes
        for (int i = 0; i < spectrumLength; i++) {
            float freq = min * (float) Math.pow(max / min, (float) i / (spectrumLength - 1));
            sp.play(piano, f(spectrum[i]), f(spectrum[i]), 100, 0, freq);
        }
        // input amplitude array, min freq, max freq
        // make sounds loopable and short
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
