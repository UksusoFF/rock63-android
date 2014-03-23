package com.uksusoff.rock63;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionbarsherlock.app.SherlockListFragment;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EFragment;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.INewsDataSourceListener;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.utils.CommonUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

@EFragment
public class NewsView extends SherlockListFragment implements INewsDataSourceListener, IRefreshableFragment {

    @Bean
	DataSource source;
	
	private View progressWrap;
	private ProgressBar progress;
	//private List<NewsItem> mNews;
		
	@AfterViews
	void init() {
	    
	    source.setNewsListener(this);
        
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        progressWrap = inflater.inflate(R.layout.listview_header_progress, null);
        progress = (ProgressBar)progressWrap.findViewById(R.id.header_progress_bar);
        progress.setVisibility(View.GONE);
        
        setEmptyText(getActivity().getText(R.string.news_no_item_text));

        // We have a menu item to show in action bar.
        setHasOptionsMenu(false);
        
        if (getListView().getAdapter()==null)
            getListView().addHeaderView(progressWrap);
        else {
            ListAdapter la = getListView().getAdapter();
            getListView().setAdapter(null);
            getListView().addHeaderView(progressWrap);
            getListView().setAdapter(la);
        }
                        
        refreshNews();
        
	}
	
	@Override
    public void onDestroy() {
	    
	    source.setNewsListener(null);
	    
	    super.onDestroy();
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	    super.onListItemClick(l, v, position, id);
	    NewsItem item = (NewsItem)((Map<String, Object>)l.getAdapter().getItem(position)).get("obj");
	    NewsDetailActivity_.intent(getActivity()).newsItemId(item.getId()).start();
	}
	
	@Override
	public void newsRefreshed(DataSource lSource) {
	    loadNewsFromDataSource(lSource);
	    if (this.getView()!=null) {
    	    setListShown(true);
    	    progress.setVisibility(View.GONE);
	    }
	}

	@Override
	public void newsRefreshFailed(DataSource source) {
	}
	
    @Override
    public void onRefresh() {
        refreshNews();
    }
    
    public void refreshNews() {        
        getListView().scrollTo(getListView().getScrollX(), 0);
        progress.setVisibility(View.VISIBLE);
        source.refreshNews();
    }
    
    public void loadNewsFromDataSource(DataSource lSource) {
        List<NewsItem> news = null;
        try {
            news = lSource.getAllNews();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        for (int i = 0; i<news.size(); i++) {
            NewsItem item = news.get(i);
            Map<String, Object> datum = new HashMap<String, Object>(3);
            datum.put("title", item.getTitle());
            datum.put("text", CommonUtils.getCroppedString(CommonUtils.getTextFromHtml(item.getBody()), 50, true));
            datum.put("imageUrl", item.getSmallThumbUrl());
            datum.put("obj", item);
            
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), data,
                                                  com.uksusoff.rock63.R.layout.news_item_view,
                                                  new String[] {"title", "text", "imageUrl" },
                                                  new int[] { com.uksusoff.rock63.R.id.newsTitle,
                                                              com.uksusoff.rock63.R.id.newsDescription,
                                                              com.uksusoff.rock63.R.id.newsImageView });
        
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,String textRepresentation) 
            {
                if((view instanceof ImageView) && (data instanceof String)) 
                {
                    UrlImageViewHelper.setUrlDrawable((ImageView) view, (String)data, R.drawable.news_no_image);
                    
                    return true;
                }
                return false;
            }
        });
        
        this.setListAdapter(adapter);
        
    }

}
