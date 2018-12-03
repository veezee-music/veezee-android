package cloud.veezee.android.activities

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.UserManager
import kotlinx.android.synthetic.main.activity_home_page_content.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.*
import android.widget.Button
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import cloud.veezee.android.externalComponentsAndLibs.NonSwipeableViewPager
import cloud.veezee.android.fragments.AccountFragment
import cloud.veezee.android.fragments.BrowseFragment
import cloud.veezee.android.fragments.SearchFragment
import cloud.veezee.android.utils.hideKeyboard
import cloud.veezee.android.utils.mkDirs
import kotlinx.android.synthetic.main.activity_home_page.*
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import cloud.veezee.android.Constants
import cloud.veezee.android.application.App
import cloud.veezee.android.R;

class HomePageActivity : BaseActivity() {

    private enum class Fragments {
        BROWSE,
        SEARCH,
        ACCOUNT
    }

    //props
    private val context: Context = this;
    var tabLayoutHeight: Int = 0;
    private var lastFragmentSelected: Fragments = Fragments.BROWSE;
    private var TAG = "HomePageActivity";

    private var materialSearchView: MaterialSearchView? = null;
    private var viewPagerContainer: NonSwipeableViewPager? = null;

    //callbacks
//    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//
//        if (panelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
//            collapsePanel();
//
//        when (item.itemId) {
//            ir.veezee.android.R.id.navigation_account -> {
//                replaceFragment(HomePageActivity.Fragments.ACCOUNT);
//                materialSearchView?.closeSearch();
//
//                return@OnNavigationItemSelectedListener true
//            }
//            ir.veezee.android.R.id.navigation_search -> {
//                replaceFragment(HomePageActivity.Fragments.SEARCH);
//                materialSearchView?.showSearch();
//                hideKeyboard(context);
//
//                return@OnNavigationItemSelectedListener true
//            }
//            ir.veezee.android.R.id.navigation_browse -> {
//                replaceFragment(HomePageActivity.Fragments.BROWSE);
//                materialSearchView?.closeSearch();
//
//                return@OnNavigationItemSelectedListener true
//            }
//        }
//        false
//    }

    private val materialSearchViewListeners = object : MaterialSearchView.SearchViewListener {
        override fun onSearchViewClosed() {
            viewPagerContainer?.setItem(lastFragmentSelected, false);
        }

        override fun onSearchViewShown() {
        }
    }

    private fun getFragment(index: HomePageActivity.Fragments): Fragment {

        return when (index) {
            Fragments.BROWSE -> BrowseFragment();
            Fragments.SEARCH -> SearchFragment();
            Fragments.ACCOUNT -> AccountFragment();
        }
    }

//    private fun replaceFragment(fragment: HomePageActivity.Fragments) {
//
//        viewPagerContainer?.setItem(fragment, false);
//    };

    private fun upgradeRequire() {
        val dialog = AlertDialog.Builder(context);
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade, null);

        val upgradeButton = view.findViewById<Button>(R.id.upgrade_button);

        dialog.setView(view).setCancelable(false);
        val createDialog = dialog.create();
        createDialog.show();

