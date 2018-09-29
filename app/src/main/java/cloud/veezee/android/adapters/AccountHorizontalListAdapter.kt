package cloud.veezee.android.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cloud.veezee.android.R
import cloud.veezee.android.application.GlideOptions.bitmapTransform
import cloud.veezee.android.models.Track
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import jp.wasabeef.glide.transformations.BlurTransformation

class AccountHorizontalListAdapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    public var itemList: ArrayList<Track> = ArrayList();

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
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumItemCover: ImageView = itemView.findViewById(R.id.track_album_art)!!;
        val trackTitle: TextView = itemView.findViewById(R.id.track_title)!!;
    }
}