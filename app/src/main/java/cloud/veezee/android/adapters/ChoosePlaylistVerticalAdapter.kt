package cloud.veezee.android.adapters

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cloud.veezee.android.adapters.interfaces.OnListItemClickListener
import cloud.veezee.android.models.Album
import cloud.veezee.android.R
import cloud.veezee.android.application.GlideApp

class ChoosePlaylistVerticalAdapter(val context: Context,val list: ArrayList<Album>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater = LayoutInflater.from(context);
    private var targetPosition: Int = -1;
    private var listener: OnListItemClickListener? = null;

    private val TAG = "ChoosePlaylistAdapter";

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view: View?;

        return when (viewType) {
            0 -> {
                view = inflater.inflate(R.layout.item_playlist, parent, false);
                MainViewHolder(view);
            }
            1 -> {
                view = inflater.inflate(R.layout.item_plus, parent, false);
                CreatePlaylistViewHolder(view);
            }
            else -> {
                view = inflater.inflate(R.layout.item_plus, parent, false);
                CreatePlaylistViewHolder(view);
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MainViewHolder) {

            val album: Album = list[holder.adapterPosition - 1];

            val title = holder.title;
            val artwork = holder.artwork;
            val tracksCount = holder.tracksCount;
            val circleCheck = holder.circleCheck;

            title.text = album.title;
            val trackCountText = album.tracks?.size.toString() + " Track(s)";
            tracksCount.text = trackCountText;

            GlideApp.with(context).load(album.image ?: "").into(artwork);

            if (targetPosition != -1 && position == targetPosition) {
                circleCheck.visibility = View.VISIBLE;
            } else {
                circleCheck.visibility = View.INVISIBLE;
            }

            holder.container.setOnClickListener {
                val newPosition = holder.adapterPosition;

                listener?.onClick(album.id!!, newPosition);

                targetPosition = if(newPosition == targetPosition) {
                    notifyItemChanged(newPosition);
                    -1;
                } else {
                    notifyItemChanged(newPosition);
                    notifyItemChanged(targetPosition);
                    newPosition;
                }
            };
        } else if (holder is CreatePlaylistViewHolder) {

            holder.container.setOnClickListener {
                val newPosition = holder.adapterPosition;

                listener?.onClick("", newPosition);
            };
        }
    }

    fun setOnItemClickListener(listener: OnListItemClickListener) {
        this.listener = listener;
    }

    override fun getItemCount(): Int {
        return list.size + 1;
    }

    override fun getItemViewType(position: Int): Int = if (position != 0) 0 else 1;

    inner class CreatePlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val plus: ImageView = itemView.findViewById(R.id.plus_image)!!;
        val container: CardView = itemView.findViewById(R.id.plus_container)!!;

        init {
            plus.clipToOutline = true;
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: CardView = itemView.findViewById(R.id.playlist_item_container)!!;
        val title: TextView = itemView.findViewById(R.id.playlist_item_title)!!;
        val tracksCount: TextView = itemView.findViewById(R.id.playlist_item_count)!!;
        val artwork: ImageView = itemView.findViewById(R.id.playlist_item_artwork)!!;
        val circleCheck: ImageView = itemView.findViewById(R.id.playlist_item_check)!!;

        init {
            artwork.clipToOutline = true;
        }
    }
}