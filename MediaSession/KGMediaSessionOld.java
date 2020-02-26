package com.MediaSession;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.Rating;
import android.media.RemoteControlClient;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by masonxu on 2017/7/24.
 * this is for below API 21, miui
 */

public class KGMediaSessionOld extends KGMediaSessionBase {

    private static final String name = "KGMediaSessionOld";

    private RemoteControlClient mRemoteControlClient;

    protected KGMediaSessionOld(Context context, String tag, ComponentName mediaButtonEventReceiver, PendingIntent mbrIntent) {
        super(context, tag, mediaButtonEventReceiver, mbrIntent);
        createNewRCCAndSetFlags();
    }

    private void createNewRCCAndSetFlags() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.d(name, "createNewRCCAndSetFlags: createNewRCCAndSetFlags");
            mRemoteControlClient = new RemoteControlClient(mediabuttonPI);
            // 设置所支持的系统操作，下面的例子中支持了上一首，下一首，播放，暂停，播放/暂停
            int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                    | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                    | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                    | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                    | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE;
            if (Build.VERSION.SDK_INT >= 19) {
                flags |= RemoteControlClient.FLAG_KEY_MEDIA_RATING;
            }
            mRemoteControlClient.setTransportControlFlags(flags);
            try {
                AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.registerMediaButtonEventReceiver(mComponentName);
                if (checkMediaSession())
                    mAudioManager.registerRemoteControlClient(mRemoteControlClient);
                initRCCPlayListener();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initRCCPlayListener() {
        //初始化时，如果酷狗锁屏打开，就不初始化mRemoteControlClient
        if (Build.VERSION.SDK_INT >= 18/* && LSUtil.isMiuiLockScreenCanUse()*/) {
             Log.d(name, "registerRemoteControlClient");
            mRemoteControlClient.setOnGetPlaybackPositionListener(new RemoteControlClient.OnGetPlaybackPositionListener() {
                @Override
                public long onGetPlaybackPosition() {
                    //这里返回媒体信息的播放进度，一般按ms算
                    //long curPos = PlaybackServiceUtil.getCurrentPosition();
                    return 0;
                }
            });

            mRemoteControlClient.setPlaybackPositionUpdateListener(new RemoteControlClient.OnPlaybackPositionUpdateListener() {

                @Override
                public void onPlaybackPositionUpdate(long newPositionMs) {
                    //自行编写播放器seek到新位置的时间
                    //PlaybackServiceUtil.seek((int) newPositionMs);
                }
            });

            if (Build.VERSION.SDK_INT >= 19) {
                mRemoteControlClient.setMetadataUpdateListener(new RemoteControlClient.OnMetadataUpdateListener() {

                    @Override
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    public void onMetadataUpdate(int key, Object newValue) {
                        try {
                            if (key == MediaMetadataEditor.RATING_KEY_BY_USER && newValue instanceof Rating) {
                                Rating rating = (Rating) newValue;

                                //SystemLockScreenHandler systemLockScreenHandler = SystemLockScreenHandler.getInstance();
                                //if (systemLockScreenHandler != null) {
                                //    systemLockScreenHandler.handleRating(rating.isRated());
                                //}
                                //媒体信息更新
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void release() {
        if (Build.VERSION.SDK_INT >= 14 && checkMediaSession()) {
            clearMetadata();
            AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            Class cls = mAudioManager.getClass();
            try {
                mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);

                String methodName = "unregisterRemoteControlClient";
                @SuppressWarnings("unchecked")
                Method method = cls.getDeclaredMethod(methodName, RemoteControlClient.class);
                method.invoke(mAudioManager, mRemoteControlClient);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            mRemoteControlClient = null;
        }
        //super.release();
    }

    @Override
    protected boolean checkMediaSession() {
        return mRemoteControlClient != null;
    }

    private boolean checkMediaSessionReInit() {
        if (!LSUtil.isMiuiLockScreenCanUse()
                && !((AudioManager) KGCommonApplication.getContext().getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
             Log.d(name, "checkMediaSessionReInit: return false");
            return false;
        }
        if (!checkMediaSession() || dealWithMiui) {
            //createNewRCCAndSetFlags();
            //initRCCPlayListener();
            if (dealWithMiui) {
                dealWithMiui = false;
                 Log.d(name, "checkMediaSessionReInit: dealWithMiui = false");
            }
        }
        return checkMediaSession();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void setPlaybackState(int state, long position) {
        if (!checkMediaSessionReInit()) {
            return;
        }
         Log.d(name, "setPlaybackState: state = " + state);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mRemoteControlClient.setPlaybackState(state, position, 1);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mRemoteControlClient.setPlaybackState(state);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void setMetadata(HashMap<Integer, Object> metadata) {
        if (!checkMediaSessionReInit()) {
            return;
        }
         Log.d(name, "setMetadata");
        if (metadata != null && metadata.size() > 0) {
            RemoteControlClient.MetadataEditor ed = mRemoteControlClient.editMetadata(true);
            for (Map.Entry<Integer, Object> entry : metadata.entrySet()) {
                if (entry.getKey() == KGMediaSessionBase.TITLE)
                    ed.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, (String) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.ALBUM)
                    ed.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, (String) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.ARTIST)
                    ed.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, (String) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.ALBUM_ARTIST)
                    ed.putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, (String) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.ALBUM_ART) {
                    //if (android.os.Build.VERSION.SDK_INT < 24) {
                        Bitmap bitmap = (Bitmap) entry.getValue();
                        if (bitmap != null && !bitmap.isRecycled()) {
                            ed.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap);
                        }
                    //}
                } else if (entry.getKey() == KGMediaSessionBase.DURATION)
                    ed.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, (long) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.LYRIC)
                    try {
                        ed.putString(MIUI_Lyric, (String) entry.getValue());
                    } catch (IllegalArgumentException es) {
                        es.printStackTrace();
                    }

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                handleLikeBtn(ed);
            }

            ed.apply();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void clearMetadata() {
         Log.d(name, "clearMetadata");
        if (checkMediaSession()) {
            RemoteControlClient.MetadataEditor metaData = mRemoteControlClient.editMetadata(true);
            metaData.clear();
            metaData.apply();
        }
    }

    //miui专用
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleLikeBtn(MediaMetadataEditor ed) {
        //SystemLockScreenHandler lockScreenHandler = SystemLockScreenHandler.getInstance();
        //if (lockScreenHandler != null) {
        //    boolean isAddToFav = lockScreenHandler.isAddToFav();
        //    ed.putObject(MediaMetadataEditor.RATING_KEY_BY_USER, Rating.newHeartRating(isAddToFav));
        //}
    }


    private final int MIUI_Lyric = 1000;

    private boolean dealWithMiui = false;

    //如果mRemoteControlClient已经创建了，那就重新注册一个空的
    public void dealWithMiui() {
        if (checkMediaSession() && !dealWithMiui) {
             Log.d(name, "dealWithMiui");
            //release();
            //createNewRCCAndSetFlags();
             Log.d(name, "dealWithMiui: setPlaybackState: state = " + RemoteControlClient.PLAYSTATE_PAUSED);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED, 0, 1);
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
            }
            dealWithMiui = true;
        }
    }
}
