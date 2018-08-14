package cloud.veezee.android.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import cloud.veezee.android.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cloud.veezee.android.adapters.MoreTracksVerticalListAdapter
import cloud.veezee.android.api.API
import cloud.veezee.android.api.albums
import cloud.veezee.android.api.playlists
import cloud.veezee.android.api.tracks
import cloud.veezee.android.utils.contentReadyToShow
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.adapters.interfaces.OnLoadMoreListener
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.HomePageItem
import cloud.veezee.android.models.Track
import kotlinx.android.synthetic.main.activity_albums_track_list.*
import kotlinx.android.synthetic.main.activity_albums_track_list_content.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.json.JSONObject
import java.lang.reflect.Type
import java.net.URLEncoder

class AlbumsTrackListActivity : BaseActivity() {

    private val context: Context = this;
    private val TAG: String = "AlbumsTrackListActivity";

    private val GRID_SYSTEM_SPAN_COUNT: Int = 2;
    private var lastId: String? = "";
    private val itemList: ArrayList<Any> = ArrayList();
    private var type: String = "";

    private var albumTrackRecycler: RecyclerView? = null;
    private var loading: ProgressBar? = null;
    private var listAdapter: MoreTracksVerticalListAdapter? = null;

    private val loadMoreListener = object : OnLoadMoreListener {
        override fun onLoadMore() {
            if (lastId != null) {
                requestTarget();
            }
        }
    }

    private val volleyRequestListeners = object : HttpRequestListeners.StringResponseListener {

        override fun response(response: String?) {


            if (type == "Playlist" || type == "Album") {
                itemList.addAll(Gson().fromJson(response, object : TypeToken<ArrayList<Album>>() {}.type));
                lastId = (itemList[itemList.size - 1] as Album).id;
            }
            else {
                itemList.addAll(Gson().fromJson(response, object : TypeToken<ArrayList<Track>>() {}.type));
                lastId = (itemList[itemList.size - 1] as Track).id;
            }


            listAdapter?.allowLoading = true;
            listAdapter?.notifyDataSetChanged();

            (albumTrackRecycler as View).contentReadyToShow(true, loading);
        }

        override fun headers(json: JSONObject) {

        }

        override fun error(error: JSONObject?) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_track_list);
        setSupportActionBar(toolbar);
        navigationIcon(toolbar);
        myAppbarLayout = albums_track_app_bar_layout

        type = intent.extras.getString("type");

        val title = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase() + "s";
        albums_track_title.text = title;

        requestPlayer();
        components();
        initList();

        requestTarget();
    }

    /**
     *
     * witch kind of list needed
     * track/album/playlist
     *
     */
    private fun requestTarget() {
        when (type) {
            HomePageItem.ALBUM -> {
                API.Lists.albums(context, lastId!!, volleyRequestListeners);
            }
            HomePageItem.TRACK -> {
                API.Lists.tracks(context, lastId!!, volleyRequestListeners);
            }
            else -> {
                API.Lists.playlists(context, lastId!!, volleyRequestListeners);
            };
        }
    }

//    private fun createAlbumListFrom(trackJson: String): ArrayList<Album> {
//
//        val temp: ArrayList<Track> = Gson().fromJson(Gson().toJson(trackJson), object : TypeToken<ArrayList<Track>>() {}.type);
//        val albumList: ArrayList<Album> = ArrayList();
//
//        for (track in temp) {
//
//            if (track.album == null)
//                continue;
//
//            if (albumList.size == 0 || albumList.last().id != track.album?.id) {
//                val copyAlbumJson = Gson().toJson(track.album!!);
//                track.album = null;
//                val album: Album = Gson().fromJson(copyAlbumJson, Album::class.java);
//                album.tracks?.add(track);
//
//                albumList.add(album);
//            } else {
//                track.album = null;
//                albumList.last().tracks?.add(track);
//            }
//        }
//
//        return albumList;
//    }

    private fun components() {
        albumTrackRecycler = albums_track_recycler;
        loading = albums_track_loading;
    }

    private fun initList() {
        albumTrackRecycler?.layoutManager = GridLayoutManager(context, GRID_SYSTEM_SPAN_COUNT, GridLayoutManager.VERTICAL, false);
        listAdapter = MoreTracksVerticalListAdapter(context, itemList, albumTrackRecycler);
        listAdapter?.setOnLoadMoreListener = loadMoreListener;
        albumTrackRecycler?.adapter = listAdapter;
        OverScrollDecoratorHelper.setUpOverScroll(albumTrackRecycler, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }
}
