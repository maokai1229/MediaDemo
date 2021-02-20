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

import com.bailun.kai.audiopickdemo.audio.AudioJet;
import com.bailun.kai.audiopickdemo.audio.callback.IAudioListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private Button mBtClip;
    private int successNum;
    private Button mBtMix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        initView();
    }

    private void initView() {
        mBtClip = findViewById(R.id.bt_start);
        mBtMix = findViewById(R.id.bt_mix);
        mBtClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clip();
            }
        });
        mBtMix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mixAudio();
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

                AudioJet.getInstance().decodeToPCM(aacPath, outPath, 5 * 1000 * 1000, 8 * 1000 * 1000, new IAudioListener() {
                        @Override
                        public void onOperateFinish(File pcmFile) {
                            File wavFile = new File(Environment.getExternalStorageDirectory(),"output1122.mp3" );
                            new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
                                    2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmFile.getAbsolutePath()
                                    , wavFile.getAbsolutePath());
                            Log.i("David", "mixAudioTrack: 转换完毕"); }
                    });
            }
        }).start();
    }



    private void mixAudio(){
        new Thread(new Runnable() {
           @Override
           public void run() {
               final String aacPath = new File(Environment.getExternalStorageDirectory(), "music.mp3").getAbsolutePath();
               final String videoAAPath = new File(Environment.getExternalStorageDirectory(), "input2.mp4").getAbsolutePath();
               try {
                   copyAssets("music.mp3", aacPath);

                   copyAssets("input2.mp4", videoAAPath);
               } catch (IOException e) {
                   e.printStackTrace();
               }
               // 找到音频轨道，解码
               final File firstPCM = new File(Environment.getExternalStorageDirectory(), "first.pcm");
               final File secondPCM = new File(Environment.getExternalStorageDirectory(), "second.pcm");
               final String outPathPcm = new File(Environment.getExternalStorageDirectory(), "outPut.mp3").getAbsolutePath();

               successNum = 0;
               AudioJet.getInstance().decodeToPCM(aacPath, firstPCM.getAbsolutePath(), 60 * 1000 * 1000, 70 * 1000 * 1000, new IAudioListener() {
                   @Override
                   public void onOperateFinish(File pcmFile) {
                       successNum++;
                       if (successNum == 2){
                           AudioJet.getInstance().mixAudio(firstPCM.getAbsolutePath(), secondPCM.getAbsolutePath(), outPathPcm, 100,//0 - 100
                                   10, new IAudioListener() {
                                       @Override
                                       public void onOperateFinish(File mixpcmFile) {
                                           new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
                                                   2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(mixpcmFile.getAbsolutePath()
                                                   , outPathPcm);
                                       }
                                   });
                       }
                   }
               });

               AudioJet.getInstance().decodeToPCM(videoAAPath, secondPCM.getAbsolutePath(), 60 * 1000 * 1000, 70 * 1000 * 1000, new IAudioListener() {
                   @Override
                   public void onOperateFinish(File pcmFile) {
                       successNum++;
                       if (successNum == 2){
                           AudioJet.getInstance().mixAudio(firstPCM.getAbsolutePath(),secondPCM.getAbsolutePath(),outPathPcm,100,//0 - 100
                                   10,new IAudioListener() {
                                       @Override
                                       public void onOperateFinish(File mixpcmFile) {
                                           new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
                                                   2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(mixpcmFile.getAbsolutePath()
                                                   , outPathPcm);
                                       }
                                   });
                       }
                   }
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