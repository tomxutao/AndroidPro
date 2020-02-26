package com.MediaSession;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.kugou.common.business.miui.LSUtil;
import com.kugou.common.utils.KGLog;
import com.kugou.framework.service.MediaButtonIntentReceiver;

import java.util.HashMap;

/**
 * Created by masonxu on 2017/7/17.
 */

public class KGMediaSessionManager {

    private KGMediaSessionBase kgmediasession;

    public KGMediaSessionManager() {
    }

    private static class MediaSessionHolder {
        private static KGMediaSessionManager mediaSession = new KGMediaSessionManager();
    }

    public static KGMediaSessionManager getInstance() {
        return MediaSessionHolder.mediaSession;
    }

    public void createMediaSession(Context context, String tag) {
        PackageManager PackageManager = context.getPackageManager();
        ComponentName componentMB = new ComponentName(context, MediaButtonIntentReceiver.class.getName()); //mediabutton的接收器
        PackageManager.setComponentEnabledSetting(componentMB, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(componentMB);
        PendingIntent mediabuttonPi = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, 0);

        if (Build.VERSION.SDK_INT >= 21 && !LSUtil.isMiuiSupport()) {
            try {
                kgmediasession = new KGMediaSession(context, tag, componentMB, mediabuttonPi);
            }catch (Exception es) {
                es.printStackTrace();
                kgmediasession = null;
            }
            if (kgmediasession == null) {
                kgmediasession = new KGMediaSessionOld(context, tag, componentMB, mediabuttonPi);
            }
        } else if (Build.VERSION.SDK_INT >= 14) {
            kgmediasession = new KGMediaSessionOld(context, tag, componentMB, mediabuttonPi);
        }
    }

    public void release() {
        if (checkKGMediaSession()) {
            kgmediasession.release();
            kgmediasession = null;
        }
    }

    public void setPlaybackState(final int state, final long position) {
        if (checkKGMediaSession())
            kgmediasession.setPlaybackState(state, position);
    }

    public void setMetadata(HashMap<Integer, Object> metadata) {
        if (checkKGMediaSession())
            kgmediasession.setMetadata(metadata);
    }

    private boolean checkKGMediaSession() {
        return kgmediasession != null;
    }

    public void setFlags() {
        if (checkKGMediaSession() && kgmediasession instanceof KGMediaSession)
            ((KGMediaSession) kgmediasession).setFlags();
    }

    public void setFlagsNC() {
        if (checkKGMediaSession() && kgmediasession instanceof KGMediaSession)
            ((KGMediaSession) kgmediasession).setFlagsNC();
    }

    public void dealWithMiuiKGMediaSessionOld() {
         Log.d("KGMediaSessionManager", "dealWithMiuiKGMediaSessionOld");
        if (checkKGMediaSession() && kgmediasession instanceof KGMediaSessionOld) {
            ((KGMediaSessionOld) kgmediasession).dealWithMiui();
        }
    }

    @Nullable
    public MediaSessionCompat.Token getSessionToken() {
        if (checkKGMediaSession()) {
            return kgmediasession.getSessionToken();
        }
        return null;
    }
}
