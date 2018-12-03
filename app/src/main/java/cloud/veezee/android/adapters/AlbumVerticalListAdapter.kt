package cloud.veezee.android.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import cloud.veezee.android.Constants
import com.google.gson.Gson
import cloud.veezee.android.activities.AlbumActivity
import cloud.veezee.android.utils.interfaces.OnDialogButtonsClickListener
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.Track
import cloud.veezee.android.utils.AudioPlayer
import cloud.veezee.android.utils.PlayListFactory
import cloud.veezee.android.utils.TrackMenu
import cloud.veezee.android.R
import cloud.veezee.android.utils.UserManager

class AlbumVerticalListAdapter(var album: Album, var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context);
    private val tracks: ArrayList<Track> = album.tracks!!;
    private var playListCode = System.currentTimeMillis().toInt();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view: View = inflater.inflate(R.layout.item_album_track, parent, false);
        val holder = MainRowViewHolder(view);

        return holder;
    }

    override fun getItemCount(): Int = tracks.size;

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MainRowViewHolder) {
            val track: Track = tracks[position];
            holder.title.text = track.title
            holder.number.text = (position + 1).toString();

            if (track.album != null) {
                holder.artist.text = track.album?.artist?.name;
                holder.artist.visibility = View.VISIBLE;
            } else {
                holder.artist.visibility = View.GONE;
            }

            holder.container.setOnClickListener {
                val controller = AudioPlayer.getInstance().init(context);

                if(controller.currentPlayListCode != playListCode) {
                    val playListFactory = PlayListFactory(context);
                    controller.start(playListFactory.album(album), position, playListCode);
                } else
                    controller.start(index =  position);

                val i = Intent(AudioPlayer.ACTION_CHANGE_BOTTOM_PLAYER_STATE)
                i.putExtra("open", true);
                context.sendBroadcast(i);
//                (context as AlbumActivity).openBottomPlayer();
            };

            holder.container.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_track_menu, null);

                if(track.album == null)
                    track.album = cloneAlbum();

                if(Constants.GUEST_MODE == true)
                    view?.findViewById<Button>(R.id.dialog_menu_add)?.visibility = View.GONE;

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
            }


        }
    }

    private fun cloneAlbum(): Album {
        val albumJson = Gson().toJson(album);
        val albumCopy: Album = Gson().fromJson(albumJson, Album::class.java);
        albumCopy.tracks = null;

        return albumCopy;
    }

    //ViewHolders
    inner class MainRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val container: CardView = itemView.findViewById(R.id.album_track_container)!!;
        val number: TextView = itemView.findViewById(R.id.album_track_number)!!;
        val title: TextView = itemView.findViewById(R.id.album_track_title)!!;
        val artist: TextView = itemView.findViewById(R.id.album_track_artist)!!;
    }
}