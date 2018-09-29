package cloud.veezee.android.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import cloud.veezee.android.activities.LoginActivity
import cloud.veezee.android.adapters.AccountHorizontalListAdapter
import cloud.veezee.android.externalComponentsAndLibs.CenterZoomLayoutManager
import cloud.veezee.android.google.GoogleSignInHelper
import cloud.veezee.android.google.interfaces.GoogleSignOutListener
import cloud.veezee.android.utils.now
import cloud.veezee.android.utils.UserManager
import cloud.veezee.android.R
import cloud.veezee.android.api.API
import cloud.veezee.android.api.tracksHistory
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yarolegovich.discretescrollview.DiscreteScrollView
import java.util.concurrent.TimeUnit
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer


class AccountFragment : Fragment() {

    private val TAG = "AccountFragment";

    private var userName: TextView? = null;
    private var userEmail: TextView? = null;
    private var list: DiscreteScrollView? = null;

    private var account: UserManager? = null;
    private var google: GoogleSignInHelper? = null;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true);

        initComponents(view);
        recentlyPlayedList();

        return view;
    }

    private val volleyRequestListeners = object : HttpRequestListeners.StringResponseListener {
        override fun response(response: String?) {
            (list?.adapter as AccountHorizontalListAdapter).itemList = Gson().fromJson(response, object : TypeToken<ArrayList<Track>>() {}.type);
            list?.adapter?.notifyDataSetChanged();
        }
    }

    fun loadRecentlyPlayedTracks() {
        API.VEX.tracksHistory(this.context!!, volleyRequestListeners);
    }

    private fun recentlyPlayedList() {
        //list?.layoutManager = CenterZoomLayoutManager(context, LinearLayout.HORIZONTAL, false);
        list?.adapter = AccountHorizontalListAdapter(context!!);
        list?.setItemTransformer(ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.CENTER) // CENTER is a default one
                .build())
        list?.setSlideOnFling(true);

        loadRecentlyPlayedTracks();
    }

    private fun initComponents(view: View?) {
        userName = view?.findViewById(R.id.user_name);
        userEmail = view?.findViewById(R.id.user_email);
        list = view?.findViewById(R.id.recently_played);

        account = UserManager.get(context);

        userName?.text = account?.name;
        userEmail?.text = account?.email;
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater?.inflate(R.menu.account_fragment_menu, menu);
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId;

        when (id) {
            R.id.logout -> {
                logout();
                val loginActivity = Intent(context, LoginActivity::class.java);
                context?.startActivity(loginActivity);

                activity?.finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private fun logout() {
        google = GoogleSignInHelper(context!!);
        google?.signOut(object : GoogleSignOutListener {
            override fun onCompleted() {
                UserManager.remove(context!!);
                return;
            }
        });

        UserManager.remove(context!!);
    }
}