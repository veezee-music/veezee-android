package cloud.veezee.android.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cloud.veezee.android.R
import cloud.veezee.android.application.GlideOptions.bitmapTransform
import cloud.veezee.android.models.Track
import cloud.veezee.android.utils.AudioPlayer
import cloud.veezee.android.utils.PlayListFactory
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class AccountHorizontalListAdapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    public var itemList: ArrayList<Track> = ArrayList();
    private var playListCode = System.currentTimeMillis().toInt();

    val inflater = LayoutInflater.from(context);

    override fun getItemCount(): Int = itemList.size;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.item_recently_played, parent, false);

        return MainViewHolder(view);
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is MainViewHolder) {
            val track: Track = itemList[holder.adapterPosition];

            if(track.image != null) {
                Glide.with(context).load(track.image)
                        .apply(bitmapTransform(RoundedCornersTransformation(25, 15)))
                        .into(holder.albumItemCover);
            } else {
                Glide.with(context).load(R.drawable.placeholder)
                        .apply(bitmapTransform(RoundedCornersTransformation(25, 15)))
                        .into(holder.albumItemCover);
            }

            holder.trackTitle.text = track.title;

            holder.container.setOnClickListener {
                val controller = AudioPlayer.getInstance().init(context);

                if (controller.currentPlayListCode != playListCode) {
                    val playListFactory = PlayListFactory(context);
                    controller.start(playListFactory.track(itemList), position, playListCode);
                } else
                    controller.start(index = position);

                val i = Intent(AudioPlayer.ACTION_CHANGE_BOTTOM_PLAYER_STATE)
                i.putExtra("open", true);
                context.sendBroadcast(i);
            }
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.container);
        val albumItemCover: ImageView = itemView.findViewById(R.id.track_album_art);
        val trackTitle: TextView = itemView.findViewById(R.id.track_title);
    }
}