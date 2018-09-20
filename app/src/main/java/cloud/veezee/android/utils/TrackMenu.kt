package cloud.veezee.android.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import cloud.veezee.android.utils.interfaces.OnDialogButtonsClickListener
import cloud.veezee.android.models.Track
import cloud.veezee.android.R
import cloud.veezee.android.activities.AlbumActivity
import cloud.veezee.android.application.GlideApp
import cloud.veezee.android.models.Album
import com.google.gson.Gson

class TrackMenu {

    private var artwork: ImageView? = null;
    private var blurredArtwork: ImageView? = null;
    private var add: Button? = null;
    private var cancel: Button? = null;
    private var title: TextView? = null;
    private var albumTitle: TextView? = null;
    private var artist: TextView? = null;
    private var dialogMenuChevron: ImageView? = null;

    private lateinit var dialog: AlertDialog.Builder;
    private lateinit var d: AlertDialog;
    private lateinit var context: Context;
    private lateinit var track: Track;
    private lateinit var listener: OnDialogButtonsClickListener;


    private fun vibrate(context: Context) {
        val v: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        v.vibrate(40);
        //view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    fun with(context: Context): TrackMenu {
        this.context = context;
        dialog = AlertDialog.Builder(context);

        vibrate(context);

        return this;
    }

    fun view(view: View): TrackMenu {
        dialog.setView(view);
        d = dialog.create();

        prepareComponents(view);

        return this;
    }

    fun setOnButtonsClickListener(listener: OnDialogButtonsClickListener): TrackMenu {
        this.listener = listener;

        return this;
    }

    fun transparentBackground(): TrackMenu {
        d.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        return this;
    }

    fun gravity(gravity: Int): TrackMenu {
        val windowManager: WindowManager.LayoutParams = d.window.attributes;
        windowManager.gravity = gravity
        windowManager.y = 50;

        return this;
    }

    fun content(track: Track): TrackMenu {
        this.track = track;

        title?.text = track.title;
        albumTitle?.text = track.album?.title;
        artist?.text = track.album?.artist?.name;

        GlideApp.with(context).asBitmap().load(track.image).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                artwork?.setImageBitmap(resource);

                title?.visibility = View.VISIBLE;
                albumTitle?.visibility = View.VISIBLE;
                artist?.visibility = View.VISIBLE;

                Handler().postDelayed(Runnable {
                    val drawable: Drawable = ImageUtils.createBlurredImageFromBitmap(resource, context, 11);
                    blurredArtwork?.setImageDrawable(drawable);
                    blurredArtwork?.animate()?.alpha(1f)?.setDuration(300)?.start();
                }, 100);
            }
        });

        return this;
    }

    fun showAlbumPage(album: Album) {
        val albumActivity = Intent(context, AlbumActivity::class.java);
        albumActivity.putExtra("album", Gson().toJson(album));
        albumActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        context.startActivity(albumActivity);
    }

    fun show() {
        d.show();
    }

    private fun prepareComponents(view: View) {
        artwork = view.findViewById(R.id.dialog_menu_artwork);
        blurredArtwork = view.findViewById(R.id.dialog_menu_blurred_artwork);
        add = view.findViewById(R.id.dialog_menu_add);
        cancel = view.findViewById(R.id.dialog_menu_cancel);
        title = view.findViewById(R.id.dialog_menu_title);
        albumTitle = view.findViewById(R.id.dialog_menu_album_title);
        artist = view.findViewById(R.id.dialog_menu_artist);
        dialogMenuChevron = view.findViewById(R.id.dialog_menu_chevron);

        add?.setOnClickListener {
            listener.addClickListener(d);
        };

        cancel?.setOnClickListener {
            listener.cancelCLickListener(d);
        }

        artwork?.setOnClickListener {
            if(track.album != null) {
                showAlbumPage(track.album!!);
            }
        }

        dialogMenuChevron?.setOnClickListener {
            if(track.album != null) {
                showAlbumPage(track.album!!);
            }
        }

        artwork?.clipToOutline = true;
        blurredArtwork?.clipToOutline = true;
    }
}