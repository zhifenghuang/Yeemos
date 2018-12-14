package com.yeemos.app.glsurface.utils;

/**
 * Created by gigabud on 16-12-29.
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.nio.ByteBuffer;

public class CameraRecorder {

    private static final String TAG = "CameraRecorder";

    private MediaCodec mVideoCodec = null;
    private MediaCodec mAudioCodec = null;
    private InputSurface mInputSurface = null;
    private MediaCodec.BufferInfo mVieoBufferInfo = null, mAudioBufferInfo = null;
    private MediaMuxer mMediaMuxer = null;
    private int mVideoTrackIndex = -1, mAudioTrackIndex = -1;
    private boolean mMuxerStarted = false;

    private long mStartPauseTime;  //暂停起始时间
    private long mPauseTime;   //暂停时长
    private int mNumTracksAdded;  //mNumTracksAdded==2时，mMediaMuxer才能start()，否则跑出异常

    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int AUDIO_SAMPLE_RATE = 44100;    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    private static final int AUDIO_BIT_RATE = 64000;
    private static final int AUDIO_SAMPLES_PER_FRAME = 1024;    // AAC, bytes/frame/channel
    private static final int AUDIO_FRAMES_PER_BUFFER = 30;    // AAC, frame/buffer/sec
    private static final int AUDIO_CHANNEL_COUNT = 2;

    private static final String VIDEO_MIME = "video/avc";
    private static final int VIDEO_BPS = 4 * 1024 * 1024;
    private static final int VIDEO_IFI = 5;
    private static final int VIDEO_FRAME_RATE = 30;

    /**
     * previous presentationTimeUs for writing
     */
    private long mPrevOutputPTSUs = 0;
    private static final int TIMEOUT_USEC = 10000;    // 10[msec]

    private AudioThread mAudioThread;

    public void prepareEncoder(String videoPath) {
        if (mVideoCodec != null || mAudioCodec != null || mInputSurface != null) {
            throw new RuntimeException("prepareEncoder called twice?");
        }
        mPauseTime = 0l;
        mNumTracksAdded = 0;
        mVieoBufferInfo = new MediaCodec.BufferInfo();
        mAudioBufferInfo = new MediaCodec.BufferInfo();
        try {
            String mime = null;
            int codec_num = MediaCodecList.getCodecCount();
            for (int i = 0; i < codec_num; i++) {
                MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
                if (info.isEncoder()) {
                    final String[] mimes = info.getSupportedTypes();
                    for (String m : mimes) {
                        if (VIDEO_MIME.equals(m)) {
                            mime = m;
                        }
                    }
                }
            }
            if (mime == null) {
                throw new UnsupportedOperationException(
                        String.format("Not support MIME: %s", VIDEO_MIME));
            }

            MediaFormat format = MediaFormat.createVideoFormat(
                    VIDEO_MIME, 720, 1280);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BPS);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_IFI);
            mVideoCodec = MediaCodec.createEncoderByType(VIDEO_MIME);
            mVideoCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            format = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_COUNT);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
            format.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, AUDIO_CHANNEL_COUNT);
            mAudioCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
            mAudioCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mMediaMuxer = new MediaMuxer(videoPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mMuxerStarted = false;
        } catch (Exception e) {
            releaseEncoder();
            throw (RuntimeException) e;
        }
    }

    public boolean firstTimeSetup() {
        if (!isRecording() || mInputSurface != null) {
            return false;
        }
        try {
            mInputSurface = new InputSurface(mVideoCodec.createInputSurface());
            mVideoCodec.start();
            mAudioCodec.start();
            mAudioThread = new AudioThread();
            mAudioThread.start();
        } catch (Exception e) {
            releaseEncoder();
            throw (RuntimeException) e;
        }
        return true;
    }

    public boolean isRecording() {
        return mVideoCodec != null;
    }

    public void makeCurrent() {
        mInputSurface.makeCurrent();
    }

    /**
     * 暂停录制时要保存暂停总共时间
     *
     * @param isPause
     */
    public void setPauseState(boolean isPause) {
        if (isPause) {
            mStartPauseTime = System.nanoTime();
            if (mAudioThread != null) {
                mAudioThread.setRecording(false);
            }
            mAudioThread = null;
        } else if (mStartPauseTime != 0l) {
            mPauseTime += (System.nanoTime() - mStartPauseTime);
            mAudioThread = new AudioThread();
            mAudioThread.start();
        }

    }

    synchronized public void swapBuffers() {
        if (!isRecording()) {
            return;
        }
        drainEncoder(false, mVieoBufferInfo, mVideoCodec, mVideoTrackIndex);
        if (!mMuxerStarted) {
       //     drainEncoder(false, mAudioBufferInfo, mAudioCodec, mAudioTrackIndex);
        }
        mInputSurface.swapBuffers();
        mInputSurface.setPresentationTime(System.nanoTime() - mPauseTime);
    }

    synchronized public void stop() {
        if (mAudioThread != null) {
            mAudioThread.setRecording(false);
        }
        drainEncoder(true, mVieoBufferInfo, mVideoCodec, mVideoTrackIndex);
        releaseEncoder();
    }

    //---------------------------------------------------------------------
    // PRIVATE...
    //---------------------------------------------------------------------
    private void releaseEncoder() {
        if (mVideoCodec != null) {
            mVideoCodec.stop();
            mVideoCodec.release();
            mVideoCodec = null;
        }
        if (mAudioCodec != null) {
            mAudioCodec.stop();
            mAudioCodec.release();
            mAudioCodec = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mMediaMuxer != null) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
        }
    }

    private void drainEncoder(boolean endOfStream, MediaCodec.BufferInfo bufferInfo, MediaCodec mediaCodec, int trackIndex) {
        if (endOfStream) {
            mediaCodec.signalEndOfInputStream();
        }
        ByteBuffer[] encoderOutputBuffers = mediaCodec.getOutputBuffers();
        while (true) {
            int encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                if (mediaCodec == mVideoCodec) {
//                    mVideoTrackIndex = mMediaMuxer.addTrack(mediaCodec.getOutputFormat());
//                } else {
//                    mAudioTrackIndex = mMediaMuxer.addTrack(mediaCodec.getOutputFormat());
//                }
//                ++mNumTracksAdded;
//                if (mNumTracksAdded == 2) {
//                    if (mMuxerStarted) {
//                        throw new RuntimeException("muxer start twice");
//                    }
//                    mMediaMuxer.start();
//                    mMuxerStarted = true;
//                }
//                break;

                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mediaCodec.getOutputFormat();
                mVideoTrackIndex = mMediaMuxer.addTrack(newFormat);
                mMediaMuxer.start();
                mMuxerStarted = true;
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    mMediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                }
                mediaCodec.releaseOutputBuffer(encoderStatus, false);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }
    }

    private static final int[] AUDIO_SOURCES = new int[]{
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    /**
     * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
     * and write them to the MediaCodec encoder
     */
    private class AudioThread extends Thread {

        private boolean mIsRecording;

        public AudioThread() {
            mIsRecording = true;
        }

        public void setRecording(boolean isRecording) {
            mIsRecording = isRecording;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            AudioRecord audioRecord = null;
            try {
                final int min_buffer_size = AudioRecord.getMinBufferSize(
                        AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                int buffer_size = AUDIO_SAMPLES_PER_FRAME * AUDIO_FRAMES_PER_BUFFER;
                if (buffer_size < min_buffer_size)
                    buffer_size = ((min_buffer_size / AUDIO_SAMPLES_PER_FRAME) + 1) * AUDIO_SAMPLES_PER_FRAME * 2;

                for (final int source : AUDIO_SOURCES) {
                    try {
                        audioRecord = new AudioRecord(
                                source, AUDIO_SAMPLE_RATE,
                                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
                        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
                            audioRecord = null;
                    } catch (final Exception e) {
                        audioRecord = null;
                    }
                    if (audioRecord != null) break;
                }
                if (audioRecord != null) {
                    Log.i("AudioSoftwarePoller", "SW recording begin");
                    final ByteBuffer buf = ByteBuffer.allocateDirect(AUDIO_SAMPLES_PER_FRAME);
                    int readBytes;
                    audioRecord.startRecording();
                    while (mIsRecording) {
                        buf.clear();
                        readBytes = audioRecord.read(buf, AUDIO_SAMPLES_PER_FRAME);
                        if (readBytes > 0) {
                            // set audio data to encoder
                            buf.position(readBytes);
                            buf.flip();
                            encode(buf, readBytes, getPTSUs());
                        }
                        //drainEncoder(false, mAudioBufferInfo, mAudioCodec, mAudioTrackIndex);
                    }
                } else {
                    Log.e(TAG, "failed to initialize AudioRecord");
                }
            } catch (final Exception e) {
                Log.e(TAG, "AudioThread#run", e);
            } finally {
                if (audioRecord != null) {
                    audioRecord.setRecordPositionUpdateListener(null);
                    audioRecord.release();
                    audioRecord = null;
                    Log.i("AudioSoftwarePoller", "stopped");
                }
            }
        }
    }

    /**
     * Method to set byte array to the MediaCodec encoder
     *
     * @param buffer
     * @param length             　length of byte array, zero means EOS.
     * @param presentationTimeUs
     */
    private void encode(final ByteBuffer buffer, final int length, final long presentationTimeUs) {
        final ByteBuffer[] inputBuffers = mAudioCodec.getInputBuffers();
        while (true) {
            final int inputBufferIndex = mAudioCodec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }
                if (length <= 0) {
                    mAudioCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mAudioCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            presentationTimeUs, 0);
                }
                break;
            } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait for MediaCodec encoder is ready to encode
                // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
                // will wait for maximum TIMEOUT_USEC(10msec) on each call
            }
        }
    }


    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    private long getPTSUs() {
        long result = (System.nanoTime() - mPauseTime) / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < mPrevOutputPTSUs)
            result = (mPrevOutputPTSUs - result) + result;
        return result;
    }
}

