package com.bailun.kai.audiopickdemo.audio.callback;

import java.io.File;

/**
 * @author : kai.mao
 * @date :  2021/2/20
 */
public interface IAudioListener {

    void onOperateFinish(File pcmFile);

}
