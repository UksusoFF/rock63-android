package com.uksusoff.rock63;

import java.util.ArrayList;

import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.uksusoff.rock63.R;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseFragmentActivity implements OnQueryTextListener, TabHost.OnTabChangeListener  {
    
	private TabHost mTabHost;
	private ViewPager  mViewPager;
	private TabsAdapter mTabsAdapter;
	private SearchView mSearchView;
        
    private String currentTab;
    
    @Pref
    ISharedPrefs_ sharedPrefs;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
	    	    
	    String theme = sharedPrefs.theme().get();

        if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_DARK)) {
            setTheme(R.style.AppDarkTheme);
        } else if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_LIGHT)) {
            setTheme(R.style.AppLightTheme);
        }
	            
        super.onCreate(savedInstanceState);
        
    }
	
	@AfterViews
	void init() {
	    mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        mViewPager = (ViewPager)findViewById(R.id.pager);
        
        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
        
        mTabsAdapter.setTabListener(this);

        mTabsAdapter.addTab(mTabHost.newTabSpec("news").setIndicator(getText(R.string.tab_name_news)),
                NewsView_.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec("events").setIndicator(getText(R.string.tab_name_events)),
                EventsView_.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec("radio").setIndicator(getText(R.string.tab_name_radio)),
                RadioPlayerView_.class, null);

        /*if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }*/
        
        currentTab = "news";
	}

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }*/

    /**
     * This is a helper class that implements a generic mechanism for
     * associating fragments with the tabs in a tab host.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between fragments.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabManager supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct fragment shown in a separate content area
     * whenever the selected tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter 
    		implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    	private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
        
        private TabHost.OnTabChangeListener tabListener;

        public TabHost.OnTabChangeListener getTabListener() {
            return tabListener;
        }

        public void setTabListener(TabHost.OnTabChangeListener tabListener) {
            this.tabListener = tabListener;
        }

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));

            TabInfo info = new TabInfo(clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        public Fragment getCurrentItem() {
            return mFragments.get(mTabHost.getCurrentTab());
        }
        
        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            Fragment f = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            mFragments.add(position, f);
            return f;
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
            
            tabListener.onTabChanged(tabId);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		
		mSearchView = (SearchView) menu.findItem( R.id.menu_search ).getActionView();
        
        mSearchView.setOnQueryTextListener(this);
		
		if (!currentTab.equalsIgnoreCase("events")) {
		    ((SearchView) menu.findItem( R.id.menu_search ).getActionView()).setVisibility(View.GONE);
		}
				
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    
	    switch (item.getItemId()) {
	    case R.id.menuRefresh:
	        
	        if (mTabsAdapter.getCurrentItem() instanceof IRefreshableFragment)
	            ((IRefreshableFragment)mTabsAdapter.getCurrentItem()).onRefresh();
	        	        
	        break;
	    case R.id.menuPreferences:
	        
	        SettingsActivity_.intent(this).start();
	        
	        break;
	    }
		
		return super.onOptionsItemSelected(item);
	}

    @Override
    public boolean onQueryTextChange(String newText) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (mTabsAdapter.getCurrentItem() instanceof ISearchableFragment) {
            ((ISearchableFragment)mTabsAdapter.getCurrentItem()).onSearch(query);
            mSearchView.clearFocus();
            return true;
        }
        
        return false;
    }

    @Override
    public void onTabChanged(String tabId) {
        
        currentTab = tabId;
        
        getSherlock().dispatchInvalidateOptionsMenu();
        
        /*if (tabId.equalsIgnoreCase("news")) {
            
        } else if (tabId.equalsIgnoreCase("events")) {
            
        } else if (tabId.equalsIgnoreCase("radio")) {
            
        }*/
    }

}
