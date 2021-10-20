package sun.bob.leela.ui.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import sun.bob.leela.R;
import sun.bob.leela.db.AccountHelper;
import sun.bob.leela.db.Category;
import sun.bob.leela.db.CategoryHelper;
import sun.bob.leela.ui.fragments.AcctListFragment;
import sun.bob.leela.utils.AppConstants;
import sun.bob.leela.utils.ResUtil;
import sun.bob.leela.utils.UserDefault;

/**
 * Created by bob.sun on 16/3/19.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private AcctListFragment acctListFragment;
    private AcctListFragment currentFragment;
    private Toolbar toolbar;
    private SearchView mSearchView;     //  输入框对象
    private HashMap<Long, AcctListFragment> fragments;
    private SubMenu categoriesMenu;
    private MenuItem lastChecked;
    private NavigationView navigationView;
    private ImageView headerImageView;
    private TextView headerTextView;
    private String searchKeyWord;
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        verifyStoragePermissions(this);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragments = new HashMap<>();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View reveal = findViewById(R.id.reveal_background);
                // get the center for the clipping circle
                int centerX = (fab.getLeft() + fab.getRight()) / 2;
                int centerY = (fab.getTop() + fab.getBottom()) / 2;

                int startRadius = 0;
                // get the final radius for the clipping circle
                int endRadius = Math.max(reveal.getWidth(), reveal.getHeight());

                // create the animator for this view (the start radius is zero)
                SupportAnimator anim =
                        ViewAnimationUtils.createCircularReveal(reveal, centerX, centerY, startRadius, endRadius);

                // make the view visible and start the animation
                reveal.setVisibility(View.VISIBLE);
                anim.start();
                anim.addListener(new SupportAnimator.AnimatorListener() {
                    @Override
                    public void onAnimationStart() {

                    }

                    @Override
                    public void onAnimationEnd() {
                        Intent intent = new Intent(MainActivity.this, AddAccountActivity.class);
                        intent.putExtra("showMode", AddAccountActivity.AddAccountShowMode.ShowModeAdd);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
//                        reveal.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel() {

                    }

                    @Override
                    public void onAnimationRepeat() {

                    }
                });
                mSearchView.clearFocus();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        headerImageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.header_image_view);
        headerTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.header_category);

        categoriesMenu = navigationView.getMenu().addSubMenu(R.string.categories);

        loadCategoriesInNavigation();

        acctListFragment = AcctListFragment.newInstance(AppConstants.CAT_ID_ALL, "");
        fragments.put(AppConstants.CAT_ID_ALL, acctListFragment);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.id_fragment_container, acctListFragment, "AcctListFragment")
                .commit();
        currentFragment = acctListFragment;

        loadAccountByCategory(CategoryHelper.getInstance(this).getCategoryById(AppConstants.CAT_ID_ALL),"");


        if (!AccountHelper.getInstance(this).hasMasterPassword()) {
            Intent intent = new Intent(this, SetMasterPasswordActivity.class);
            intent.putExtra("showMode", SetMasterPasswordActivity.ShowMode.ShowModeAdd);
            startActivity(intent);
        }
//        if (UserDefault.getInstance(this).hasQuickPassword()) {
//            Intent intent = new Intent(this, SetQuickPasswordActivity.class);
//            intent.putExtra("type", SetQuickPasswordActivity.ShowTypeVerify);
//            this.startActivity(intent);
//
//        } else {
//            Intent intent = new Intent(this, AuthorizeActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//            this.getApplicationContext().startActivity(intent);
//        }
    }

    private void loadCategoriesInNavigation() {
        categoriesMenu.clear();
        for (Category category : CategoryHelper.getInstance(null).getAllCategory()) {
            try {
                categoriesMenu.add(category.getName()).setIcon(new BitmapDrawable(getResources(),
                        ResUtil.getInstance(null).getBmp(category.getIcon())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSearchView != null) mSearchView.clearFocus();
        findViewById(R.id.reveal_background).setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView mSearchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        MainActivity.this.mSearchView = mSearchView;
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(searchKeyWord);
                mSearchView.clearFocus();
                return false;
            }
            // 当搜索内容改变时触发该方法，时刻监听输入搜索框的值
            @Override
            public boolean onQueryTextChange(String newText) {
                searchKeyWord = newText;
                search(searchKeyWord);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_generator) {
            startActivity(new Intent(MainActivity.this, PasswordGenActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Category category = CategoryHelper.getInstance(getApplicationContext())
                .getCategoryByName(String.valueOf(item.getTitle()));
        this.item = item;
        loadAccountByCategory(category,searchKeyWord);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (headerImageView != null)
            Picasso.with(this).load(ResUtil.getInstance(null).getBmpUri(category.getIcon()))
                    .config(Bitmap.Config.RGB_565)
                    .fit()
                    .into(headerImageView);
        if (headerTextView != null)
            headerTextView.setText(category.getName());
        drawer.closeDrawer(GravityCompat.START);
        if (lastChecked != null)
            lastChecked.setChecked(false);
        item.setChecked(true);
        lastChecked = item;
        return true;
    }

    private void loadAccountByCategory(Category category, String searchKeyWord) {
        AcctListFragment toShow = fragments.get(category.getId());

        toShow = acctListFragment.newInstance(category.getId(),searchKeyWord);
        fragments.put(category.getId(), toShow);
        getSupportFragmentManager().beginTransaction()
                    .hide(currentFragment)
                    .add(R.id.id_fragment_container, toShow, category.getName())
                    .commit();
        System.gc();

        getSupportActionBar().setTitle(category.getName());
        currentFragment = toShow;
    }

    private void search(String searchKeyWord){
            if(item == null)
                loadAccountByCategory(CategoryHelper.getInstance(MainActivity.this).getCategoryById(AppConstants.CAT_ID_ALL),searchKeyWord);
            else{
                Category category = CategoryHelper.getInstance(getApplicationContext())
                        .getCategoryByName(String.valueOf(item.getTitle()));
                loadAccountByCategory(category,searchKeyWord);
            }
    }



    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
