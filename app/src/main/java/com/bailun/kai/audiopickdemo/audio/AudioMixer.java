package com.bailun.kai.audiopickdemo.audio;

import android.os.Environment;
import android.util.Log;

import com.bailun.kai.audiopickdemo.audio.callback.IAudioListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 音频混音，接受 PCM 数据
 * @author : kai.mao
 * @date :  2021/2/20
 */
public class AudioMixer {

    private final static String TAG = "AudioMixer";
    private String firstPcmPath;
    private String secondPcmPath;
    private String outputPcmPath;
    private int firstVol;
    private int secondVol;

    public AudioMixer(String firstPcmPath, String secondPcmPath, String outputPcmPath, int firstVol, int secondVol) {
        this.firstPcmPath = firstPcmPath;
        this.secondPcmPath = secondPcmPath;
        this.outputPcmPath = outputPcmPath;
        this.firstVol = firstVol;
        this.secondVol = secondVol;
    }

    private float normalizeVolume(int vol){
        return vol / 100f * 1;
    }

    public void start(IAudioListener iAudioListener){
        final File mixPcmFile = new File(Environment.getExternalStorageDirectory(), outputPcmPath);
        try {
            mixPcm(firstPcmPath,secondPcmPath, mixPcmFile.getAbsolutePath(), firstVol, secondVol);
            if (iAudioListener != null){
                iAudioListener.onOperateFinish(mixPcmFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //vol1  vol2  0-100  0静音  120
    private void mixPcm(String pcm1Path, String pcm2Path, String toPath
            , int volume1, int volume2) throws IOException {
        float vol1 = normalizeVolume(volume1);
        float vol2 = normalizeVolume(volume2);
        //一次读取多一点 2k
        byte[] buffer1 = new byte[2048];
        byte[] buffer2 = new byte[2048];
        //待输出数据
        byte[] buffer3 = new byte[2048];

        FileInputStream is1 = new FileInputStream(pcm1Path);
        FileInputStream is2 = new FileInputStream(pcm2Path);

        //输出PCM 的
        FileOutputStream fileOutputStream = new FileOutputStream(toPath);
        short temp2, temp1;//   两个short变量相加 会大于short   声音
        int  temp;
        boolean end1 = false, end2 = false;
        while (!end1 || !end2) {

            if (!end1) {
                end1 = (is1.read(buffer1) == -1);
                //音乐的pcm数据  写入到 buffer3
                System.arraycopy(buffer1, 0, buffer3, 0, buffer1.length);
            }

            if (!end2) {
                end2 = (is2.read(buffer2) == -1);
                int voice = 0;//声音的值  跳过下一个声音的值    一个声音 2 个字节
                for (int i = 0; i < buffer2.length; i += 2) {
                    //或运算
                    temp1 = (short) ((buffer1[i] & 0xff) | (buffer1[i + 1] & 0xff) << 8);
                    temp2 = (short) ((buffer2[i] & 0xff) | (buffer2[i + 1] & 0xff) << 8);
                    temp = (int) (temp1*vol1 + temp2*vol2);//音乐和 视频声音 各占一半
                    if (temp > 32767) {
                        temp = 32767;
                    }else if (temp < -32768) {
                        temp = -32768;
                    }
                    buffer3[i] = (byte) (temp & 0xFF);
                    buffer3[i + 1] = (byte) ((temp >>> 8) & 0xFF);
                }
                fileOutputStream.write(buffer3);
            }
        }
        is1.close();
        is2.close();
        fileOutputStream.close();
        Log.i(TAG, "decode: 转换完毕");
    }

}
