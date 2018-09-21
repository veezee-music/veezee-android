package cloud.veezee.android.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cloud.veezee.android.R

class AccountHorizontalListAdapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val inflater = LayoutInflater.from(context);


    override fun getItemCount(): Int = 10;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.item_recently_played, parent, false);
        return MainViewHolder(view);
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}