package cloud.veezee.android.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import cloud.veezee.android.R
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.models.HomePageItem
import cloud.veezee.android.utils.contentReadyToShow
import cloud.veezee.android.utils.hideKeyboard
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.miguelcatalan.materialsearchview.MaterialSearchView
import cloud.veezee.android.adapters.SearchVerticalListAdapter
import cloud.veezee.android.api.API
import cloud.veezee.android.api.search
import cloud.veezee.android.externalComponentsAndLibs.HidingScrollListener
import kotlinx.android.synthetic.main.activity_home_page.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.json.JSONObject


class SearchFragment : Fragment() {
    private var materialSearchView: MaterialSearchView? = null;
    private var searchRecyclerView: RecyclerView? = null;
    private var loading: ProgressBar? = null;

    private var adapter: SearchVerticalListAdapter? = null;
    private var request: AppClient? = null;
    private var searchItems: ArrayList<HomePageItem> = ArrayList();

    private val queryTextListener = object : MaterialSearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {

            API.Lists.search(context!!, query!!, volleyResponseListener)
            hideKeyboard(context!!);

            (searchRecyclerView as View).contentReadyToShow(false, loading);
            return true;
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return false;
        }

    }

    private val volleyResponseListener = object : HttpRequestListeners.StringResponseListener {
        override fun response(response: String?) {
            if(response == null)
                return;

            if (searchItems.size > 0) {
                searchItems.clear();
                adapter?.notifyDataSetChange();
            }

            searchItems.addAll(Gson().fromJson(response, object : TypeToken<ArrayList<HomePageItem>>() {}.type));
            adapter?.notifyDataSetChange();

            (searchRecyclerView as View).contentReadyToShow(true, loading);

        }

    };

    private fun initializeList() {

        adapter = SearchVerticalListAdapter(context!!, searchItems);
        searchRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

        val tabHeight = resources.getDimension(R.dimen.tab_layout_height).toInt();

        searchRecyclerView?.addOnScrollListener(object : HidingScrollListener(tabHeight) {
            override fun onShow() {
                val valueAnimation = ValueAnimator.ofInt(activity?.tab_layout?.height!!, tabHeight);
                valueAnimation.addUpdateListener { animation ->
                    activity?.tab_layout?.layoutParams?.height = (animation?.animatedValue) as Int;
                    activity?.tab_layout?.requestLayout();
                };
                valueAnimation.start();
            }

            override fun onHide() {
            }

            override fun onMoved(distance: Int) {
                activity?.tab_layout?.layoutParams?.height = tabHeight - distance;
                activity?.tab_layout?.requestLayout();
            }
        });
        searchRecyclerView?.adapter = adapter;

        OverScrollDecoratorHelper.setUpOverScroll(searchRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater.inflate(R.layout.fragment_search, container, false);

        materialSearchView = activity?.homePage_material_searchView;
        materialSearchView?.setOnQueryTextListener(queryTextListener);
        searchRecyclerView = view?.findViewById(R.id.search_list);
        loading = view?.findViewById(R.id.search_loading);

        request = AppClient(context!!);

        initializeList();

        return view;
    }
}