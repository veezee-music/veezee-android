package cloud.veezee.android.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import cloud.veezee.android.Constants
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.HomePageItem
import cloud.veezee.android.models.Track
import cloud.veezee.android.utils.AudioPlayer
import cloud.veezee.android.utils.PlayListFactory
import com.google.gson.Gson
import cloud.veezee.android.R
import cloud.veezee.android.activities.HomePageActivity
import cloud.veezee.android.application.GlideApp
import cloud.veezee.android.utils.interfaces.OnDialogButtonsClickListener
import cloud.veezee.android.utils.TrackMenu
import cloud.veezee.android.utils.UserManager
import kotlin.collections.ArrayList

class SearchVerticalListAdapter(val context: Context, private val searchItems: ArrayList<HomePageItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var headerCounterAfter: HashMap<String, Int> = HashMap();
    private var headerCounter: Int = 0;
    private var playListCode = 0;
    private var listSize = 0;
    private var updateList = true;
    private var list: ArrayList<Any?> = ArrayList();
    private var inflater: LayoutInflater = LayoutInflater.from(context);

    companion object {
        const val FLAG_HEADER = 0;
        const val FLAG_MAIN = 1;
    }

    private fun calculateSize(item: HomePageItem?): Int {
        val type = item?.type;
        var size = 0;

        when (type) {
            HomePageItem.TRACK -> {
                size = item.trackList?.size!!;
                if (size > 0) {
                    list.add(item.title);
                    list.addAll(item.trackList!!);

                    headerCounter++;
                    headerCounterAfter[HomePageItem.TRACK] = headerCounter;

                    return size;
                }
            };
            HomePageItem.ALBUM -> {
                size = item.albumList?.size!!
                if (size > 0) {
                    list.add(item.title);
                    list.addAll(item.albumList!!);

                    headerCounter++;
                    headerCounterAfter[HomePageItem.ALBUM] = headerCounter;

                    return size;
                }
            };
        }

        return 0;
    }

    fun notifyDataSetChange() {
        if (list.size > 0)
            list.clear();
        listSize = 0;
        playListCode = System.currentTimeMillis().toInt();
        headerCounter = 0;
        headerCounterAfter.clear();
        updateList = true;
        notifyDataSetChanged();
    }

    private fun extractTrack(): ArrayList<Track> {

        val tracks = ArrayList<Track>();

        (0 until listSize)
                .filter { list[it] is Track }
                .mapTo(tracks) { list[it] as Track }

        return tracks;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view: View;

        return if (viewType == cloud.veezee.android.adapters.SearchVerticalListAdapter.Companion.FLAG_MAIN) {
            view = inflater.inflate(cloud.veezee.android.R.layout.item_search_track, parent, false);
            val viewHolder = TrackViewHolder(view);

            viewHolder;

        } else {
            view = inflater.inflate(cloud.veezee.android.R.layout.item_search_header, parent, false);
            val viewHolder = HeaderViewHolder(view);

            viewHolder;
        }
    }

    override fun getItemCount(): Int {
        if (updateList && searchItems.size != 0) {

            updateList = false;

            for (i in 0 until searchItems.size) {
                listSize += calculateSize(searchItems[i]);
            }
        }
        return listSize + headerCounter;
    }

    override fun getItemViewType(position: Int): Int {
        if (list[position] is String)
            return cloud.veezee.android.adapters.SearchVerticalListAdapter.Companion.FLAG_HEADER;
        return cloud.veezee.android.adapters.SearchVerticalListAdapter.Companion.FLAG_MAIN;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SearchVerticalListAdapter.TrackViewHolder) {

            var title: String? = "";
            var artist: String? = "";
            var imageUrl: String? = ""

            if (list[position] is Track) {
                val track: Track = list[position] as Track;

                // track position without headers
                val clearTrackPosition = holder.adapterPosition - (headerCounterAfter[HomePageItem.TRACK]
                        ?: 0);

                title = track.title;
                artist = track.album?.artist?.name;
                imageUrl = track.image ?: "";

                holder.container.setOnClickListener {
                    val controller = AudioPlayer.getInstance().init(context);

                    if (controller.currentPlayListCode != playListCode) {
                        val playListFactory = PlayListFactory(context);
                        controller.start(playListFactory.track(extractTrack()), clearTrackPosition, playListCode);
                    } else
                        controller.start(index = clearTrackPosition);

                    (context as HomePageActivity).openBottomPlayer();
                };

                holder.container.setOnLongClickListener {

                    val view = LayoutInflater.from(context).inflate(R.layout.dialog_track_menu, null);

                    if(Constants.GUEST_MODE)
                        view?.findViewById<Button>(R.id.dialog_menu_add)?.visibility = View.GONE;

                    TrackMenu().whit(context)
                            .view(view)
                            .transparentBackground()
                            .gravity(Gravity.BOTTOM)
                            .content(track)
                            .setOnButtonsClickListener(object : OnDialogButtonsClickListener {
                                override fun addClickListener(dialog: AlertDialog) {

                                    val trackJsonIntent = Intent("TrackMenu");
                                    trackJsonIntent.putExtra("track", Gson().toJson(track));

                                    context.sendBroadcast(trackJsonIntent);
                                }

                                override fun cancelCLickListener(dialog: AlertDialog) {
                                    dialog.dismiss();
                                }
                            })
                            .show();

                    return@setOnLongClickListener false;
                };


            } else if (list[position] is Album) {
                val album: Album = list[position] as Album;

                //album position without headers
                val clearAlbumPosition = holder.adapterPosition - (headerCounterAfter[HomePageItem.ALBUM]
                        ?: 0);

                title = album.title;
                artist = album.artist?.name;
                imageUrl = album.image ?: "";

                holder.container.setOnClickListener {
                    val albumActivity = Intent(context, cloud.veezee.android.activities.AlbumActivity::class.java);
                    albumActivity.putExtra("album", Gson().toJson(album));
                    albumActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    context.startActivity(albumActivity);
                }

            }

            holder.title.text = title;
            holder.artist.text = artist;

            GlideApp.with(context).load(imageUrl).into(holder.artWork);

        } else if (holder is HeaderViewHolder) {

            val title = list[position] as String;
            holder.title.text = title;
        }
    }

    inner class TrackViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView?.findViewById(R.id.search_track_title)!!;
        var artist: TextView = itemView?.findViewById(R.id.search_track_artist)!!;
        var artWork: ImageView = itemView?.findViewById(R.id.search_track_artWork)!!;
        var container: ConstraintLayout = itemView?.findViewById(R.id.track_item_search_container)!!;

        init {
            artWork.clipToOutline = true;
        }
    }

    inner class HeaderViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView?.findViewById(cloud.veezee.android.R.id.search_header_title)!!;
    }

}