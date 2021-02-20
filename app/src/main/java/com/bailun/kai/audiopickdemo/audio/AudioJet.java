package com.bailun.kai.audiopickdemo.audio;

import android.media.AudioFormat;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import com.bailun.kai.audiopickdemo.PcmToWavUtil;
import com.bailun.kai.audiopickdemo.audio.callback.IAudioDecoderListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 音频工具，能够解码，裁剪，混音
 * @author : kai.mao
 * @date :  2021/1/23
 */

public class AudioJet implements IAudio{

    private static AudioJet INSTANCE;

    private AudioJet() {
    }

    public static AudioJet getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AudioJet();
        }
        return INSTANCE;
    }


    @Override
    public void decodeToPCM(String originFilePath,String pcmFilePath,int startTime,int endTime,IAudioDecoderListener listener) {
        AudioDecoder audioDecoder = new AudioDecoder(originFilePath,pcmFilePath,startTime,endTime);
        try {
            audioDecoder.decode(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 音频混合
     */
    @Override
    public void mixAudio(String originFilePath,String  otherFilePath,String newFilePath,int vol,int otherOriginVol) throws FileNotFoundException {
        final int BUFFER_SIZE = 2048;
        byte[] buffer1 = new byte[BUFFER_SIZE];
        byte[] buffer2 = new byte[BUFFER_SIZE];
        byte[] buffer3 = new byte[BUFFER_SIZE];
        float originVol = normalizeVolume(vol);
        float otherVol = normalizeVolume(otherOriginVol);

        FileInputStream originFileInputStream = new FileInputStream(originFilePath);
        FileInputStream otherFileInputStream = new FileInputStream(otherFilePath);
        FileOutputStream fileOutputStream = new FileOutputStream(newFilePath);

    }




    private float normalizeVolume(int vol){
        return vol / 100f * 1;
    }


}
