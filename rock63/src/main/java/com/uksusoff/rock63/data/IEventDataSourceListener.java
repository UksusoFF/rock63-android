package com.uksusoff.rock63.data;

public interface IEventDataSourceListener {
    void eventsRefreshed(DataSource source);
    void eventsRefreshFailed(DataSource source);
}