        upgradeButton.setOnClickListener {
            //navigation.selectedItemId = R.id.navigation_account;
            createDialog.dismiss();
        };
    }

    private fun NonSwipeableViewPager.setItem(item: Fragments, smoothScroll: Boolean = true) {
        var index = -1;

        index = when (item) {
            Fragments.BROWSE -> 0;
            Fragments.SEARCH -> 1;
            Fragments.ACCOUNT -> 2;
        };

        viewPagerContainer?.setCurrentItem(index, smoothScroll);
    }

    private fun makeTabLayout() {

        val inflater = LayoutInflater.from(context);
        for (i in 0..tab_layout?.tabCount!!) {
            val view = inflater.inflate(R.layout.item_tab_layout, null);

            when (i) {
                0 -> {
                    view.findViewById<ImageView>(R.id.tab_image).setImageResource(R.drawable.ic_music_note_24dp);
                    view.findViewById<TextView>(R.id.tab_title).text = "Browse";
                }
                1 -> {
                    view.findViewById<ImageView>(R.id.tab_image).setImageResource(R.drawable.ic_search_24dp);
                    view.findViewById<TextView>(R.id.tab_title).text = "Search";
                }
                2 -> {
                    view.findViewById<ImageView>(R.id.tab_image).setImageResource(R.drawable.ic_person_24dp);
                    view.findViewById<TextView>(R.id.tab_title).text = "Account";
                }
            }

            tab_layout?.getTabAt(i)?.customView = view;

            tab_layout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> {
                            lastFragmentSelected = Fragments.BROWSE;
                        }
                        1 -> {
                            materialSearchView?.closeSearch();
                            lastFragmentSelected = Fragments.SEARCH;
                        }
                        2 -> {
                            lastFragmentSelected = Fragments.ACCOUNT;
                        }
                    }
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab?.position == 1) {
                        materialSearchView?.showSearch();
                        hideKeyboard(context);
                    }
                }
            });
        }
    }

    // override method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setSupportActionBar(toolbar);

        myAppbarLayout = homePage_app_bar_layout;
        root = home_page_root;

        object : CountDownTimer(100, 100) {
            override fun onFinish() {
                tabLayoutHeight = tab_layout?.height!!;
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start();

        //permissions();
        makeRequiredFiles();
        requestPlayer();

        viewPagerContainer = view_pager_container;
        viewPagerContainer?.offscreenPageLimit = 3;
        viewPagerContainer?.adapter = ViewPagerAdapter(supportFragmentManager);

        materialSearchView = homePage_material_searchView;
        materialSearchView?.setOnSearchViewListener(materialSearchViewListeners);

        tab_layout?.setupWithViewPager(viewPagerContainer);
        makeTabLayout();

        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private fun makeRequiredFiles() {
        mkDirs(Constants.DIRECTORY + "/cache/images");
        mkDirs(Constants.DIRECTORY + "/cache/audio");
    }

//    private fun permissions() {
//        val permission = Permission(context);
//        // check for READ STORAGE and WRITE STORAGE permissions
//        val permissionsRequest: Array<String> = permission.arePermissionsGranted(arrayOf(Permission.READ_STORAGE, Permission.WRITE_STORAGE));
//        if (permissionsRequest.isNotEmpty()) {
//            permission.requestPermission(permissionsRequest, 0);
//        } else {
//            mkDirs(App.setting?.directory + "/cache/images");
//            mkDirs(App.setting?.directory + "/cache/audio");
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//
//        val permission = StringBuilder();
//        val constText = context.getString(ir.veezee.android.R.string.permission_deny);
//
//        (0 until grantResults.size)
//                .filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
//                .forEach { permission.append(permissions[it] + "\n\n") }
//
//        if (permission.isNotEmpty()) {
//            val dialog = AlertDialog.Builder(context);
//            dialog.setTitle(constText)
//                    .setMessage(permission.toString())
//                    .setCancelable(false)
//                    .setPositiveButton(context.getString(R.string.try_again)) { _, _ ->
//                        permissions();
//                    }
//                    .setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
//                        finish();
//                    };
//            dialog.create();
//            dialog.show();
//        } else {
//            mkDirs(App.setting?.directory + "/cache/images");
//            mkDirs(App.setting?.directory + "/cache/audio");
//        }
//    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK)
            if (panelState() != SlidingUpPanelLayout.PanelState.EXPANDED) {
                finish();
            }

        return super.onKeyDown(keyCode, event);
    }

    inner class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> getFragment(HomePageActivity.Fragments.BROWSE);
                1 -> getFragment(HomePageActivity.Fragments.SEARCH);
                2 -> getFragment(HomePageActivity.Fragments.ACCOUNT);

                else -> getFragment(HomePageActivity.Fragments.BROWSE);
            }
        }

        override fun getCount(): Int {
            return if(Constants.GUEST_MODE == true) {
                2
            } else  {
                3
            }
        }
    }
}
