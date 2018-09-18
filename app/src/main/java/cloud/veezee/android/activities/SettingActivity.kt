package cloud.veezee.android.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import cloud.veezee.android.adapters.SettingVerticalListAdapter
import cloud.veezee.android.models.SettingModel
import cloud.veezee.android.utils.Setting
import cloud.veezee.android.R
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.activity_setting_content.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper


class SettingActivity : BaseActivity() {

    private val TAG: String = "SettingActivity Console";
    private val context: Context = this;
    private var recyclerView: RecyclerView? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        navigationIcon(setting_toolbar);

        recyclerView = setting_recycler_view;

        initList();
    }

    private fun initList() {
        val adapter = SettingVerticalListAdapter(context, prepareList());
        recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView?.adapter = adapter;

        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    private fun prepareList(): ArrayList<SettingModel> {

        val models: ArrayList<SettingModel> = ArrayList();
        var model: SettingModel? = null;

        for (i in 0..2) {
            when(i) {
                0 -> {
                    model = SettingModel(SettingModel.ViewType.TEXT);
                    model.itemType = SettingModel.ItemType.THEME;
                    model.title = "Theme";
                    model.icon = R.drawable.ic_palette;
                }
                1 -> {
                    model = SettingModel(SettingModel.ViewType.SWITCH);
                    model.itemType = SettingModel.ItemType.OFFLINE_ACCESS;
                    model.title = "Offline Access";
                    model.icon = R.drawable.ic_cached;
                }
                2 -> {
                    model = SettingModel(SettingModel.ViewType.SWITCH);
                    model.itemType = SettingModel.ItemType.COLORED_PLAYER;
                    model.title = "Colored Player";
                    model.icon = R.drawable.ic_invert_colors;
                }
            }

            models.add(model!!);
        }

        return models;
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater;
        inflater.inflate(R.menu.setting_menu, menu);

        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId;

        when (id) {
            R.id.reset_default -> {
                Setting.resetSetting(context);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
