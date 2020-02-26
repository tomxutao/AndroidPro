package com.MediaSession;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.HashMap;

/**
 * Created by masonxu on 2017/7/24.
 */

public class KGMediaSessionBase {

    protected Context mContext;

    protected ComponentName mComponentName = null;

    protected PendingIntent mediabuttonPI = null;

    protected KGMediaSessionBase(Context context, String tag, ComponentName mediaButtonEventReceiver, PendingIntent mbrIntent) {
        this.mContext = context;
        this.mComponentName = mediaButtonEventReceiver;
        this.mediabuttonPI = mbrIntent;
    }

    protected void release() {
        mComponentName = null;
        mediabuttonPI = null;
    }

    protected boolean checkMediaSession() {
        return true;
    }

    protected void setPlaybackState(final int state, final long position) {

    }

    public void setMetadata(HashMap<Integer, Object> metadata) {

    }

    public void clearMetadata() {

    }

    public MediaSessionCompat.Token getSessionToken() {
        return null;
    }

    public static int TITLE = 0;
    public static int ALBUM = 1;
    public static int ARTIST = 2;
    public static int ALBUM_ARTIST = 3;
    public static int DURATION = 4;
    public static int ALBUM_ART = 5;
    public static int LYRIC = 6;
    public static int ALBUM_AVATOR = 7;
}


