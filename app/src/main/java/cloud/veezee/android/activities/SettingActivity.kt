package cloud.veezee.android.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import cloud.veezee.android.adapters.SettingVerticalListAdapter
import cloud.veezee.android.models.SettingModel
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

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
            val model1 = SettingModel(SettingModel.ViewType.TEXT);
            model1.itemType = SettingModel.ItemType.THEME;
            model1.title = "Theme";
            model1.icon = R.drawable.ic_palette;
            models.add(model1);
        }

        val model2 = SettingModel(SettingModel.ViewType.SWITCH);
        model2.itemType = SettingModel.ItemType.OFFLINE_ACCESS;
        model2.title = "Offline Access";
        model2.icon = R.drawable.ic_cached;
        models.add(model2);

        val model3 = SettingModel(SettingModel.ViewType.SWITCH);
        model3.itemType = SettingModel.ItemType.COLORED_PLAYER;
        model3.title = "Colored Player";
        model3.icon = R.drawable.ic_invert_colors;
        models.add(model3);

        return models;
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater = menuInflater;
//        inflater.inflate(R.menu.setting_menu, menu);
//
//        return true;
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        val id = item?.itemId;
//
//        when (id) {
//            R.id.reset_default -> {
//                Setting.resetSetting(context);
//            }
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
