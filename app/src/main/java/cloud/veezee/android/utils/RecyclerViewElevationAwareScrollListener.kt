package cloud.veezee.android.utils;

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.RecyclerView

class RecyclerViewElevationAwareScrollListener(private var elevationView: AppBarLayout) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy);

        if(!recyclerView.canScrollVertically(-1)) {
            // we have reached the top of the list
            elevationView.elevation = 0f
        } else {
            // we are not at the top yet
            elevationView.elevation = 50f
        }
    }
}