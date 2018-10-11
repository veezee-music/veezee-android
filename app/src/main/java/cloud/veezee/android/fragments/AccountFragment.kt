package cloud.veezee.android.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import cloud.veezee.android.activities.LoginActivity
import cloud.veezee.android.adapters.AccountHorizontalListAdapter
import cloud.veezee.android.google.GoogleSignInHelper
import cloud.veezee.android.google.interfaces.GoogleSignOutListener
import cloud.veezee.android.utils.UserManager
import cloud.veezee.android.R
import cloud.veezee.android.api.API
import cloud.veezee.android.api.tracksHistory
import cloud.veezee.android.api.updateNameAndPassword
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import org.json.JSONObject

class AccountFragment : Fragment() {

    private val TAG = "AccountFragment";

    private var changeNameAndPassword: TextView? = null;
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
        changeNameAndPassword = view?.findViewById(R.id.changeNameAndPassword);
        userName = view?.findViewById(R.id.user_name);
        userEmail = view?.findViewById(R.id.user_email);
        list = view?.findViewById(R.id.recently_played);

        account = UserManager.get(context);

        userName?.text = account?.name;
        userEmail?.text = account?.email;

        changeNameAndPassword?.setOnClickListener {
            val changeNameAndPasswordDialog = LayoutInflater.from(context).inflate(R.layout.dialog_update_name_password, null);
            ChangeNameAndPasswordAlertDialog().with(context!!).view(changeNameAndPasswordDialog).show();
        }
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

    inner class ChangeNameAndPasswordAlertDialog {
        private var dialogBuilder: AlertDialog.Builder? = null;
        private var alertDialog: AlertDialog? = null;
        private var context: Context? = null;
        private var onProcess = false;

        private lateinit var nameEditText: TextInputEditText;
        private lateinit var passwordEditText: TextInputEditText;
        private lateinit var confirm: Button;
        private lateinit var cancel: Button;
        private lateinit var createPlaylistLoading: ProgressBar;

        private val volleyResponseListener = object : HttpRequestListeners.StringResponseListener {
            override fun response(response: String?) {
                userName?.text = nameEditText.text.toString();

                alertDialog?.dismiss();
            }

            override fun error(er: String?, responseStatusCode: Int?) {
                Toast.makeText(context, er, Toast.LENGTH_LONG).show();
                onProcess = false;
                createPlaylistLoading.visibility = View.INVISIBLE;
            }
        }

        fun with(context: Context): ChangeNameAndPasswordAlertDialog {
            this.context = context;

            dialogBuilder = AlertDialog.Builder(context);

            return this;
        }

        fun view(view: View): ChangeNameAndPasswordAlertDialog {
            dialogBuilder?.setView(view);

            prepareComponent(view);

            return this;
        }

        fun show(): ChangeNameAndPasswordAlertDialog {
            alertDialog = dialogBuilder?.create();
            alertDialog?.show();

            return this;
        }

        private fun prepareComponent(view: View) {
            nameEditText = view.findViewById(R.id.name_edit_text);
            passwordEditText = view.findViewById(R.id.password_edit_text);
            confirm = view.findViewById(R.id.submit_button);
            cancel = view.findViewById(R.id.cancel_button);
            createPlaylistLoading = view.findViewById(R.id.create_playlist_confirm_loading);

            confirm.setOnClickListener {
                if (!onProcess) {

                    createPlaylistLoading.visibility = View.VISIBLE;

                    val name: String = nameEditText.text.toString();
                    val password: String = passwordEditText.text.toString();
                    API.Account.updateNameAndPassword(context!!, name, password, volleyResponseListener);

                    onProcess = true;
                }
            }

            cancel.setOnClickListener {
                alertDialog?.dismiss();
            }
        }
    }
}