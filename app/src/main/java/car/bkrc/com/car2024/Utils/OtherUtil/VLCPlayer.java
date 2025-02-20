package car.bkrc.com.car2024.Utils.OtherUtil;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;

import car.bkrc.com.car2024.ActivityView.FirstActivity;

/**
 * VLC播放视频工具类
 */
public class VLCPlayer implements MediaPlayer.EventListener{

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;

    public VLCPlayer(Context context) {
        ArrayList<String> options = new ArrayList<>();
        options.add("--no-drop-late-frames"); //防止掉帧
        options.add("--skip-frames"); //防止掉帧
        options.add("--rtsp-tcp");//强制使用TCP方式
        options.add("--avcodec-hw=any"); //尝试使用硬件加速
        options.add("--live-caching=0");//缓冲时长
        options.add("--no-audio");//关闭音频
        options.add("--sout-display");//持续串流输出
        options.add("--network-caching=300");//网络缓冲时长

        libVLC = new LibVLC(context, options);
        mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.setEventListener(this);
    }

    public SurfaceTexture surfaceTexture;
    /**
     * 设置播放视图
     * @param textureView
     */
    public void setVideoSurface(TextureView textureView) {

        surfaceTexture = textureView.getSurfaceTexture();
        mediaPlayer.getVLCVout().setVideoSurface(surfaceTexture);
        mediaPlayer.getVLCVout().setWindowSize(textureView.getWidth(), textureView.getHeight());
        textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // 获取新的宽度和高度
                int newWidth = right - left;
                int newHeight = bottom - top;
                // 设置VLC播放器的宽高参数
                mediaPlayer.getVLCVout().setWindowSize(newWidth, newHeight);
            }
        });

        mediaPlayer.getVLCVout().attachViews();

    }

    /**
     * 设置播放地址
     * @param url
     */
    public void setDataSource(String url) {
        try {
            Media media = new Media(libVLC, Uri.parse(url));
            Log.e("RTSP", "setDataSource: " + url);
            media.setHWDecoderEnabled(false,false);
            mediaPlayer.setMedia(media);
        }catch (Exception e){
            Log.e("VLCPlayer",e.getMessage(),e);
        }
    }

    /**
     * 播放
     */
    public void play() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.play();
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.pause();
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (mediaPlayer == null) {
            return;
        }
        new Thread(() -> {
            try{
                mediaPlayer.stop();
            }catch (Exception ignored){}

        }).start();
    }

    public boolean takeSnapShot(int number, String _path, int width, int height){

        boolean b = mediaPlayer.takeSnapShot(number,_path,width,height);
        MediaScannerConnection.scanFile(FirstActivity.INSTANCE, new String[]{_path}, null, (path, uri) -> Log.e("资源刷新成功路径为", path));
        return b;
    }

    /**
     * 兼容android 10
     * 更新图库
     * @param mContext
     * @param file
     */
    public static void updatePhotoAlbum(Context mContext,String file) {
        // 扫描刷新
        String[] pathArray = new String[]{file};
        String[] typeArray = new String[]{"image/jpeg"};
        MediaScannerConnection.scanFile(mContext, pathArray, typeArray, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                Log.d("ImageUtils", "onScanCompleted  s->" + s);
            }
        });
    }

    public static String getMimeType(File file) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(file.getName());
        return type;
    }



    /**
     * 释放资源
     */
    public void release() {
        if(mediaPlayer!=null) {
            mediaPlayer.release();
            surfaceTexture = null;
        }
        if(libVLC!=null) {
            libVLC.release();

        }
    }


    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Buffering:
                // 处理缓冲事件
                if (callback != null) {
                    callback.onBuffering(event.getBuffering());
                }
                break;
            case MediaPlayer.Event.EndReached:
                // 处理播放结束事件
                if (callback != null) {
                    callback.onEndReached();
                }
                break;
            case MediaPlayer.Event.EncounteredError:
                // 处理播放错误事件
                if (callback != null) {
                    callback.onError();
                }
                break;
            case MediaPlayer.Event.TimeChanged:
                // 处理播放进度变化事件
                if (callback != null) {
                    callback.onTimeChanged(event.getTimeChanged());
                }
                break;
            case MediaPlayer.Event.PositionChanged:
                // 处理播放位置变化事件
                if (callback != null) {
                    callback.onPositionChanged(event.getPositionChanged());
                }
                break;
            case MediaPlayer.Event.Vout:

                break;
        }
    }


    private VLCPlayerCallback callback;

    public void setCallback(VLCPlayerCallback callback) {
        this.callback = callback;
    }

    public interface VLCPlayerCallback {
        void requestPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

        void onBuffering(float bufferPercent);
        void onEndReached();
        void onError();
        void onTimeChanged(long currentTime);
        void onPositionChanged(float position);

        boolean onTouch(View v, MotionEvent event);
    }

}