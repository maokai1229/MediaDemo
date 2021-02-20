package com.bailun.kai.audiopickdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bailun.kai.audiopickdemo.audio.AudioJet;
import com.bailun.kai.audiopickdemo.audio.callback.IAudioDecoderListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private Button mBtClip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        initView();

    }

    private void initView() {
        mBtClip = findViewById(R.id.bt_start);
        mBtClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clip();
            }
        });
    }

    private void clip() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String aacPath = new File(Environment.getExternalStorageDirectory(), "musictest.mp3").getAbsolutePath();
                final String outPath = new File(Environment.getExternalStorageDirectory(), "out.mp3").getAbsolutePath();
                try {
                    copyAssets("music.mp3", aacPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                AudioJet.getInstance().decodeToPCM(aacPath, outPath, 5 * 1000 * 1000, 8 * 1000 * 1000, new IAudioDecoderListener() {
                        @Override
                        public void onDecodeFinish(File pcmFile) {
                            File wavFile = new File(Environment.getExternalStorageDirectory(),"output1122.mp3" );
                            new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
                                    2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmFile.getAbsolutePath()
                                    , wavFile.getAbsolutePath());
                            Log.i("David", "mixAudioTrack: 转换完毕"); }
                    });
            }
        }).start();
    }


    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

        }
        return false;
    }

    private void copyAssets(String assetsName, String path) throws IOException {
        AssetFileDescriptor assetFileDescriptor = getAssets().openFd(assetsName);
        FileChannel from = new FileInputStream(assetFileDescriptor.getFileDescriptor()).getChannel();
        FileChannel to = new FileOutputStream(path).getChannel();
        from.transferTo(assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength(), to);
    }
}