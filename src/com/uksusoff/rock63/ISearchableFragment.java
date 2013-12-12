package com.uksusoff.rock63;

public interface ISearchableFragment {
    public void onSearch(String query);
    public void onFilter(String query);
    public void onClearFilter();
}
