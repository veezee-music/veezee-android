package cloud.veezee.android.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.*
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import cloud.veezee.android.BuildConfig
import cloud.veezee.android.Constants
import cloud.veezee.android.application.App
import cloud.veezee.android.models.PlayableItem
import cloud.veezee.android.services.AudioService
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AudioPlayer {

    companion object {
        const val DATA = "data";

        const val FLAG_PLAY = 0;
        const val FLAG_PAUSE = 1;
        const val FLAG_SEEK_TO = 3;
        const val FLAG_NEW_TRACK = 4;
        const val FLAG_NEXT = 6;
        const val FLAG_PREV = 7;
        const val FLAG_SHUFFLE = 8;
        const val FLAG_REPEAT = 9;

        const val ACTION_PLAYER_CONTROLLER = "playerController";
        const val ACTION_CHANGE_BOTTOM_PLAYER_STATE = "ACTION_CHANGE_BOTTOM_PLAYER_STATE";
        const val ACTION_META_DATA = "MetaDataReceiver";

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: AudioPlayer? = null;

        fun getInstance(): AudioPlayer {
            if (INSTANCE == null)
                INSTANCE = AudioPlayer();

            return INSTANCE!!;
        }

        private var originalSystemLockScreenWallpaper: Drawable? = null;

        fun setLockScreenWallpaper(resource: Bitmap) {
            return;
            if(!Constants.COLORED_PLAYER) {
                return;
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                doAsync {
                    val wallpaperManager = WallpaperManager.getInstance(App.instance);
                    if(wallpaperManager.isSetWallpaperAllowed) {
                        if(originalSystemLockScreenWallpaper == null) {
                            // requires external storage permission
                            originalSystemLockScreenWallpaper = wallpaperManager.drawable;
                        }
                        uiThread {
                            wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_LOCK);
                        }
                    }
                }
            }
        }

        fun resetLockScreenWallpaper() {
            return;
            if(!Constants.COLORED_PLAYER) {
                return;
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                doAsync {
                    uiThread {
                        val wallpaperManager = WallpaperManager.getInstance(App.instance);
                        if(wallpaperManager.isSetWallpaperAllowed && originalSystemLockScreenWallpaper != null) {
                            setLockScreenWallpaper(BitmapUtils.drawableToBitmap(originalSystemLockScreenWallpaper));
                        }
                    }
                }
            }
        }
    }

    private lateinit var context: Context;
    private var code: Int = 0;

    private var myPlayableItem: PlayableItem? = null;
    private var myIndex: Int? = -1;

    val playableItem: PlayableItem?
        get() = myPlayableItem;

    val index: Int?
        get() = myIndex;

    var currentPlayListCode: Int = 0
        get() = code;

    fun init(context: Context): AudioPlayer {
        this.context = context;

        return this;
    }

    fun start(playList: ArrayList<PlayableItem>? = null, index: Int, playListCode: Int = 0) {
        if (playList != null) {
            this.code = playListCode;
            val serializePlayList = Gson().toJson(playList);
            val i = Intent(context, AudioService::class.java);
            i.putExtra("playList", serializePlayList);
            i.putExtra("index", index);
            if (Build.VERSION.SDK_INT < 26) {
                startService(i);
            } else {
                startForegroundService(i);
            }
        } else {
            newTrack(index);
        }
    }

    fun destroyPlayer() {
        releaseController();

        val audioServiceIntent = Intent(context, AudioService::class.java);
        context.stopService(audioServiceIntent);

        resetLockScreenWallpaper();
    }

    fun releaseController() {
        this.code = 0;
        myPlayableItem = null;
        myIndex = -1;

        val i = Intent(ACTION_CHANGE_BOTTOM_PLAYER_STATE);
        i.putExtra("open", false);
        context.sendBroadcast(i);
    }

    fun play() {
        sendBroadCast(getIntent(FLAG_PLAY, ACTION_PLAYER_CONTROLLER));
    }

    fun pause() {
        sendBroadCast(getIntent(FLAG_PAUSE, ACTION_PLAYER_CONTROLLER));
        resetLockScreenWallpaper();
    }

    fun next() {
        sendBroadCast(getIntent(FLAG_NEXT, ACTION_PLAYER_CONTROLLER));
    }

    fun prev() {
        sendBroadCast(getIntent(FLAG_PREV, ACTION_PLAYER_CONTROLLER));
    }

    fun shuffle() {
        sendBroadCast(getIntent(FLAG_SHUFFLE, ACTION_PLAYER_CONTROLLER));
    }

    fun repeat() {
        sendBroadCast(getIntent(FLAG_REPEAT, ACTION_PLAYER_CONTROLLER));
    }

    fun seekTo(position: Int) {
        val i = getIntent(FLAG_SEEK_TO, ACTION_PLAYER_CONTROLLER);
        i.putExtra(DATA, position)
        sendBroadCast(i);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun startForegroundService(playerService: Intent) {
        playerService.putExtra(ACTION_PLAYER_CONTROLLER, Receiver(Handler()));
        context.startForegroundService(playerService);
    }

    private fun startService(playerService: Intent) {
        playerService.putExtra(ACTION_PLAYER_CONTROLLER, Receiver(Handler()));
        context.startService(playerService);
    }

    private fun getIntent(flag: Int, action: String): Intent {
        val i = Intent(action);
        i.flags = flag;
        return i;
    }

    private fun sendBroadCast(i: Intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    private fun newTrack(index: Int) {
        val i = getIntent(FLAG_NEW_TRACK, ACTION_PLAYER_CONTROLLER);
        i.putExtra(DATA, index);
        sendBroadCast(i);
    }

    private fun toPlayableItem(json: String?): PlayableItem? = if (json != null) Gson().fromJson(json, PlayableItem::class.java) else null;

    inner class Receiver(handler: Handler?) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                AudioService.FLAG_PLAYER_STATE -> {
                    val playerState = resultData?.getInt("playerState", 0);

                    if (playerState == 4)
                        releaseController();

                    val i = getIntent(resultCode, ACTION_META_DATA);
                    i.putExtra(DATA, resultData);
                    sendBroadCast(i);
                }
                AudioService.FLAG_NEW_TRACK -> {
                    myPlayableItem = toPlayableItem(resultData?.getString("playableItem", null));
                    myIndex = resultData?.getInt("index", -1);

                    val i = getIntent(resultCode, ACTION_META_DATA);
                    sendBroadCast(i);
                }
                AudioService.FLAG_POSITION -> {
                    val i = getIntent(resultCode, ACTION_META_DATA);
                    i.putExtra(DATA, resultData);
                    sendBroadCast(i);
                }
            }
        }
    }
}