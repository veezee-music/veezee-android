package cloud.veezee.android.services

import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color.*
import android.media.AudioManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.util.Patterns
import cloud.veezee.android.Constants
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import cloud.veezee.android.models.PlayableItem
import cloud.veezee.android.utils.AudioPlayer
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import cloud.veezee.android.activities.HomePageActivity
import cloud.veezee.android.application.App
import cloud.veezee.android.utils.VeezeeCache
import cloud.veezee.android.utils.Couchbase
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.ArrayList
import cloud.veezee.android.R
import cloud.veezee.android.application.GlideApp

class AudioService : Service() {

    companion object {
        const val MAX_CACHE_SIZE: Long = 1024 * 1024 * 10;
        const val CHANNEL_ID = "AudioPlayer_01";

        const val FLAG_PLAYER_STATE: Int = 10;
        const val FLAG_POSITION: Int = 11;
        const val FLAG_NEW_TRACK: Int = 20;
    }

    //props
    private var TAG = "AudioService";
    private val context = this;
    private var resultReceiver: ResultReceiver? = null;
    private var audioPlayerControlsBroadcastReceiver: BroadcastReceiver? = null;
    private var audioBecomingNoisyBroadcastReceiver: BroadcastReceiver? = null;
    private var player: SimpleExoPlayer? = null;
    private var indexBackup = -1;
    private var playList: ArrayList<PlayableItem>? = null;
    private var notificationId = 50;
    private val handler = Handler();
    private val delayMs: Long = 1000;
    private var prevState = PlaybackStateCompat.STATE_PAUSED;
    private var artwork: Bitmap? = null;
    private var albumArtwork: Bitmap? = null;
    private var placeHolder: Bitmap? = null;

    // notifications
    private var notificationCompat: NotificationCompat.Builder? = null;
    private var session: MediaSessionCompat? = null;
    private var state: PlaybackStateCompat.Builder? = null;
    private var notificationManager: NotificationManager? = null;

    private var ongoingCall: Boolean = false;
    private var phoneStateListener: PhoneStateListener? = null;
    private var telephonyManager: TelephonyManager? = null;

    // callBacks

