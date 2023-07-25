package com.uksusoff.rock63.ui;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.exceptions.NoContentException;
import com.uksusoff.rock63.exceptions.NoInternetException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.a_list)
public abstract class ItemListActivity extends BaseMenuActivity {

    @ViewById(R.id.list)
    ListView list;

    @ViewById(R.id.refresh)
    SwipeRefreshLayout refresh;

    @ViewById(R.id.list_empty)
    TextView emptyView;

    protected abstract boolean isRefreshing();

    protected abstract void setRefreshing(boolean refreshing);

    protected abstract ItemListActivity getActiveActivity();

    protected abstract void setActiveActivity(ItemListActivity activity);

    protected abstract ListAdapter createAdapterFromStorageItems();

    protected abstract void refreshItemStorage() throws NoInternetException, NoContentException;

    protected abstract int getEmptyListTextResId();

    @Override
    protected void init() {
        super.init();
        setActiveActivity(this);

        emptyView.setText(getEmptyListTextResId());

        loadNewsFromDatabase();

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        refresh.post(new Runnable() {
            @Override
            public void run() {
                if (isRefreshing()) {
                    setRefreshIndicatorActive(true);
                } else if (list.getAdapter().isEmpty()) {
                    refreshList();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getActiveActivity() == this) {
            setActiveActivity(null);
        }
    }

    public void refreshList() {
        setRefreshIndicatorActive(true);
        reloadAll();
    }

    @Background
    void reloadAll() {
        try {
            setRefreshing(true);
            refreshItemStorage();
            getActiveActivity().loadNewsFromDatabase();
        } catch (NoInternetException | NoContentException e) {
            showWarning(R.string.error_no_internet);
        } finally {
            setRefreshing(false);
        }
        setRefreshIndicatorActive(false);
    }

    @UiThread
    void setRefreshIndicatorActive(boolean active) {
        refresh.setRefreshing(active);
    }

    @UiThread
    void loadNewsFromDatabase() {

        list.setAdapter(createAdapterFromStorageItems());

        if (list.getAdapter().isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

}
