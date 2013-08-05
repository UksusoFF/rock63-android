package com.uksusoff.rock63.data;

public interface INewsDataSourceListener {
	void newsRefreshed(DataSource source);
	void newsRefreshFailed(DataSource source);
	//void placesRefreshed(DataSource source);
	//void placesRefreshFailed(DataSource source);
}