    private val simpleGlideAlbumArtworkTarget = object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            albumArtwork = resource;
        }
    }

    private val simpleGlideArtworkTarget = object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            artwork = resource;
            startForeground(notificationId, generateNotification());
        }
    }

    private val exoTransferListener = object : TransferListener<DataSource> {

        override fun onTransferStart(source: DataSource?, dataSpec: DataSpec?) {

        }

        override fun onBytesTransferred(source: DataSource?, bytesTransferred: Int) {
        }

        override fun onTransferEnd(source: DataSource?) {
            val playableItem = playList?.get(player!!.currentWindowIndex);
            val url = playableItem?.fileName;

            val proxy = VeezeeCache(context).proxy;
            if (proxy!!.isCached(playableItem?.id) && Patterns.WEB_URL.matcher(url).matches() && Constants.OFFLINE_ACCESS) {

                val audioCacheLocation = File(Constants.DIRECTORY, "cache/audio/${playableItem?.id}").toString();
                val imageCacheLocation = File(Constants.DIRECTORY, "cache/images/${playableItem?.id}");
                val albumImageCacheLocation = File(Constants.DIRECTORY, "cache/images/${playableItem?.album?.id}");

                try {
                    val os = FileOutputStream(imageCacheLocation);
                    val os2 = FileOutputStream(albumImageCacheLocation);
                    artwork?.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    albumArtwork?.compress(Bitmap.CompressFormat.JPEG, 100, os2);
                    os.flush();
                    os2.flush();
                    os.close();
                    os2.close();

                    playableItem?.imageUrl = imageCacheLocation.toString();
                    playableItem?.album?.image = albumImageCacheLocation.toString();
                } catch (e: Exception) {
                    e.printStackTrace();
                    Log.i(TAG, e.message);
                }

                playableItem?.fileName = audioCacheLocation;

                val nestedJson = Gson().toJson(playableItem);
                val result: LinkedTreeMap<String, Any> = Gson().fromJson(nestedJson, LinkedTreeMap<String, Any>()::class.java);

                Couchbase.getInstance(context)?.save(result, playableItem?.id!!);
            }
        }
    }

    private val mSessionCallBack = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay();
            startPlayer();
        }

        override fun onPause() {
            super.onPause();
            pausePlayer();
        }

        override fun onStop() {
            super.onStop();
            stopPlayer();
        }

        override fun onSkipToNext() {
            super.onSkipToNext();
            skipToNext();
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            skipToPrev();
        }
    }

    // handle incoming calls
    private fun createPhoneStateListener() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state:Int, incomingNumber:String) {
                when (state) {
                //if at least one call exists or the phone is ringing
                //pause the MediaPlayer
                    TelephonyManager.CALL_STATE_OFFHOOK,
                    TelephonyManager.CALL_STATE_RINGING -> {
                        pausePlayer();
                        ongoingCall = true;
                    };
                    TelephonyManager.CALL_STATE_IDLE -> {
                        // Phone idle. Start playing.
                        if (ongoingCall)
                        {
                            ongoingCall = false;
                            //startPlayer();
                            pausePlayer();
                        }
                    };
                }
            }
        }
    }

    private val exoPlayerListener = object : Player.EventListener {

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

        }

        override fun onSeekProcessed() {

        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

        }

        override fun onRepeatModeChanged(repeatMode: Int) {

        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            // send duration position buffer position when track change and player ready
            object : CountDownTimer(500, 500) {
                override fun onFinish() {
                    updateProgressBar();
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }.start();
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

            sendResult(playerStateBundleFactory(), FLAG_PLAYER_STATE);
            updateProgressBar();

            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                if (playWhenReady && prevState == PlaybackStateCompat.STATE_PAUSED) {
                    prevState = PlaybackStateCompat.STATE_PLAYING;
                    startForeground(notificationId, generateNotification());
                } else if (!playWhenReady && prevState == PlaybackStateCompat.STATE_PLAYING) {
                    prevState = PlaybackStateCompat.STATE_PAUSED;
                    startForeground(notificationId, generateNotification());
                    //if (Build.VERSION.SDK_INT < 25)
                        stopForeground(false);
                }
            } else if (playbackState == Player.STATE_ENDED)
                stopPlayer();
        }

        override fun onLoadingChanged(isLoading: Boolean) {

        }

        override fun onPositionDiscontinuity(reason: Int) {
            // send audio meta data and update notification when track changed
            if (player?.currentWindowIndex != indexBackup) {
                indexBackup = player?.currentWindowIndex!!;

                updateMetaData();
            }
        }

    }

    private val updateProgressAction = object : Runnable {
        override fun run() {
            handler.postDelayed(this, delayMs);

            //send player position and buffer position on loop
            sendResult(currentPositionBundleFactory(), FLAG_POSITION);
        }
    }

    private fun registerBroadcastReceivers() {
        val audioPlayerControllerIntentFilter = IntentFilter(AudioPlayer.ACTION_PLAYER_CONTROLLER);
        LocalBroadcastManager.getInstance(context).registerReceiver(audioPlayerControlsBroadcastReceiver!!, audioPlayerControllerIntentFilter);

        val audioBecomingNoisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(audioBecomingNoisyBroadcastReceiver!!, audioBecomingNoisyIntentFilter);
    }

    private fun unregisterBroadcastReceivers() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(audioPlayerControlsBroadcastReceiver!!);
        unregisterReceiver(audioBecomingNoisyBroadcastReceiver!!);
    }

    private fun registerPhoneStateListener() {
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private fun unregisterPhoneStateListener() {
        if(phoneStateListener != null) {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private fun currentPositionBundleFactory(): Bundle {
        val b = Bundle();

        val duration = player?.duration!!;

        b.putLong("duration", duration);
        b.putLong("currentPosition", player?.currentPosition!!);
        b.putLong("currentBufferPosition", player?.bufferedPosition!!);

        return b;
    }

    private fun currentTrackMetaDataBundleFactory(): Bundle {

        val index = player!!.currentWindowIndex;

        val b = Bundle();
        b.putString("playableItem", Gson().toJson(playList!![index]));
        b.putInt("index", index);

        return b;
    }

    private fun playerStateBundleFactory(): Bundle {
        val b = Bundle();
        b.putBoolean("isPlay", player?.playWhenReady!!);
        b.putInt("playerState", player?.playbackState!!);

        return b;
    }

    private fun sendResult(bundle: Bundle, resultCode: Int) {
        resultReceiver?.send(resultCode, bundle);
    }

    private fun releasePlayer() {
        player?.stop();
        player?.release();
    }

    private fun startPlayer() {
        player?.playWhenReady = true;
    }

    private fun pausePlayer() {
        player?.playWhenReady = false;
    }

    private fun stopPlayer() {

        val bundle = Bundle();
        bundle.putBoolean("isPlay", false);
        bundle.putInt("playerState", Player.STATE_ENDED);
        sendResult(bundle, FLAG_PLAYER_STATE);

        stopSelf();
    }

    private fun skipToNext() {
        if (playList?.size!! > 1) {
            val index = player!!.currentWindowIndex;
            if (index + 1 < playList?.size!!)
                newTrack(player!!.nextWindowIndex);
            else
                newTrack(0);
        }
    }

    private fun skipToPrev() {
        if (playList?.size!! > 1) {
            val index = player!!.currentWindowIndex;
            if (index - 1 >= 0)
                newTrack(player!!.previousWindowIndex);
            else
                newTrack(playList?.size!! - 1);
        }
    }

    private fun shuffle() {
        player?.shuffleModeEnabled = !player!!.shuffleModeEnabled;
    }

    private fun repeat() {
        player?.repeatMode = if(player?.repeatMode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE;
    }

    private fun seekTo(position: Int) {
        player?.seekTo(position.toLong());
    }

    //all is ready just play song
    private fun newTrack(playerCurrentWindowIndex: Int) {
        try {
            if (playerCurrentWindowIndex != this.indexBackup) {
                player?.seekTo(playerCurrentWindowIndex, 0);
                startPlayer();
            } else {
                // pause/play player when current item taped
                if(player?.repeatMode != 1)
                    player?.playWhenReady = !player?.playWhenReady!!
            }
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    private fun updateProgressBar() {
        handler.removeCallbacks(updateProgressAction);

        val playerState = player?.playbackState;
        val isPlaying = player?.playWhenReady;

        if (playerState == Player.STATE_READY && isPlaying!!)
            handler.post(updateProgressAction)
    }

    private fun updateMetaData() {
        val index = player!!.currentWindowIndex;
        if (index < playList?.size!!) {
            artwork = placeHolder;
            GlideApp.with(context).asBitmap().load(playList!![index].imageUrl).into(simpleGlideArtworkTarget);
            GlideApp.with(context).asBitmap().load(playList!![index].album?.image).into(simpleGlideAlbumArtworkTarget);

            sendResult(currentTrackMetaDataBundleFactory(), FLAG_NEW_TRACK);
            startForeground(notificationId, generateNotification());

        } else {
            stopPlayer();
        }
    }

    private fun resourceExtractor(jsonPlayList: String?): ArrayList<String> {
        val playList: ArrayList<PlayableItem> = Gson().fromJson(jsonPlayList, object : TypeToken<ArrayList<PlayableItem>>() {}.type);
        this.playList = playList;
        val resources: ArrayList<String> = ArrayList();

        (0 until playList.size).mapTo(resources) { playList[it].fileName };

        return resources;
    }


    private fun getHttpDataSourceFactory(userAgent: String): DefaultHttpDataSourceFactory {
        return DefaultHttpDataSourceFactory(
                userAgent, exoTransferListener,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
    }

    private fun getDefaultDataSourceFactory(userAgent: String): DefaultDataSourceFactory {
        return DefaultDataSourceFactory(
                context,
                userAgent, exoTransferListener);
    }

    private fun getCacheDataSourceFactory(dataSourceFactory: DefaultHttpDataSourceFactory): CacheDataSourceFactory {
        val cache = SimpleCache(File(Constants.DIRECTORY, " cache"), LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE));
        val cacheFlags = CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR or CacheDataSource.FLAG_BLOCK_ON_CACHE;

        return CacheDataSourceFactory(cache, dataSourceFactory, cacheFlags, MAX_CACHE_SIZE);
    }

    private fun initMediaSource(resources: ArrayList<String>): MediaSource {
        val mHandler = Handler();
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name));

        val defaultDataSourceFactory = getDefaultDataSourceFactory(userAgent);

        val mediaSourcesToLoad = ArrayList<MediaSource>()
        for (i in 0 until resources.size) {

            val resource = resources[i];
            var dataSource: DataSource.Factory? = null;
            dataSource = defaultDataSourceFactory;
            mediaSourcesToLoad.add(ExtractorMediaSource.Factory(dataSource).createMediaSource(Uri.parse(resource), mHandler, null));
            //mediaSourcesToLoad.add(ExtractorMediaSource(Uri.parse(resource), dataSource, DefaultExtractorsFactory(), mHandler, null));
        }

        val dynamicConcatenatingMediaSource = DynamicConcatenatingMediaSource();
        dynamicConcatenatingMediaSource.addMediaSources(0, mediaSourcesToLoad);

        return dynamicConcatenatingMediaSource
    }

    private fun initializePlayer(sources: MediaSource?, playerWindow: Int) {
        if (player != null)
            releasePlayer();

        val trackSelector = DefaultTrackSelector();
        //val loadControl = DefaultLoadControl();
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        player?.prepare(sources);
        player?.addListener(exoPlayerListener);

        newTrack(if (playerWindow < playList?.size!!) playerWindow else 0);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun notificationChannel(): NotificationChannel {
        val name = context.getString(R.string.notificationChannelName);
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);

        return channel;
    }

    private fun generateNotification(): Notification? {
        if (artwork != null && artwork?.isRecycled!!)
            artwork = null; //unfortunately bitmap recycled, show the placeholder

        notificationCompat = NotificationCompat.Builder(context, CHANNEL_ID);

        val playPauseAction: android.support.v4.app.NotificationCompat.Action = if (!player?.playWhenReady!!) {
            android.support.v4.app.NotificationCompat.Action(
                    R.drawable.ic_play_white_36dp, "play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                            PlaybackStateCompat.ACTION_PLAY));
        } else {
            android.support.v4.app.NotificationCompat.Action(
                    R.drawable.ic_pause_white_36dp, "pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                            PlaybackStateCompat.ACTION_PAUSE));
        };

        val skipToNext = NotificationCompat.Action(
                R.drawable.ic_fast_forward_white_24dp, "next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        val skipToPrev = NotificationCompat.Action(
                R.drawable.ic_rewind_white_24dp, "prev",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        val closeAction = NotificationCompat.Action(
                R.drawable.ic_close_white_24dp, "close",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP));

        val playableItem = playList?.get(player!!.currentWindowIndex);

        val homePageActivityIntent = Intent(context, HomePageActivity::class.java);
        val homePageActivityPendingIntent = PendingIntent.getActivity(context, 123, homePageActivityIntent, 0);

        notificationCompat?.setSmallIcon(R.drawable.ic_logo)
                ?.setLargeIcon(artwork ?: placeHolder)
                ?.setContentTitle(playableItem?.title ?: "unknown")
                ?.setContentText(playableItem?.artist?.name ?: "unknown")
                ?.setContentIntent(homePageActivityPendingIntent)
                ?.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                ?.setShowWhen(false)
                ?.addAction(skipToPrev)
                ?.addAction(playPauseAction)
                ?.setPriority(Notification.PRIORITY_MAX)
                ?.addAction(skipToNext);

        if (!player?.playWhenReady!!) {
            notificationCompat?.addAction(closeAction);
        }

        notificationCompat
                ?.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                ?.setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(session?.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2, 3))

        if (Build.VERSION.SDK_INT in 24..25 && playableItem?.colors != null) {
            notificationCompat?.color = parseColor(playableItem.colors?.accentColor);
        }

        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager?.createNotificationChannel(notificationChannel());
            notificationCompat?.setColorized(true);
        };

        return notificationCompat?.build();
    }

    private fun mediaSession(): MediaSessionCompat {
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)

        val mSession = MediaSessionCompat(this, "mSession", mediaButtonReceiver, null)
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        mSession.setMediaButtonReceiver(pendingIntent)

        state = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY);

        mSession.setPlaybackState(state?.build());
        mSession.setCallback(mSessionCallBack);
        mSession.isActive = true;

        return mSession;
    }

    //override methods
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onCreate() {
        super.onCreate();

        audioPlayerControlsBroadcastReceiver = AudioPlayerControlsBroadcastReceiver();
        audioBecomingNoisyBroadcastReceiver = AudioBecomingNoisyBroadcastReceiver();
        registerBroadcastReceivers();
        createPhoneStateListener();
        registerPhoneStateListener();

        notificationId = System.currentTimeMillis().toInt();
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;

        session = mediaSession();
    }

    override fun onDestroy() {
        super.onDestroy();

        stopForeground(true);
        notificationManager?.cancel(notificationId);
        unregisterBroadcastReceivers();
        unregisterPhoneStateListener();
        handler.removeCallbacks(updateProgressAction);
        releasePlayer();
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (placeHolder == null) {
            Thread(Runnable {
                placeHolder = BitmapFactory.decodeResource(resources, R.drawable.placeholder);
            }).start()
        }

        if (resultReceiver == null)
            resultReceiver = intent?.getParcelableExtra(AudioPlayer.ACTION_PLAYER_CONTROLLER);

        MediaButtonReceiver.handleIntent(session, intent);

        val jsonPlayList = intent?.getStringExtra("playList");
        val requestWindow = intent?.getIntExtra("index", -1);

        if (jsonPlayList != null && requestWindow != -1) {
            this.indexBackup = -1;
            initializePlayer(initMediaSource(resourceExtractor(jsonPlayList)), requestWindow!!);

            prevState = PlaybackState.STATE_PAUSED;
        }

        startForeground(notificationId, generateNotification());

        return START_NOT_STICKY;
    }

    inner class AudioPlayerControlsBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.flags) {
                AudioPlayer.FLAG_PLAY -> startPlayer();
                AudioPlayer.FLAG_PAUSE -> pausePlayer();
                AudioPlayer.FLAG_NEXT -> skipToNext();
                AudioPlayer.FLAG_PREV -> skipToPrev();
                AudioPlayer.FLAG_SHUFFLE -> shuffle();
                AudioPlayer.FLAG_REPEAT -> repeat();
                AudioPlayer.FLAG_NEW_TRACK -> {
                    newTrack(intent.getIntExtra(AudioPlayer.DATA, 0));
                };
                AudioPlayer.FLAG_SEEK_TO -> {
                    val position = intent.getIntExtra(AudioPlayer.DATA, 0);
                    seekTo(position);
                };
            }
        }
    }

    inner class AudioBecomingNoisyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            pausePlayer();
        }
    }
}