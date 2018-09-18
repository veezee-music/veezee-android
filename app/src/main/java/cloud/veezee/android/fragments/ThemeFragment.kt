package cloud.veezee.android.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import cloud.veezee.android.application.App
import cloud.veezee.android.models.SettingModel
import cloud.veezee.android.utils.Setting
import cloud.veezee.android.R

class ThemeFragment : BottomSheetDialogFragment() {

    private val TAG = "ThemeFragment";

    private var bottomSheet: BottomSheetBehavior<LinearLayout>? = null;
    private var radioGroup: RadioGroup? = null;

    private val bottomSheetCallBack = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN)
                dismiss();
        }
    }

    private fun setTheme(theme: Setting.Theme) {
        val s2 = App.setting;

        s2?.theme = theme;
        s2?.save(context);

        recreateActivities();
    }


    private fun recreateActivities() {

        val recreateRequest: Intent = Intent(Setting.SETTING_NOTIFICATION);
        recreateRequest.flags = SettingModel.THEME_CHANGED;
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(recreateRequest);
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style);
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_theme, null);

        radioGroup = view.findViewById(R.id.theme_radio_group);
        val radioBlack: RadioButton = view.findViewById(R.id.radio_button_black);
        val radioWhite: RadioButton = view.findViewById(R.id.radio_button_white);
        val radioPurpleDark: RadioButton = view.findViewById(R.id.radio_button_purple_dark);

        radioBlack.isChecked = true;

        val theme: Setting.Theme = App.setting!!.theme!!;

        when(theme) {
            Setting.Theme.WHITE -> radioWhite.isChecked = true;
            Setting.Theme.BLACK -> radioBlack.isChecked = true;
            Setting.Theme.PURPLE_DARK -> radioPurpleDark.isChecked = true;
        }

        radioGroup?.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                radioBlack.id -> {
                    setTheme(Setting.Theme.BLACK);
                }
                radioPurpleDark.id -> {
                    setTheme(Setting.Theme.PURPLE_DARK);
                }
                radioWhite.id -> {
                    setTheme(Setting.Theme.WHITE);
                }
            }
        }

        dialog?.setContentView(view);

        val params: CoordinatorLayout.LayoutParams = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams;
        val behavior: CoordinatorLayout.Behavior<View>? = params.behavior;

        if (behavior != null) {
            bottomSheet = (behavior as BottomSheetBehavior<LinearLayout>);
            bottomSheet?.setBottomSheetCallback(bottomSheetCallBack);
        }

    }
}