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

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    int spectrumLength = 25;
    float[] spectrum = new float[spectrumLength];
    SoundPool sp;
    int violin;
    Context context;
    File dir;
//    MediaPlayer[] mps = new MediaPlayer[spectrumLength];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        context = getApplicationContext();
        sp = new SoundPool(spectrumLength, AudioManager.STREAM_MUSIC, 0);
//        dir = new File(getFilesDir() + "/Justworkgoddammit");
//        boolean ok = dir.mkdirs();
//        Log.wtf("TAG", ok + "" + getFilesDir() + "/Justworkgoddammit");
        for (int i = 0; i < spectrumLength; i++) {
            spectrum[i] = (float) Math.pow(1, Math.abs(i - (spectrumLength-1) / 2));
        }
        violin = sp.load(this, R.raw.piano_a2, 0);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                sounds(spectrum, 0.5f, 2.0f);
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

    protected void sounds(float[] spectrum, float min, float max) {
        for (int i = 0; i < spectrumLength; i++) {
            float freq = min * (float) Math.pow(max / min, (float) i / (spectrumLength - 1));
            sp.play(violin, spectrum[i], spectrum[i], 100, -1, freq);
        }
        // input amplitude array, min freq, max freq
        // make sounds loopable and short
    }
}
