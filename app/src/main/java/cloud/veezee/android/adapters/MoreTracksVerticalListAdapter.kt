package cloud.veezee.android.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cloud.veezee.android.adapters.interfaces.OnLoadMoreListener
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.Track
import cloud.veezee.android.R
import cloud.veezee.android.activities.AlbumActivity
import cloud.veezee.android.activities.AlbumsTrackListActivity
import cloud.veezee.android.activities.BaseActivity
import cloud.veezee.android.activities.HomePageActivity
import cloud.veezee.android.application.GlideApp
import cloud.veezee.android.utils.AudioPlayer
import cloud.veezee.android.utils.PlayListFactory
import kotlin.collections.ArrayList

class MoreTracksVerticalListAdapter(val context: Context, private val list: ArrayList<Any>, recyclerView: RecyclerView?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = "MoreTracksAdapter"

    val inflater: LayoutInflater = LayoutInflater.from(context);
    private var loadMoreListener: OnLoadMoreListener? = null;
    private var loading: Boolean = true;
    private var playListCode = 0;

    var setOnLoadMoreListener: OnLoadMoreListener? = null
        set(value) = setOnLoadMoreListener(value!!);

    var allowLoading: Boolean
        get() = loading
        set(value) = setLoading(value);

    init {

        val layoutManager = recyclerView?.layoutManager;

        if (layoutManager is GridLayoutManager) {

            var totalItemCount = 0;
            var lastVisibleItem = 0;

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = layoutManager.itemCount;
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                    if (loading && totalItemCount <= lastVisibleItem + 5) {
                        if (loadMoreListener != null) {
                            loadMoreListener?.onLoadMore();
                        }

                        loading = false;
                    }
                }
            });
        }
    }

    private fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        loadMoreListener = listener;
    }

    private fun setLoading(value: Boolean) {
        playListCode = System.currentTimeMillis().toInt();
        loading = value;
    }

//    private var firstIndex: Int = 0;
//    fun notifyDataSetChange() {
//
//        val lastIndex: Int = list.lastIndex;
//
//        if (list[lastIndex] is Track) {
//            createAlbumListFrom(firstIndex, lastIndex);
//            firstIndex = lastIndex + 1;
//        }
//
//        notifyDataSetChanged();
//    }

//    private fun createAlbumListFrom(firstOfTheList: Int, endOfTheList: Int) {
//
//        val sublist: MutableList<Any> = list.subList(firstOfTheList, endOfTheList);
//        val temp: ArrayList<Track> = Gson().fromJson(Gson().toJson(sublist), object : TypeToken<ArrayList<Track>>() {}.type);
//        sublist.clear();
//
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
//        list.addAll(albumList);
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = inflater.inflate(R.layout.item_more_tracks, parent, false);

        return MainItemViewHolder(view);
    }

    override fun getItemCount(): Int = list.size;

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val newPosition = holder.adapterPosition;
        val item = list[newPosition];

        if (holder is MainItemViewHolder) {
            var artwork: ImageView? = null;
            var imageUrl: String = "";

            if (item is Track) {

                holder.title.text = item.title;
                holder.artist.visibility = View.VISIBLE;
                holder.artist.text = item.album?.artist?.name;
                artwork = holder.artwork;
                imageUrl = item.image ?: "";

                holder.container.setOnClickListener {

                    val controller = AudioPlayer.getInstance().init(context);

                    if (controller.currentPlayListCode != playListCode) {
                        val playListFactory = PlayListFactory(context);
                        controller.start(playListFactory.track(list as ArrayList<Track>), position, playListCode);
                    } else
                        controller.start(index = position);

                    val i = Intent(AudioPlayer.ACTION_CHANGE_BOTTOM_PLAYER_STATE)
                    i.putExtra("open", true);
                    context.sendBroadcast(i);
//                    (context as AlbumsTrackListActivity).openBottomPlayer();
                };

            } else if (item is  Album) {

                holder.title.text = item.title;
                artwork = holder.artwork;
                imageUrl = item.image ?: "";

                if (item.artist != null) {
                    holder.artist.visibility = View.VISIBLE;
                    holder.artist.text = item.artist?.name;
                }

                holder.container.setOnClickListener {
                    val albumActivity = Intent(context, AlbumActivity::class.java);
                    albumActivity.putExtra("album", Gson().toJson(item));
                    albumActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    context.startActivity(albumActivity);
                };
            }

            GlideApp.with(context).load(imageUrl).into(artwork!!);
        }
    }

    inner class MainItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val container: CardView = itemView?.findViewById(R.id.album_item_container)!!;
        val title: TextView = itemView?.findViewById(R.id.album_item_title)!!;
        val artist: TextView = itemView?.findViewById(R.id.album_item_artist)!!;
        val artwork: ImageView = itemView?.findViewById(R.id.album_item_cover)!!;

        init {
            artwork.clipToOutline = true;
        }
    }
}