package cloud.veezee.android.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import cloud.veezee.android.Constants
import cloud.veezee.android.models.HomePageItem
import cloud.veezee.android.utils.AudioPlayer
import cloud.veezee.android.utils.PlayListFactory
import com.google.gson.Gson
import cloud.veezee.android.activities.AlbumActivity
import cloud.veezee.android.activities.HomePageActivity
import cloud.veezee.android.utils.interfaces.OnDialogButtonsClickListener
import cloud.veezee.android.utils.TrackMenu
import cloud.veezee.android.R
import cloud.veezee.android.application.GlideApp
import cloud.veezee.android.utils.UserManager


class BrowseHorizontalListAdapter(private val context: Context, private val homePageItem: HomePageItem) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "HorizontalList";
    private val type: String = homePageItem.type!!;

    private var inflater = LayoutInflater.from(context);
    private var playListCode = System.currentTimeMillis().toInt();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view: View;

        when (type) {
            HomePageItem.GENRE -> {
                view = inflater.inflate(R.layout.item_home_genre, parent, false);
                val genreViewHolder = GenreViewHolder(view);

                return genreViewHolder;
            }
            HomePageItem.ALBUM -> {
                view = inflater.inflate(R.layout.item_home_album, parent, false);
                val albumViewHolder = AlbumViewHolder(view);

                return albumViewHolder;
            }
            HomePageItem.HEADER -> {
                view = inflater.inflate(R.layout.item_home_header, parent, false);
                val headerViewHolder = HeaderViewHolder(view);

                return headerViewHolder;
            }
            HomePageItem.TRACK -> {
                view = inflater.inflate(R.layout.item_home_track, parent, false);
                val trackViewHolder = TrackViewHolder(view);

                return trackViewHolder;
            }
        }

        return AlbumViewHolder(View(context));
    }

    override fun getItemCount(): Int {
        return when (type) {
            HomePageItem.ALBUM -> {
                homePageItem.albumList?.size as Int;
            }
            HomePageItem.HEADER -> {
                homePageItem.headerList?.size as Int;
            }
            HomePageItem.TRACK -> {
                homePageItem.trackList?.size as Int;
            }
            HomePageItem.GENRE -> {
                homePageItem.genreList?.size as Int;
            }
            else -> 0;
        }
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var imageUrl = "";
        var artWork: ImageView? = null;
        when (holder) {
            is AlbumViewHolder -> {
                val album = homePageItem.albumList?.get(position);

                holder.title.text = album?.title
                if (album?.artist != null) {
                    holder.artist.visibility = View.VISIBLE;
                    holder.artist.text = album.artist?.name;
                } else {
                    holder.artist.visibility = View.GONE;
                }

                imageUrl = album?.image!!;
                artWork = holder.artWork;

                holder.container.setOnClickListener {
                    val albumActivity = Intent(context, AlbumActivity::class.java);
                    albumActivity.putExtra("album", Gson().toJson(album));
                    albumActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    context.startActivity(albumActivity);
                };

            }
            is GenreViewHolder -> {
                val genre = homePageItem.genreList?.get(position);

                holder.title.text = genre?.title

                imageUrl = genre?.image!!;
                artWork = holder.artWork;
            }
            is HeaderViewHolder -> {
                val header = homePageItem.headerList?.get(position);

                holder.title.text = header?.title;
                holder.note.text = header?.type?.toUpperCase();

                if(header?.artist != null ) {
                    holder.artist.visibility = View.VISIBLE;
                    holder.artist.text = header.artist?.name;
                } else {
                    holder.artist.visibility = View.GONE;
                }

                imageUrl = header?.image!!;
                artWork = holder.artWork

                holder.container.setOnClickListener {
                    val albumActivity = Intent(context, AlbumActivity::class.java);
                    albumActivity.putExtra("album", Gson().toJson(header.album));
                    albumActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    context.startActivity(albumActivity);
                };
            }
            is TrackViewHolder -> {
                val track = homePageItem.trackList?.get(position);

                holder.title.text = track?.title;
                holder.artist.text = track?.album?.artist?.name;

                imageUrl = track?.image!!;
                artWork = holder.artwork;

                holder.container.setOnClickListener {
                    val controller = AudioPlayer.getInstance().init(context);

                    if (controller.currentPlayListCode != playListCode) {
                        val playListFactory = PlayListFactory(context);
                        controller.start(playListFactory.track(homePageItem.trackList), position, playListCode);
                    } else
                        controller.start(index = position);

                    (context as HomePageActivity).openBottomPlayer();
                };

                holder.container.setOnLongClickListener {

                    val view = LayoutInflater.from(context).inflate(R.layout.dialog_track_menu, null);

                    if(Constants.GUEST_MODE)
                        view?.findViewById<Button>(R.id.dialog_menu_add)?.visibility = View.GONE;

                    if(track.image == null)
                        track.image = track.album?.image;

                    TrackMenu().with(context)
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

            }
        }

        GlideApp.with(context).load(imageUrl).thumbnail(0.1f).into(artWork!!);
    }

    inner class HeaderViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var container: CardView = itemView?.findViewById(R.id.header_item_container)!!;
        val title: TextView = itemView?.findViewById(R.id.header_item_title)!!;
        val note: TextView = itemView?.findViewById(R.id.header_item_note)!!;
        val artWork: ImageView = itemView?.findViewById(R.id.header_item_cover)!!;
        val artist: TextView = itemView?.findViewById(R.id.header_item_artist)!!;

        init {
            artWork.clipToOutline = true;
        }
    }

    inner class AlbumViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var container: CardView = itemView?.findViewById(R.id.album_item_container)!!;
        val title: TextView = itemView?.findViewById(R.id.album_item_title)!!;
        val artWork: ImageView = itemView?.findViewById(R.id.album_item_cover)!!;
        val artist: TextView = itemView?.findViewById(R.id.album_item_artist)!!;

        init {
            artWork.clipToOutline = true;
        }
    }

    inner class GenreViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var container: CardView = itemView?.findViewById(R.id.genre_item_container)!!;
        val title: TextView = itemView?.findViewById(R.id.genre_item_title)!!;
        val artWork: ImageView = itemView?.findViewById(R.id.genre_item_cover)!!;

        init {
            artWork.clipToOutline = true;
        }
    }

    inner class TrackViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var container: CardView = itemView?.findViewById(R.id.track_item_home_container)!!;
        val title: TextView = itemView?.findViewById(R.id.track_item_title)!!;
        val artwork: ImageView = itemView?.findViewById(R.id.track_item_cover)!!;
        val artist: TextView = itemView?.findViewById(R.id.track_item_artist)!!;

        init {
            artwork.clipToOutline = true;
        }
    }

}