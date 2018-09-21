package cloud.veezee.android.adapters

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.*
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cloud.veezee.android.Constants
import cloud.veezee.android.activities.AlbumsTrackListActivity
import cloud.veezee.android.externalComponentsAndLibs.CenterZoomLayoutManager

import cloud.veezee.android.models.HomePageItem
import cloud.veezee.android.externalComponentsAndLibs.StartSnapHelper
import cloud.veezee.android.R
import cloud.veezee.android.R.layout.item_browse_horizontal_list

class BrowseVerticalListAdapter(private val context: Context, private val list: ArrayList<HomePageItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "BrowseVerticalAdapter";

    private val inflater: LayoutInflater = LayoutInflater.from(context);

    private fun gridLayoutManager(count: Int, orientation: Int): GridLayoutManager = GridLayoutManager(context, count, orientation, false);

    private fun linearLayoutManager(orientation: Int): LinearLayoutManager = LinearLayoutManager(context, orientation, false);

    private fun centerZoomLayoutManager(orientation: Int): CenterZoomLayoutManager = CenterZoomLayoutManager(context, orientation, false);

    private fun getLayoutManager(position: Int): RecyclerView.LayoutManager {

        val type = list[position].type;

        return when (type) {
            HomePageItem.ALBUM -> {
                val albumsSize = list[position].albumList!!.size;
                gridLayoutManager(if(Constants.OFFLINE_ACCESS) { if (albumsSize < 2) albumsSize else 2 } else {2}, GridLayoutManager.HORIZONTAL);
            }
            HomePageItem.HEADER -> linearLayoutManager(LinearLayoutManager.HORIZONTAL);
            HomePageItem.GENRE -> gridLayoutManager(1, GridLayoutManager.HORIZONTAL);
            HomePageItem.TRACK -> {
                val tracksSize = list[position].trackList?.size!!;
                gridLayoutManager(if (Constants.OFFLINE_ACCESS) { if(tracksSize < 4) tracksSize else 4  } else {3}, GridLayoutManager.HORIZONTAL)
            }
            else -> {
                linearLayoutManager(LinearLayoutManager.HORIZONTAL);
            }
        }
    }

    private fun hasSnapHelper(type: String): Boolean {

        return when (type) {
            HomePageItem.ALBUM -> true;
            HomePageItem.HEADER -> true;
            HomePageItem.TRACK -> false;
            else -> {
                false;
            }
        }
    }

    private fun attachSnapToRecycler(recycler: RecyclerView) {
        val snap = StartSnapHelper(Gravity.START);
        snap.attachToRecyclerView(recycler);
    }

    private fun initializeLists(position: Int, recycler: RecyclerView) {
        val itemsListAdapterBrowse = BrowseHorizontalListAdapter(context, list[position]);

        recycler.layoutManager = getLayoutManager(position);
        recycler.adapter = itemsListAdapterBrowse;
        //attachSnapToRecycler(recycler);
        //OverScrollDecoratorHelper.setUpOverScroll(recycler, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = inflater.inflate(item_browse_horizontal_list, parent, false);
        val mainHolder = HomePageViewHolder(view);

        return mainHolder;
    }

    override fun getItemCount(): Int = list.size;

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val homePageItem: HomePageItem = list[position];
        val title = homePageItem.title;
        val type = homePageItem.type;

        if (holder is HomePageViewHolder) {
            val list = holder.itemsRecycler;
            val headerContainer = holder.headerContainer;
            val headerTitle = holder.title;
            val moreResult = holder.moreResult;

            headerContainer.visibility = if (title == null) View.GONE else View.VISIBLE;
            headerTitle.text = title ?: "";

            if (type == HomePageItem.GENRE)
                moreResult.visibility = View.GONE;

            initializeLists(holder.adapterPosition, list);

            moreResult.setOnClickListener {
                val newPosition = holder.adapterPosition;

                val albumsTrackListIntent = Intent(context, AlbumsTrackListActivity::class.java);
                albumsTrackListIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
                if (type == HomePageItem.ALBUM && this.list[newPosition].albumList!![0].artist == null) {

                    albumsTrackListIntent.putExtra("type", "Playlist");
                    context.startActivity(albumsTrackListIntent);
                    return@setOnClickListener;
                }
                albumsTrackListIntent.putExtra("type", type);

                context.startActivity(albumsTrackListIntent);
            }
        }
    }

    inner class HomePageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerContainer: ConstraintLayout = itemView.findViewById(R.id.browse_header)!!;
        val title: TextView = itemView.findViewById(R.id.browse_header_title)!!;
        val moreResult: TextView = itemView.findViewById(R.id.browse_header_more)!!;
        val itemsRecycler: RecyclerView = itemView.findViewById(R.id.browse_horizontal_list)!!;
    }
}