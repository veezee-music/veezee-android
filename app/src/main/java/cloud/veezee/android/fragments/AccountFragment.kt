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
import java.util.concurrent.TimeUnit

class AccountFragment : Fragment() {

    private val TAG = "AccountFragment Console";

//    private var daysLeftText: TextView? = null;
//    private var hoursLeftText: TextView? = null;
    private var userName: TextView? = null;
    private var userEmail: TextView? = null;
    private var list: RecyclerView? = null;

    private var account: UserManager? = null;
    private var google: GoogleSignInHelper? = null;


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true);

        initComponents(view);
        recentlyPlayedList();

        return view;
    }

    private fun recentlyPlayedList() {
        val adapter = AccountHorizontalListAdapter(context!!);
        list?.layoutManager = CenterZoomLayoutManager(context, LinearLayout.HORIZONTAL, false);
        list?.adapter = adapter;
        list?.smoothScrollBy(1, 0);
        LinearSnapHelper().attachToRecyclerView(list);
    }

//    private fun leftTime(start: Long = 0, end: Long = 0): HashMap<String, Long> {
//
//        var secondDif = Math.abs(end - start);
//
//        val time = HashMap<String, Long>();
//
//        val day = TimeUnit.SECONDS.toDays(secondDif);
//        secondDif -= TimeUnit.DAYS.toSeconds(day);
//
//        val hours = TimeUnit.SECONDS.toHours(secondDif);
//        secondDif -= TimeUnit.HOURS.toSeconds(hours);
//
//        val minute = TimeUnit.SECONDS.toMinutes(secondDif);
//        secondDif -= TimeUnit.MINUTES.toMillis(minute);
//
//        time.put("day", day);
//        time.put("hour", hours);
//        time.put("minute", minute);
//
//        return time;
//    }

    private fun initComponents(view: View?) {

        userName = view?.findViewById(R.id.user_name);
        userEmail = view?.findViewById(R.id.user_email);
        list = view?.findViewById(R.id.recently_played);

        account = UserManager.get(context!!);

        userName?.text = account?.name;
        userEmail?.text = account?.email;

//        if (account?.isAccessExpired!!) {
//            daysLeftText?.text = "0";
//            hoursLeftText?.text = context?.getString(R.string.expired);
//        } else {
//            val leftTime = leftTime(account?.access?.expiresIn!!, now() / 1000);
//            val leftDays = leftTime["day"];
//            val leftHours = leftTime["hour"];
//
//            daysLeftText?.text = leftDays.toString();
//            hoursLeftText?.text = leftHours.toString() + if (leftHours!! > 0) " Hours" else "Hour";
//        }
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