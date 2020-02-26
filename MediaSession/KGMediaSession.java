package com.MediaSession;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by masonxu on 2017/7/24.
 */

public class KGMediaSession extends KGMediaSessionBase {

    private static final String name = "KGMediaSession";

    private MediaSessionCompat mediasession;

    private MediaSessionCompat.Callback mediaSessionCallback = null;

    //由于非线程安全，这里要把所有的事件都放到主线程中处理，使用这个handler保证都处于主线程
    private Handler mainHandler = null;

    //小米，华为用到的自定义歌词
    private final String METADATA_KEY_LYRIC_HUAWEI = "android.media.metadata.LYRIC";

    protected KGMediaSession(Context context, String tag, ComponentName mediaButtonEventReceiver, PendingIntent mbrIntent) {
        super(context, tag, mediaButtonEventReceiver, mbrIntent);
        mainHandler = new Handler();
        mediaSessionCallback = new MediaSessionCallback();

        mediasession = new MediaSessionCompat(context, tag, mediaButtonEventReceiver, mbrIntent);
        mediasession.setCallback(mediaSessionCallback, mainHandler);
        //mediasession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediasession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mediasession.setActive(true);
    }

    @Override
    protected void release() {
        mediasession.setActive(false);
        mediasession.release();
        mediaSessionCallback = null;
        mainHandler = null;
        super.release();
    }

    @Override
    protected boolean checkMediaSession() {
        return mediasession != null;
    }

    @Override
    protected void setPlaybackState(final int state, final long position) {
        Log.d(name, "setPlaybackState: state = " + state + ", position = " + position);
        if (checkMediaSession()) {
            if (((AudioManager) KGCommonApplication.getContext().getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
                setFlags(); //蓝牙播放设置FLAG_HANDLES_TRANSPORT_CONTROLS
            } else {
                setFlagsNC();
            }
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
            float speed = PlaybackServiceUtil.getCurrentPlaySpeed();
            stateBuilder.setState(state, position, speed > 0f ? speed : 1.0f, SystemClock.elapsedRealtime());
            mediasession.setPlaybackState(stateBuilder.build());
        }
    }
    @Override
    public void setMetadata(HashMap<Integer, Object> metadata) {
        Log.d(name, "setMetadata");
        Bitmap bitmap = null;
        boolean isFullAvator = false;
        if (metadata != null && metadata.size() > 0) {
            final MediaMetadataCompat.Builder metaDataBuilder = new MediaMetadataCompat.Builder();
            for (Map.Entry<Integer, Object> entry : metadata.entrySet()) {
                if (entry.getKey() == KGMediaSessionBase.TITLE)
                    metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, (String) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.ALBUM)
                    metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, (String) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.ARTIST)
                    metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, (String) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.ALBUM_ARTIST)
                    metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, (String) entry.getValue());
                else if (entry.getKey() == KGMediaSessionBase.ALBUM_ART) {
                    bitmap = (Bitmap) entry.getValue();
                } else if (entry.getKey() == KGMediaSessionBase.DURATION){
                    metaDataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (long) entry.getValue());
                }else if (entry.getKey() == KGMediaSessionBase.ALBUM_AVATOR){
                    if((int)entry.getValue() == 1){
                        isFullAvator = true;
                    }
                }
            }
            if (bitmap != null && !bitmap.isRecycled() && isFullAvator) {
                // 设置播放封面
                metaDataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap);
                metaDataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
            } else {
                return;
            }

            if (checkMediaSession())
                mediasession.setMetadata(metaDataBuilder.build());
        }
    }

    @Override
    public void clearMetadata() {
        Log.d(name, "clearMetadata");
        if (checkMediaSession()) {
            mediasession.setMetadata(new MediaMetadataCompat.Builder().build());
            mediasession.setPlaybackState(new PlaybackStateCompat.Builder().build());
        }
    }

    @Override
    @Nullable
    public MediaSessionCompat.Token getSessionToken() {
        return mediasession.getSessionToken();
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            KeyEvent mEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.d(name, "mediaButtonEvent: getAction = " + mEvent.getAction() + ", getKeyCode = " + mEvent.getKeyCode());
            try {
                if (mediabuttonPI != null) {
                    Log.d(name, "mediaButtonEvent: send");
                    mediabuttonPI.send(mContext, 0, mediaButtonEvent);
                }
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public void onSetRating(RatingCompat rating) {
             Log.d(name, "onSetRating rating.isRated() = " + rating.isRated());
        }

        @Override
        public void onSeekTo(long pos) {
             Log.d(name, "onSeekTo: ");
        }

        @Override
        public void onPlay() {
             Log.d(name, "onPlay: ");
            sendFakeMediaButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }

        @Override
        public void onPause() {
             Log.d(name, "onPause: ");
            sendFakeMediaButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }

        @Override
        public void onSkipToNext() {
             Log.d(name, "onSkipToNext: ");
            sendFakeMediaButton(KeyEvent.KEYCODE_MEDIA_NEXT);
        }

        @Override
        public void onSkipToPrevious() {
             Log.d(name, "onSkipToPrevious: ");
            sendFakeMediaButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        }
    }

    private void sendFakeMediaButton(int keyCode) {
        try {
            if (mediabuttonPI != null) {
                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                //模拟一次mediabutton事件，为了统一给线控耳机作处理
                mediaButtonIntent.putExtra(AbstractMediaButtonIntentReceiver.FromMediaSession, true);
                mediabuttonPI.send(mContext, 0, mediaButtonIntent);
            }
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public void setFlags() {
        if (checkMediaSession()) {
             Log.d(name, "setFlags: ");
            if (mediasession.getController().getFlags() != (MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)) {
                 Log.d(name, "setFlags: 3");
                mediasession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            }
        }
    }

    public void setFlagsNC() {
        if (checkMediaSession()) {
             Log.d(name, "setFlagsNC: ");
            if (mediasession.getController().getFlags() != MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS) {
                 Log.d(name, "setFlagsNC: 1");
                mediasession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
            }
        }
    }
}
