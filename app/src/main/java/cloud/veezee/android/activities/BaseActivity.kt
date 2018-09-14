package cloud.veezee.android.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import cloud.veezee.android.R
import cloud.veezee.android.application.App
import kotlinx.android.synthetic.main.activity_home_page_content.*
import kotlinx.android.synthetic.main.content_player.*
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.animation.AnimationUtils
import android.widget.*
import cloud.veezee.android.Constants
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import cloud.veezee.android.api.API
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.application.GlideApp
import cloud.veezee.android.fragments.PlayListOptionFragment
import cloud.veezee.android.google.GoogleSignInHelper
import cloud.veezee.android.google.interfaces.GoogleSignOutListener
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.api.validateLogin
import cloud.veezee.android.models.Color
import cloud.veezee.android.models.SettingModel
import cloud.veezee.android.services.AudioService
import cloud.veezee.android.utils.*
import org.json.JSONObject

open class BaseActivity : AppCompatActivity() {

    companion object {
        const val FLAG_PLAY = 1;
        const val FLAG_PAUSE = 0;
    }

    private val context: Context = this;
    protected var controller: AudioPlayer? = null
    private var slidingLayout: SlidingUpPanelLayout? = null;
    private var bottomPlayerLayout: RelativeLayout? = null;
    private var controllerContainer: ConstraintLayout? = null;
    private var bottomPlayerPlay: ImageView? = null;
    private var bottomPlayerPause: ImageView? = null;
    private var pause: ImageView? = null;
    private var play: ImageView? = null;
    private var shuffle: ImageView? = null;
    private var repeat: ImageView? = null;
    private var arrowDown: ImageView? = null;
    private var playerVolume: SeekBar? = null;
    private var close: ImageView? = null;
    private var playerArtWork: ImageView? = null;
    private var bottomPlayerArtwork: ImageView? = null;
    private var blurBackground: ImageView? = null;
    private var playerSeek: SeekBar? = null;
    private var bufferLoading: ProgressBar? = null;
    private var timerRight: TextView? = null;
    private var timerLeft: TextView? = null;
    private var playerTitle: TextView? = null;
    private var playerArtist: TextView? = null;
    private var bottomPlayerArtist: TextView? = null;
    private var bottomPlayerTitle: TextView? = null;
    var myAppbarLayout: AppBarLayout? = null;
    lateinit var root: ConstraintLayout;


    private var google: GoogleSignInHelper? = null;
    private var prevPosition: Long = -1;
    private var isActivityVisible: Boolean = true;
    private var audioManager: AudioManager? = null
    private var dragging = false;
    private var TAG = "BaseActivity";

    private var closeBottomPlayerReceiver: BroadcastReceiver = CloseBottomPlayerReceiver();
    private var addToPlayListReceiver: BroadcastReceiver = AddToPlayListReceiver();
    private var settingChangedReceiver: BroadcastReceiver = SettingChangedReceiver();
    private var metaDataReceiver: BroadcastReceiver = AudioReceiver();

    private val glideSimpleTarget = object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            bottomPlayerArtwork?.setImageBitmap(resource);
            playerArtWork?.setImageBitmap(resource);

            if (App.setting!!.coloredPlayer) {
                val task = SetBlurredArtWork();
                task.execute(resource);
            }
        }
    }


    private val validateLoginResponseListener = object : HttpRequestListeners.JsonObjectResponseListener {
        override fun response(response: JSONObject) {

            App.autoLoginSessionExpireDate = now();
        }

        override fun headers(json: JSONObject) {

        }

        override fun error(er: JSONObject) {
            val statusCode = er[AppClient.STATUS_CODE];
            if (statusCode == 500 || statusCode == 410) {
                val logoutRequire = Intent(context, LogoutRequireActivity::class.java);
                startActivity(logoutRequire);
            }
        }
    }


