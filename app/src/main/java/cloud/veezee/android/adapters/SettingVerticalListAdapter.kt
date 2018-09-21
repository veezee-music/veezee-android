package cloud.veezee.android.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import cloud.veezee.android.Constants
import cloud.veezee.android.R
import cloud.veezee.android.activities.SettingActivity
import cloud.veezee.android.application.App
import cloud.veezee.android.fragments.ThemeFragment
import cloud.veezee.android.models.SettingModel

class SettingVerticalListAdapter(private val context: Context, private val list: ArrayList<SettingModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context);

    @SuppressLint("WrongConstant")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var title: TextView? = null;
        val container: ConstraintLayout;
        var icon: ImageView? = null;
        var model: SettingModel? = null;

        when (holder) {
            is SimpleTextTypeViewHolder -> {
                model = list[position];

                title = holder.title;
                icon = holder.icon;
                container = holder.container;

                if (model.itemType == SettingModel.ItemType.THEME) {
                    container.setOnClickListener {
                        val themeSheet: BottomSheetDialogFragment = ThemeFragment();
                        themeSheet.show((context as SettingActivity).supportFragmentManager, themeSheet.tag);
                    };
                }
            }
            is SwitchTypeViewHolder -> {
                model = list[position];

                icon = holder.icon;
                container = holder.container;

                val toggleSwitch = holder.switch;
                title = toggleSwitch;

                if (model.itemType == SettingModel.ItemType.COLORED_PLAYER) {
                    toggleSwitch.isChecked = Constants.COLORED_PLAYER;

                } else if (model.itemType == SettingModel.ItemType.OFFLINE_ACCESS) {
                    toggleSwitch.isChecked = Constants.OFFLINE_ACCESS;
                }

                toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (model.itemType == SettingModel.ItemType.COLORED_PLAYER) {
                        Constants.COLORED_PLAYER = isChecked;

                        val coloredPlayerChanged: Intent = Intent(Constants.SETTINGS_CHANGED_NOTIFICATION_ID);
                        coloredPlayerChanged.flags = SettingModel.COLORED_PLAYER_CHANGED;
                        LocalBroadcastManager.getInstance(context).sendBroadcast(coloredPlayerChanged);

                    } else if (model.itemType == SettingModel.ItemType.OFFLINE_ACCESS) {
                        Constants.OFFLINE_ACCESS = isChecked;
                    }
                };
            }
        }

        title?.text = model?.title;
        icon?.setImageResource(model?.icon!!);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View;
        val viewHolder: RecyclerView.ViewHolder?;

        when (viewType) {
            0 -> {
                view = inflater.inflate(R.layout.item_setting_text_type, parent, false);
                viewHolder = SimpleTextTypeViewHolder(view);
            }
            1 -> {
                view = inflater.inflate(R.layout.item_setting_switch_type, parent, false);
                viewHolder = SwitchTypeViewHolder(view);
            }

            else -> {
                viewHolder = null;
            };
        }

        return viewHolder!!;
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position].viewType) {
            SettingModel.ViewType.TEXT -> 0;
            SettingModel.ViewType.SWITCH -> 1;
        }
    }

    override fun getItemCount(): Int = list.size;

    inner class SimpleTextTypeViewHolder(itemType: View) : RecyclerView.ViewHolder(itemType) {
        val container: ConstraintLayout = itemType.findViewById(R.id.setting_item_type_text_container)!!;
        val title: TextView = itemType.findViewById(R.id.setting_item_type_text_text_view)!!;
        val icon: ImageView = itemType.findViewById(R.id.setting_item_type_text_icon)!!;
    }

    inner class SwitchTypeViewHolder(itemType: View) : RecyclerView.ViewHolder(itemType) {
        val container: ConstraintLayout = itemType.findViewById(R.id.setting_item_type_switch_container)!!;
        //val title: TextView = itemType?.findViewById(R.id.setting_item_type_switch_text_view)!!;
        val icon: ImageView = itemType.findViewById(R.id.setting_item_type_switch_icon)!!;
        val switch: Switch = itemType.findViewById(R.id.setting_item_type_switch_switch)!!;
    }
}