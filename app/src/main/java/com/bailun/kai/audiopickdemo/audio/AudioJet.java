package com.bailun.kai.audiopickdemo.audio;

import com.bailun.kai.audiopickdemo.audio.callback.IAudioListener;

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
    public void decodeToPCM(String originFilePath, String pcmFilePath, int startTime, int endTime, IAudioListener listener) {
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
    public void mixAudio(String originFilePath,String  otherFilePath,String newFilePath,int vol,int otherOriginVol, IAudioListener listener){
        AudioMixer audioMixer = new AudioMixer(originFilePath,originFilePath,newFilePath,vol,otherOriginVol);
        audioMixer.start(listener);
    }

}
