package cloud.veezee.android.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cloud.veezee.android.adapters.ChoosePlaylistVerticalAdapter
import cloud.veezee.android.api.API
import cloud.veezee.android.utils.contentReadyToShow
import cloud.veezee.android.api.utils.AppClient
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.adapters.interfaces.OnListItemClickListener
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.Track
import cloud.veezee.android.R
import cloud.veezee.android.api.add
import cloud.veezee.android.api.get
import cloud.veezee.android.api.new
import org.json.JSONObject
import java.util.*

class PlayListOptionFragment : BottomSheetDialogFragment() {

    private val TAG = "PlayListOptionFragment";

    private var bottomSheet: BottomSheetBehavior<LinearLayout>? = null;
    private var adapter: ChoosePlaylistVerticalAdapter? = null;
    private var albums: ArrayList<Album> = ArrayList();
    private var volleyRequest: AppClient? = null;
    private var trackJson: String = "";
    private var lastItemSelected = -1;
    private var listPosition = -1;

    private lateinit var list: RecyclerView;
    private lateinit var confirmButton: TextView;
    private lateinit var loading: ProgressBar;
    private lateinit var confirmLoading: ProgressBar;

    private val onConfirmClickListener = View.OnClickListener {
        if (lastItemSelected != -1) {
            //confirm

            val track = Gson().fromJson(trackJson, Track::class.java);
            val playlist = albums[listPosition];

            API.Account.PlayLists.Track.add(context!!, track, playlist, addTrackToThePlaylistVolleyResponseListener);
        } else {
            //cancel
            dismiss();
            AppClient.cancelLastRequest();
        }
    }

    private val bottomSheetCallBack = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN)
                dismiss();
        }
    }

    private val addTrackToThePlaylistVolleyResponseListener = object : HttpRequestListeners.JsonObjectResponseListener {
        override fun response(response: JSONObject) {

        }

        override fun headers(json: JSONObject) {

        }

        override fun error(er: JSONObject) {
            Log.i(TAG, er.getString(AppClient.ERROR_MESSAGE));
        }
    }

    private val getPlayListVolleyResponseListener = object : HttpRequestListeners.StringResponseListener {
        override fun response(response: String?) {

            if (albums.size != 0)
                albums.clear();
            albums.addAll(Gson().fromJson(response, object : TypeToken<ArrayList<Album>>() {}.type));

            adapter?.notifyDataSetChanged();

            (list as View).contentReadyToShow(true, loading);
        }

        override fun headers(json: JSONObject) {

        }

        override fun error(error: JSONObject?) {
            Log.i(TAG, error?.getString(AppClient.STATUS_CODE));
        }
    }

    private val onItemClickListener = object : OnListItemClickListener {
        override fun onClick(id: String, position: Int, extra: Int?) {
            if (position != 0) {
                listPosition = position - 1;

                if (lastItemSelected != position) {
                    confirmButton.text = context?.getString(R.string.confirm);
                    lastItemSelected = position;
                } else {
                    confirmButton.text = context?.getString(R.string.cancel);
                    lastItemSelected = -1;
                }
            } else {
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_create_playlist, null);
                CreatePlaylist().whit(context!!).view(view).show();
            }

        }
    }

    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style);
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_playlist_option, null);

        trackJson = arguments?.getString("trackBundle")!!;

        list = view.findViewById(R.id.playlist_option_recycler_view);
        confirmButton = view.findViewById(R.id.playlist_option_confirm);
        loading = view.findViewById(R.id.playlist_option_loading);
        confirmLoading = view.findViewById(R.id.playlist_option_confirm_loading);

        confirmButton.setOnClickListener(onConfirmClickListener);

        volleyRequest = AppClient(context!!);
        dialog?.setContentView(view);

        val params: CoordinatorLayout.LayoutParams = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams;
        val behavior: CoordinatorLayout.Behavior<View> = params.behavior!!;
        bottomSheet = (behavior as BottomSheetBehavior<LinearLayout>);
        bottomSheet?.setBottomSheetCallback(bottomSheetCallBack);

        API.Account.PlayLists.get(context!!, getPlayListVolleyResponseListener);

        initList();
    }

    private fun initList() {
        adapter = ChoosePlaylistVerticalAdapter(context!!, albums);
        list.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        adapter?.setOnItemClickListener(onItemClickListener);
        list.adapter = adapter;
    }

    inner class CreatePlaylist {

        private var dialog: AlertDialog.Builder? = null;
        private var d: AlertDialog? = null;
        private var context: Context? = null;
        private var onProcess = false;

        private lateinit var textBox: TextInputEditText;
        private lateinit var confirm: Button;
        private lateinit var cancel: Button;
        private lateinit var createPlaylistLoading: ProgressBar;

        private val volleyResponseListener = object : HttpRequestListeners.JsonObjectResponseListener {
            override fun response(response: JSONObject) {
                val album: Album = Gson().fromJson(response.toString(), Album::class.java);

                albums.add(0, album);
                adapter?.notifyDataSetChanged();

                d?.dismiss();
            }

            override fun headers(json: JSONObject) {

            }

            override fun error(er: JSONObject) {
                onProcess = false;
                createPlaylistLoading.visibility = View.INVISIBLE;
            }
        }

        fun whit(context: Context): CreatePlaylist {
            this.context = context;

            dialog = AlertDialog.Builder(context);

            return this;
        }

        fun view(view: View): CreatePlaylist {
            dialog?.setView(view);

            prepareComponent(view);

            return this;
        }

        fun show(): CreatePlaylist {

            d = dialog?.create();
            d?.show();

            return this;
        }

        private fun prepareComponent(view: View) {
            textBox = view.findViewById(R.id.create_playlist_edit_text);
            confirm = view.findViewById(R.id.create_playlist_confirm);
            cancel = view.findViewById(R.id.create_playlist_cancel);
            createPlaylistLoading = view.findViewById(R.id.create_playlist_confirm_loading);

            confirm.setOnClickListener {

                if (!onProcess) {

                    createPlaylistLoading.visibility = View.VISIBLE;

                    val title: String = textBox.text.toString();
                    API.Account.PlayLists.new(context!!, title, volleyResponseListener);

                    onProcess = true;
                }
            }

            cancel.setOnClickListener {
                d?.dismiss();
            }
        }
    }
}