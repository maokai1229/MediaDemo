package com.bailun.kai.audiopickdemo;

import android.app.Application;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author : kai.mao
 * @date :  2021/1/23
 */

public class AudioClipHelper {

    private static AudioClipHelper INSTANCE;
    private MediaExtractor mMediaExtractor;

    private AudioClipHelper() {
    }

    public static AudioClipHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AudioClipHelper();
        }
        return INSTANCE;
    }

    public void setDataSource(String filePath) {
        if (mMediaExtractor == null) {
            mMediaExtractor = new MediaExtractor();
        }

        try {
            mMediaExtractor.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readAuido(int startTime,int endTime) throws IOException {
        Log.e("David", "开始转换");
        int maxBufferSize;
        //1. 查找文件的音频轨道
        int audioIndex = selectAudioTrack(mMediaExtractor);

        //2. 指定音频轨道
        mMediaExtractor.selectTrack(audioIndex);
        mMediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        //3. 解码音频数据，由封转格式 -> PCM 裸数据
        MediaFormat originAudioFormat = mMediaExtractor.getTrackFormat(audioIndex);
        if (originAudioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)){
            maxBufferSize = originAudioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        }else {
            maxBufferSize = 100 * 1000;
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        // 使用 MediaCodec 解码器解码音频数据
        MediaCodec mediaCodec = MediaCodec.createDecoderByType(originAudioFormat.getString(MediaFormat.KEY_MIME));
        mediaCodec.configure(originAudioFormat,null,null,0);
        File pcmFile = new File(Environment.getExternalStorageDirectory(), "out.pcm");
        FileChannel writeChannel = new FileOutputStream(pcmFile).getChannel();
        mediaCodec.start();

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outputBufferIndex = -1;

        // 读取解码后的数据
        while (true){
            // 获取空闲的写入缓冲区
            int decodeInputIndex = mediaCodec.dequeueInputBuffer(100000);
            // 已经拿到可以使用的写入缓冲区
            if (decodeInputIndex >= 0) {
                // 将预定时间间隔内的音频数据塞入缓冲区
                long sampleTimeUs = mMediaExtractor.getSampleTime();
                 if (sampleTimeUs == -1){
                     break;
                 }else if (sampleTimeUs < startTime){
                     mMediaExtractor.advance();
                     continue;
                 }else if (sampleTimeUs > endTime){
                     break;
                 }
                // 获取数据
                info.size = mMediaExtractor.readSampleData(buffer, 0);
                info.presentationTimeUs = sampleTimeUs;
                info.flags = mMediaExtractor.getSampleFlags();
                //  通过 remaining 方法高效读取数据
                byte[] content = new byte[buffer.remaining()];
                buffer.get(content);
                FileUtils.writeContent(content);
                ByteBuffer inputByteBuffer = mediaCodec.getInputBuffer(decodeInputIndex);
                inputByteBuffer.put(content);
                mediaCodec.queueInputBuffer(decodeInputIndex,0,info.size,info.presentationTimeUs,info.flags);
                Log.e("David", "presentationTimeUs："+sampleTimeUs);
                mMediaExtractor.advance();
            }
            // 取出解码后的 pcm 裸数据
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(info,100_000);
            while (outputBufferIndex >= 0){
                ByteBuffer decodeOutputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                writeChannel.write(decodeOutputBuffer);//MP3  1   pcm2
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 100_000);
            }
        }
        writeChannel.close();
        mMediaExtractor.release();
        mediaCodec.stop();
        mediaCodec.release();

        File wavFile = new File(Environment.getExternalStorageDirectory(),"output.mp3" );
        new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
                2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmFile.getAbsolutePath()
                , wavFile.getAbsolutePath());
        Log.i("David", "mixAudioTrack: 转换完毕");

    }



    private int selectAudioTrack(MediaExtractor mediaExtractor) {
        int trackTotal = mediaExtractor.getTrackCount();
        for (int index = 0; index < trackTotal; index++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(index);
            // 获取格式类型为 audio 的轨道
            if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                return index;
            }
        }
        return -1;
    }


    /**
     * 音频混合
     * @param originFilePath 待合成文件1
     * @param otherFilePath 待合成文件2
     * @param newFilePath 新文件
     * @param vol 音量范围 0-100
     * @param otherVol
     */
    public void mixAudio(String originFilePath,String  otherFilePath,String newFilePath,int vol,int otherVol){
        final int BUFFER_SIZE = 2048;
        byte[] buffer1 = new byte[BUFFER_SIZE];
        byte[] buffer2 = new byte[BUFFER_SIZE];
        byte[] buffer3 = new byte[BUFFER_SIZE];


    }


}
