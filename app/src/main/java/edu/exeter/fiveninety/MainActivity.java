package com.example.pennybrant.cameratest;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.Manifest;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.AudioManager;
import android.util.Log;
import java.io.File;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

static final int REQUEST_IMAGE_CAPTURE = 1;
int spectrumLength = 25;
float[] spectrum = new float[spectrumLength];
SoundPool sp;
int violin;
Context context;
File dir;
//    MediaPlayer[] mps = new MediaPlayer[spectrumLength];

public class camera extends AppCompatActivity {
    private static final String TAG = "CapturePicture";
    private static final int REQUEST_PICTURE_CAPTURE = 1;
    private ImageView image;
    private String picutreFilePath;
    private String deviceIdentifier;
    private String pictureFilePath;
    private ImageView imageView;
    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

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
        setContentView(R.layout.activity_camera);
        image = findViewById(R.id.picture);
        //Button captureButton = findViewById(R.id.capture);
        getInstallationIdentifier();
        int id = 1;
        while(true){
            if(id == 1){
                id = 0;
            }else{
                id = 1;
            }

            try {
                takePhotoIntent(id);
            } catch (IOException e) {
                Log.wtf("FiveNinety", "Why did things go wrong wtf");
            }
            addToGallery();
            playSound();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void takePhotoIntent(int id) throws IOException {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);

            File pictureFile = null;
            pictureFile = getPictureFile(id);
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.zoftino.android.fileprovider",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }
    }

    private void addToGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pictureFilePath);
        Uri picUri = Uri.fromFile(f);
        galleryIntent.setData(picUri);
        this.sendBroadcast(galleryIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private File getPictureFile(int id) throws IOException {
        //String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "Image" + id;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }


    protected void onActivity(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data'");
        imageView.setImageBitmap(bitmap);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected File nameimage() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "ZOFTINO_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        String pictureFilePath = image.getAbsolutePath();
        return image;
    }


    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    static{
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_0, 0);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new  File(pictureFilePath);
            if(imgFile.exists())            {
                image.setImageURI(Uri.fromFile(imgFile));
            }
        }
        // input amplitude array, min freq, max freq
        // make sounds loopable and short
    }

    protected synchronized String getInstallationIdentifier() {
        if (deviceIdentifier == null) {
            SharedPreferences sharedPrefs = this.getSharedPreferences(
                    "DEVICE_ID", Context.MODE_PRIVATE);
            deviceIdentifier = sharedPrefs.getString("DEVICE_ID", null);
            if (deviceIdentifier == null) {
                deviceIdentifier = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("DEVICE_ID", deviceIdentifier);
                editor.commit();
            }
        }
        return deviceIdentifier;
    }

    /*static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, actionCode);
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        String mCurrentPhotoPath;
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }*/
  
  public void PlaySound() {
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
    }
}
