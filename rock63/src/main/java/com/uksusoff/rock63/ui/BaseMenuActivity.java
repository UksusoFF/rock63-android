package com.uksusoff.rock63.ui;

import androidx.annotation.IdRes;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.ViewStub;
import android.view.MenuItem;

import com.uksusoff.rock63.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by User on 13.05.2016.
 */
@EActivity
public abstract class BaseMenuActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @ViewById(R.id.drawer_layout)
    DrawerLayout drawer;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.nav_view)
    NavigationView navigationView;

    @AfterViews
    protected void init() {

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    public void setContentView(@IdRes int layoutResID) {
        super.setContentView(R.layout.a_base_with_menu);
        ViewStub stub = ((ViewStub)findViewById(R.id.content_stub));
        stub.setLayoutResource(layoutResID);
        stub.inflate();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_news:
                NewsListActivity_.intent(this).start();
                break;
            case R.id.menu_events:
                EventsListActivity_.intent(this).start();
                break;
            case R.id.menu_info:
                SettingsActivity_.intent(this).start();
                break;
            case R.id.menu_radio_vz:
                RadioPlayerActivity_.intent(this).start();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
