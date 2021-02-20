package com.bailun.kai.audiopickdemo.audio;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.bailun.kai.audiopickdemo.audio.callback.IAudioListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 音频解码器（硬解）
 * @author : kai.mao
 * @date :  2021/2/20
 */
public class AudioDecoder {

    private final static String AUDIO_START_FLAG = "audio/";
    private final static int DEQUEUE_TIMEOUT = 100_000;
    private final static String TAG = "AudioDecoder";
    private String audioPath;
    private String outputPath;
    private int startTime;
    private int endTime;

    public AudioDecoder(String audioPath, String outputPath, int startTime, int endTime) {
        this.audioPath = audioPath;
        this.outputPath = outputPath;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @SuppressLint("WrongConstant")
    public void decode(IAudioListener iAudioListener) throws IOException {
        if (endTime < startTime){
            return;
        }
        MediaExtractor mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(audioPath);

        int audioTrackIndex = selectAudioTrack(mediaExtractor);
        if (audioTrackIndex >= 0){
            mediaExtractor.selectTrack(audioTrackIndex);
            mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            MediaFormat oriAudioFormat = mediaExtractor.getTrackFormat(audioTrackIndex);
            int maxBufferSize = 100 * 1000;
            if (oriAudioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                maxBufferSize = oriAudioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            } else {
                maxBufferSize = 100 * 1000;
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
            MediaCodec mediaCodec = MediaCodec.createDecoderByType(oriAudioFormat.getString((MediaFormat.KEY_MIME)));
            mediaCodec.configure(oriAudioFormat, null, null, 0);
            File pcmFile = new File(outputPath);
            FileChannel writeChannel = new FileOutputStream(pcmFile).getChannel();
            mediaCodec.start();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int outputBufferIndex = -1;
            while (true) {
                int decodeInputIndex = mediaCodec.dequeueInputBuffer(DEQUEUE_TIMEOUT);
                if (decodeInputIndex >= 0) {
                    long sampleTimeUs = mediaExtractor.getSampleTime();

                    if (sampleTimeUs == -1) {
                        break;
                    } else if (sampleTimeUs < startTime) {
                        //丢掉 不用了
                        mediaExtractor.advance();
                        continue;
                    }else if (sampleTimeUs > endTime) {
                        break;
                    }
                    //获取到压缩数据
                    info.size = mediaExtractor.readSampleData(buffer, 0);
                    info.presentationTimeUs = sampleTimeUs;
                    info.flags = mediaExtractor.getSampleFlags();

                    //下面放数据  到dsp解码
                    byte[] content = new byte[buffer.remaining()];
                    buffer.get(content);

                    //解码
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(decodeInputIndex);
                    inputBuffer.put(content);
                    mediaCodec.queueInputBuffer(decodeInputIndex, 0, info.size, info.presentationTimeUs, info.flags);
                    //释放上一帧的压缩数据
                    mediaExtractor.advance();
                }

                outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, DEQUEUE_TIMEOUT);
                while (outputBufferIndex>=0) {
                    ByteBuffer decodeOutputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                    writeChannel.write(decodeOutputBuffer);//MP3  1   pcm2
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, DEQUEUE_TIMEOUT);
                }
            }
            writeChannel.close();
            mediaExtractor.release();
            mediaCodec.stop();
            mediaCodec.release();
            Log.i(TAG, "decode: 转换完毕");
            if (iAudioListener != null){
                iAudioListener.onOperateFinish(pcmFile);
            }

        }

    }


    private int selectAudioTrack(MediaExtractor mediaExtractor) {
        int trackTotal = mediaExtractor.getTrackCount();
        for (int index = 0; index < trackTotal; index++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(index);
            // 获取格式类型为 audio 的轨道
            if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith(AUDIO_START_FLAG)) {
                return index;
            }
        }
        return -1;
    }

}