//    private val audioPlayBack = object {
//        override fun bufferLength(duration: Long) {
//            val max: Int = duration.toInt();
//
//            playerProgress?.max = max;
//            playerSeek?.max = max;
//            timerRight?.text = formatTime(max);
//        }
//
//        override fun currentBufferPosition(currentBufferPosition: Long) {
//        }
//
//        override fun currentPosition(currentPosition: Long) {
//
////            if (Math.abs(currentPosition - prevPosition) > 5000) {
////                val animation = ObjectAnimator.ofInt(playerProgress, "progress", currentPosition.toInt());
////                animation.duration = 500; // 0.5 second
////                animation.interpolator = DecelerateInterpolator();
////                animation.start();
////            }
//
//            if (!dragging) {
////                playerProgress?.progress = currentPosition.toInt();
//                playerSeek?.progress = currentPosition.toInt();
//                prevPosition = currentPosition;
//            }
//        }
//    }
//
//    private val audioPlayerEventListener = object {
//
//        override fun onPlayerStateChanged(isPlay: Boolean, playerState: Int) {
//
//            if (playerState == Player.STATE_BUFFERING || playerState == Player.STATE_IDLE) {
//                bufferLoading?.visibility = View.VISIBLE;
//                playerProgress?.isIndeterminate = true;
//
//            } else if (playerState == Player.STATE_READY) {
//                playerProgress?.isIndeterminate = false;
//                bufferLoading?.visibility = View.GONE;
//            }
//
//            if (isPlay) {
//                changePlayButtonState(BaseActivity.FLAG_PAUSE);
//            } else {
//                changePlayButtonState(BaseActivity.FLAG_PLAY);
//            }
//        }
//
//        override fun onTracksChanged() {
//            setMetaData();
//        }
//    }

    private val onPlayerSeekChangeListener = object : SeekBar.OnSeekBarChangeListener {
        var currentPosition: Int = 0

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            currentPosition = progress;
            timerLeft?.text = formatTime(currentPosition);
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            dragging = true;
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            controller?.seekTo(currentPosition);

            object : CountDownTimer(100, 100) {
                override fun onFinish() {
                    dragging = false;
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start();
        }
    }

    private val panelSlidingListener = object : SlidingUpPanelLayout.PanelSlideListener {

        private var canShowStatusBar = false;

        override fun onPanelSlide(panel: View?, slideOffset: Float) {

            playerArtWork?.visibility = View.VISIBLE;
            playerArtWork?.alpha = slideOffset;

            if (slideOffset >= 0)
                if (myAppbarLayout != null) {
                    myAppbarLayout!!.y = -slideOffset * myAppbarLayout!!.height;
                }

            if (slideOffset == 1f) {
                panelCompletelyExpanded();
                canShowStatusBar = true;
            } else {
                if (canShowStatusBar)
                    StatusBarHelper.showStatusBar(window);
            }
        }

        override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
            //when panel is going to expand
            if (previousState == SlidingUpPanelLayout.PanelState.COLLAPSED && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                playerVolume?.progress = getCurrentVolume();
            } else if (previousState == SlidingUpPanelLayout.PanelState.EXPANDED && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                bottomPlayerLayout?.visibility = View.VISIBLE;
            } else if (previousState == SlidingUpPanelLayout.PanelState.DRAGGING && newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                bottomPlayerLayout?.visibility = View.GONE;
            }
        }
    };

    private val onVolumeSeekChangeListener = object : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    private val bottomPlayerTouch = object : View.OnTouchListener {
        var x = 0f;
        var y = 0f;

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX;
                    y = event.rawY;
                }
                MotionEvent.ACTION_UP -> {
                    val x2 = event.rawX;
                    val y2 = event.rawY;

                    if (y2 - y > 50 && x - x2 > 50) {
                        controller?.destroyPlayer();
                        closeBottomPlayer();
                    }
                }
            }

            return true;
        }
    }

    //methods
    protected fun requestPlayer() {
        prepareComponents();
        initializeProps();
        callBacks();
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun prepareComponents() {
        bottomPlayerLayout = bottom_player_container;
        controllerContainer = controller_container;
        slidingLayout = sliding_layout;
        play = controller_play;
        bottomPlayerPlay = bottom_player_play;
        pause = controller_pause;
        bottomPlayerPause = bottom_player_pause;
        playerArtWork = player_artwork;
        blurBackground = player_blur_background;
        bottomPlayerArtwork = bottom_player_artwork
        playerVolume = player_volume_seek;
        close = bottom_player_close;
        timerRight = player_timer_right;
        timerLeft = player_timer_left;
        bottomPlayerArtist = bottom_player_artist;
        bottomPlayerTitle = bottom_player_title;
        playerArtist = player_artist;
        playerTitle = player_title
        bufferLoading = player_buffer_loading;
        playerSeek = player_seek;
        shuffle = controller_shuffle;
        repeat = controller_repeat;
        arrowDown = player_arrow_down;
    }

    private fun initializeProps() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager;
        playerVolume?.max = getMaxVolume();

        if (controller == null)
            controller = AudioPlayer.getInstance().init(context);
        if (controller?.index != -1 && controller?.playableItem != null) {
            setMetaData();
            openBottomPlayer();
        } else {
            closeBottomPlayer();
        }

        playerArtWork?.clipToOutline = true;
        bottomPlayerArtwork?.clipToOutline = true;

//        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                AudioManager.ADJUST_LOWER or AudioManager.ADJUST_RAISE,
//                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE or AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private fun callBacks() {
        playerVolume?.setOnSeekBarChangeListener(onVolumeSeekChangeListener);
        playerSeek?.setOnSeekBarChangeListener(onPlayerSeekChangeListener);
        slidingLayout?.addPanelSlideListener(panelSlidingListener);
        bottomPlayerLayout?.setOnTouchListener(bottomPlayerTouch);

        bottomPlayerLayout?.setOnClickListener {
            expandPanel();
        };

        close?.setOnClickListener {
            controller?.destroyPlayer();
            //closeBottomPlayer();
        }

        panel_child.setOnClickListener(null);
    };

    fun audioPlay(view: View) {
        controller?.play();
    }

    fun audioPause(view: View) {
        controller?.pause();
    }

    fun audioPrev(view: View) {
        controller?.prev();
    }

    fun audioNext(view: View) {
        controller?.next();
    }

    fun audioShuffle(view: View) {
        controller?.shuffle();

        var color = 0;

        val shuffleXRotation = shuffle?.rotationX;
        if (shuffleXRotation == 0f) {
            color = android.graphics.Color.parseColor(controller?.playableItem?.colors?.primaryColor);
            shuffle?.animate()?.rotationX(180f)?.start();
        } else {
            color = android.graphics.Color.parseColor(controller?.playableItem?.colors?.accentColor);
            shuffle?.animate()?.rotationX(0f)?.start(); }

        DrawableCompat.setTint(shuffle?.drawable!!, color);
    }

    fun audioRepeat(view: View) {
        controller?.repeat();
    }

    private fun panelCompletelyExpanded() {
        StatusBarHelper.hideStatusBar(window);

        val anim = AnimationUtils.loadAnimation(context, R.anim.player_arrow_drop_down_animation);
        arrowDown?.startAnimation(anim);
        arrowDown?.animate()?.alpha(0f)?.setStartDelay(400)?.start();
    }

    fun expandPanel() {
        slidingLayout?.panelState = SlidingUpPanelLayout.PanelState.EXPANDED;
    }

    fun collapsePanel() {
        slidingLayout?.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
    }

    fun hiddenBottomPlayer() {
        slidingLayout?.panelState = SlidingUpPanelLayout.PanelState.HIDDEN;
    }

    fun openBottomPlayer() {
        if (slidingLayout?.panelState == SlidingUpPanelLayout.PanelState.HIDDEN) {
            collapsePanel();
        }
    }

    fun closeBottomPlayer() {
        val timer = object : CountDownTimer(350, 350) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                if (slidingLayout?.panelState != SlidingUpPanelLayout.PanelState.HIDDEN) {
                    hiddenBottomPlayer();
                }
            }
        }
        timer.start();
    }

    protected fun panelState(): SlidingUpPanelLayout.PanelState? = slidingLayout?.panelState;

    protected fun changePlayButtonState(mode: Int) {

        if (mode == BaseActivity.Companion.FLAG_PAUSE) {
            play?.visibility = View.INVISIBLE;
            bottomPlayerPlay?.visibility = View.INVISIBLE;

            pause?.visibility = View.VISIBLE;
            bottomPlayerPause?.visibility = View.VISIBLE
        } else {
            pause?.visibility = View.INVISIBLE;
            bottomPlayerPause?.visibility = View.INVISIBLE;

            play?.visibility = View.VISIBLE;
            bottomPlayerPlay?.visibility = View.VISIBLE;
        }

    }

    private fun formatTime(i: Int): String {
        val seconds = i / 1000 % 60
        val minutes = i / (1000 * 60) % 60
        var format = "%d:%d"
        if (seconds < 10 && minutes >= 10)
            format = "%d:0%d"
        else if (seconds >= 10 && minutes < 10)
            format = "0%d:%d"
        else if (seconds < 10 && minutes < 10)
            format = "0%d:0%d"
        else
            format = "%d:%d"
        return String.format(format, minutes, seconds)
    }

    private fun getMaxVolume(): Int = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)!!;

    private fun getCurrentVolume(): Int = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!;

    @SuppressLint("RestrictedApi")
    fun navigationIcon(toolbar: Toolbar) {
        setSupportActionBar(toolbar);
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(false);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener {
            finish();
        }
    }

    private fun applyTheme(theme: Setting.Theme): Int {
        return when (theme) {
            Setting.Theme.PURPLE_DARK -> R.style.PurpleDark
            Setting.Theme.BLACK -> R.style.Black;
            Setting.Theme.WHITE -> {
                //StatusBarHelper.setLightStatusBar(window.decorView);
                R.style.White
            };
        }
    }

    private fun resetPlayerStatus() {
        playerSeek?.max = 1;
        playerSeek?.progress = 0;
        timerRight?.text = "00:00";
        timerLeft?.text = "00:00";

        resetUIForColoredPlayer();
    }

    private fun setMetaData() {
        resetPlayerStatus();

        val playableItem = controller?.playableItem;

        val imageUrl = playableItem?.imageUrl;
        val title = playableItem?.title;
        val artist = playableItem?.artist?.name;

        bottomPlayerArtwork?.setImageResource(R.drawable.placeholder);
        playerArtWork?.setImageResource(R.drawable.placeholder);

        if (App.setting!!.coloredPlayer)
            updateUIForColoredPlayer(playableItem?.colors);

        GlideApp.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(glideSimpleTarget);

        playerTitle?.text = title
        bottomPlayerTitle?.text = title;
        playerArtist?.text = artist
        bottomPlayerArtist?.text = artist;
    }

    private fun resetUIForColoredPlayer() {
        val colors: ArrayList<Int> = extractThemeColors(context);
        val accent = ContextCompat.getColor(context, R.color.accent);
        val primaryText = colors[0];
        val secondaryText = colors[1];

        playerTitle?.setTextColor(primaryText);
        playerArtist?.setTextColor(secondaryText);
        timerLeft?.setTextColor(primaryText);
        timerRight?.setTextColor(primaryText);
        bottomPlayerArtist?.setTextColor(primaryText);
        bottomPlayerTitle?.setTextColor(primaryText);
        timerRight?.setTextColor(primaryText);

        DrawableCompat.setTint(play?.drawable!!, primaryText);
        DrawableCompat.setTint(pause?.drawable!!, primaryText);
        DrawableCompat.setTint(controller_next?.drawable!!, primaryText);
        DrawableCompat.setTint(controller_prev?.drawable!!, primaryText);
        DrawableCompat.setTint(repeat?.drawable!!, primaryText);
        DrawableCompat.setTint(shuffle?.drawable!!, primaryText);
        DrawableCompat.setTint(player_max_volume?.drawable!!, primaryText);
        DrawableCompat.setTint(player_min_volume?.drawable!!, primaryText);
        DrawableCompat.setTint(arrowDown?.drawable!!, primaryText);

        playerSeek?.thumb?.setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        playerSeek?.refreshDrawableState();
        playerVolume?.thumb?.setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        playerVolume?.refreshDrawableState();
        bufferLoading?.refreshDrawableState();
    }

    private fun updateUIForColoredPlayer(colors: Color?) {

        val accentColor = android.graphics.Color.parseColor(colors?.accentColor);
        val primaryColor = android.graphics.Color.parseColor(colors?.primaryColor);

        playerTitle?.setTextColor(primaryColor);
        bottomPlayerArtist?.setTextColor(accentColor);
        bottomPlayerTitle?.setTextColor(primaryColor);
        playerArtist?.setTextColor(accentColor);
        timerLeft?.setTextColor(accentColor);
        timerRight?.setTextColor(accentColor);

        DrawableCompat.setTint(play?.drawable!!, accentColor);
        DrawableCompat.setTint(pause?.drawable!!, accentColor);
        DrawableCompat.setTint(controller_next?.drawable!!, accentColor);
        DrawableCompat.setTint(controller_prev?.drawable!!, accentColor);
        DrawableCompat.setTint(repeat?.drawable!!, accentColor);
        DrawableCompat.setTint(shuffle?.drawable!!, accentColor);
        DrawableCompat.setTint(player_max_volume?.drawable!!, accentColor);
        DrawableCompat.setTint(player_min_volume?.drawable!!, accentColor);
        DrawableCompat.setTint(arrowDown?.drawable!!, accentColor);

        playerSeek?.progressDrawable?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        playerSeek?.thumb?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        playerVolume?.progressDrawable?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        playerVolume?.thumb?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        bufferLoading?.indeterminateDrawable?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        title = "";

        val setting = App.setting;
        setTheme(applyTheme(setting?.theme!!));

        LocalBroadcastManager.getInstance(context).registerReceiver(closeBottomPlayerReceiver, IntentFilter(AudioPlayer.ACTION_CLOSE_BOTTOM_PLAYER));
        LocalBroadcastManager.getInstance(context).registerReceiver(settingChangedReceiver, IntentFilter(Setting.SETTING_NOTIFICATION));
        LocalBroadcastManager.getInstance(context).registerReceiver(metaDataReceiver, IntentFilter(AudioPlayer.ACTION_META_DATA));

        if (now() > App.autoLoginSessionExpireDate && !Constants.GUEST_MODE)
            checkUserLogin();
    }

    private fun checkUserLogin() {

        val user = UserManager.get(context);

        if (user.isLoggedIn) {
            if (!App.offlineMode) {
                API.Account.validateLogin(context, userToken(context), validateLoginResponseListener);
            }
        } else {
            logout();
        }
    }

    private fun logout() {
        google = GoogleSignInHelper(context);
        google?.signOut(object : GoogleSignOutListener {
            override fun onCompleted() {
                UserManager.remove(context);
                return;
            }
        });
        UserManager.remove(context);
    }

    override fun onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(context).unregisterReceiver(closeBottomPlayerReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(settingChangedReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(metaDataReceiver);
    }

    override fun onPause() {
        super.onPause();
        isActivityVisible = false;

        unregisterReceiver(addToPlayListReceiver);
    }

    override fun onResume() {
        super.onResume();
        isActivityVisible = true;

        registerReceiver(addToPlayListReceiver, IntentFilter("TrackMenu"));

        if (panelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            StatusBarHelper.hideStatusBar(window);
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (panelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            playerVolume?.progress = getCurrentVolume();
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    collapsePanel();
                    return true;
                }
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    playerVolume?.progress = getCurrentVolume() + 1;

                    return true;
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    playerVolume?.progress = getCurrentVolume() - 1;

                    return true;
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    inner class CloseBottomPlayerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            closeBottomPlayer();
        }
    }

    inner class AddToPlayListReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bottomSheetDialogFragment = PlayListOptionFragment();

            val bundle = Bundle();
            bundle.putString("trackBundle", intent?.extras?.getString("track"))
            bottomSheetDialogFragment.arguments = bundle;
            bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag);
        }
    }

    inner class SettingChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.flags) {
                SettingModel.THEME_CHANGED -> {
                    recreate();
                }
                SettingModel.COLORED_PLAYER_CHANGED -> {
                    if (!isActivityVisible) {
                        object : CountDownTimer(1000, 1000) {
                            override fun onFinish() {
                                recreate();
                            }

                            override fun onTick(millisUntilFinished: Long) {

                            }
                        }.start();
                    }
                }
            }
        }
    }

    inner class AudioReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.flags) {
                AudioService.FLAG_NEW_TRACK -> {
                    setMetaData();
                }
                AudioService.FLAG_POSITION -> {
                    val bundle: Bundle = intent.getBundleExtra(AudioPlayer.DATA);

                    val duration = bundle.getLong("duration");
                    val position = bundle.getLong("currentPosition");
                    val bufferPosstion = bundle.getLong("currentBufferPosition");

                    val max: Int = duration.toInt();
                    playerSeek?.max = max;
                    timerRight?.text = formatTime(max);

                    if (!dragging) {
                        //playerProgress?.progress = position.toInt();
                        playerSeek?.progress = position.toInt();
                        prevPosition = position;
                    }
                }
                AudioService.FLAG_PLAYER_STATE -> {
                    val bundle: Bundle = intent.getBundleExtra(AudioPlayer.DATA);

                    val playerState = bundle.getInt("playerState");
                    val isPlay = bundle.getBoolean("isPlay");

                    if (playerState == Player.STATE_BUFFERING || playerState == Player.STATE_IDLE) {
                        bufferLoading?.visibility = View.VISIBLE;
                    } else if (playerState == Player.STATE_READY) {
                        bufferLoading?.visibility = View.GONE;
                    }
                    if (isPlay) {
                        changePlayButtonState(BaseActivity.FLAG_PAUSE);
                    } else {
                        changePlayButtonState(BaseActivity.FLAG_PLAY);
                    }
                }
            }
        }
    }

    inner class SetBlurredArtWork : AsyncTask<Bitmap, Void, Drawable>() {
        override fun doInBackground(vararg params: Bitmap?): Drawable? {
            var drawable: Drawable? = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(params[0], applicationContext, 9);
            } catch (e: Exception) {
                e.printStackTrace();
            }

            return drawable;
        }

        override fun onPostExecute(result: Drawable?) {
            super.onPostExecute(result)

            if (result != null) {
                blurBackground?.setImageDrawable(result);
            }
        }
    }
}