package cloud.veezee.android.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import cloud.veezee.android.Constants
import com.google.gson.Gson
import cloud.veezee.android.R
import cloud.veezee.android.adapters.AlbumVerticalListAdapter
import cloud.veezee.android.api.API
import cloud.veezee.android.api.album
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.application.GlideApp
import cloud.veezee.android.models.Album
import cloud.veezee.android.utils.AudioPlayer
import cloud.veezee.android.utils.PlayListFactory
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.activity_album_content.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.json.JSONObject

class AlbumActivity : BaseActivity() {

    private var context: Context = this;

    private var artwork: ImageView? = null;
    var list: RecyclerView? = null
    var title: TextView? = null;
    var artist: TextView? = null;
    var playButton: Button? = null;

    private var trackAdapter: AlbumVerticalListAdapter? = null
    private var album: Album? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        setSupportActionBar(album_toolbar);
        navigationIcon(album_toolbar);

        myAppbarLayout = album_app_bar_layout;
        root = album_root;

        val albumJson = intent.extras.getString("album");

        album = Gson().fromJson(albumJson, Album::class.java);

        requestPlayer();
        prepareComponents();
        initializeList();
    }

    private fun prepareComponents() {
        list = album_list;
        artwork = album_artwork;
        artist = album_artist;
        title = album_title;
        playButton = album_play_button;
        artwork?.clipToOutline = true;
        title?.text = album?.title;
        artist?.text = album?.artist?.name;

        GlideApp.with(context).load(album?.image).into(artwork!!);
    }

    private fun initializeList() {
        if(album?.tracks == null || album?.tracks!!.count() <= 0) {
            API.Get.album(this, album?.id ?: "", object : HttpRequestListeners.StringResponseListener {
                override fun response(response: String?) {
                    album = Gson().fromJson(response, object : TypeToken<Album>() {}.type);

                    prepareComponents();
                    initializeList();
                }

                override fun error(error: JSONObject?) {

                }
            });

            return;
        }

        trackAdapter = AlbumVerticalListAdapter(album!!, context);
        list?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        list?.adapter = trackAdapter;

        OverScrollDecoratorHelper.setUpOverScroll(list, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    fun play(view: View) {
        val playListFactory = PlayListFactory(context);
        controller?.start(playListFactory.album(album), 0);

        val i = Intent(AudioPlayer.ACTION_CHANGE_BOTTOM_PLAYER_STATE);
        i.putExtra("open", true);
        sendBroadcast(i);
    }

    fun shuffle(view: View) {
        val playListFactory = PlayListFactory(context);
        controller?.start(playListFactory.shuffle(playListFactory.album(album)), 0);

        val i = Intent(AudioPlayer.ACTION_CHANGE_BOTTOM_PLAYER_STATE);
        i.putExtra("open", true);
        sendBroadcast(i);
    }
}

