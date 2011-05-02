// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.dbartists;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbartists.api.Artist;
import org.dbartists.api.ArtistFactory;
import org.dbartists.utils.Tracker;

public class TopArtistsListActivity extends PlayerActivity implements
		OnItemClickListener {

	private final static String TAG = "TopArtistsListActivity";
	private String apiUrl = Constants.TOP_ARTIST_API_URL;
	private String description;

	protected TopArtistsListAdapter listAdapter;

	private static Map<String, Artist> artistsCache = new HashMap<String, Artist>();

	public static Artist getArtistFromCache(String artistId) {
		Artist result = artistsCache.get(artistId);
		if (result == null) {

		}
		return result;
	}

	public static void addAllToArtistCache(List<Artist> artists) {
		for (Artist artist : artists) {
			artistsCache.put(artist.getName(), artist);
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		artistsCache.clear();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		int type = getIntent().getIntExtra(Constants.EXTRA_SUBACTIVITY_ID, 1);
		description = getString(type);

		switch (type) {
		case R.string.msg_main_subactivity_pop:
			apiUrl = Constants.POP_ARTIST_API_URL;
			break;
		case R.string.msg_main_subactivity_top:
			apiUrl = Constants.TOP_ARTIST_API_URL;
			break;
		}

		super.onCreate(savedInstanceState);
		ViewGroup container = (ViewGroup) findViewById(R.id.Content);
		ViewGroup.inflate(this, R.layout.items, container);

		ListView listView = (ListView) findViewById(R.id.ListView01);
		listView.setOnItemClickListener(this);
		listAdapter = new TopArtistsListAdapter(TopArtistsListActivity.this);
		listView.setAdapter(listAdapter);

		addArtists();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Artist s = (Artist) parent.getAdapter().getItem(position);
		Intent i = new Intent(this, TrackListActivity.class);
		i.putExtra(Constants.EXTRA_ARTIST_NAME, s.getName());
		i.putExtra(Constants.EXTRA_ARTIST_IMG, s.getImg());
		i.putExtra(Constants.EXTRA_ARTIST_URL, s.getUrl());
		startActivityWithoutAnimation(i);
	}

	private void addArtists() {

		listAdapter.addMoreArtists(apiUrl, 0);

	}

	@Override
	public CharSequence getMainTitle() {
		Log.d(TAG, description);
		return description;
	}

	@Override
	public void trackNow() {
		StringBuilder pageName = new StringBuilder("News")
				.append(Tracker.PAGE_NAME_SEPARATOR);
		pageName.append(description);
		// Tracker.instance(getApplication()).trackPage(
		// new ArtistListMeasurement(pageName.toString(), "News", topicId));
	}

	@Override
	public boolean isRefreshable() {
		return true;
	}

	@Override
	public void refresh() {
		listAdapter.clear();
		addArtists();
	}
}
