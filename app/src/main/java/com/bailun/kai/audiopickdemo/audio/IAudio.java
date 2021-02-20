package com.bailun.kai.audiopickdemo.audio;

import com.bailun.kai.audiopickdemo.audio.callback.IAudioListener;

import java.io.FileNotFoundException;

/**
 * @author : kai.mao
 * @date :  2021/2/20
 */
public interface IAudio {

    /**
     * 解码为 PCM 裸数据
     * @param originFilePath
     */
    void decodeToPCM(String originFilePath, String pcmFilePath, int startTime, int endTime, IAudioListener listener);


    /**
     * 音频混音
     * @param firstPcmPath
     * @param secondPcmPath
     * @param newFilePath
     * @param firstVol
     * @param secondVol
     */
    void mixAudio(String firstPcmPath,String secondPcmPath,String newFilePath,int firstVol,int secondVol, IAudioListener listener);


}
