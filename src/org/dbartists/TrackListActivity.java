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
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbartists.api.Artist;
import org.dbartists.api.Track;
import org.dbartists.utils.PlaylistEntry;
import org.dbartists.utils.PlaylistProvider;

public class TrackListActivity extends PlayerActivity implements
		OnItemClickListener {

	private final static String TAG = "TrackListActivity";
	private String apiUrl = Constants.ARTIST_MP3_API_URL;
	private String artistUrl;
	private String artistName;
	private String artistImg;

	protected TrackListAdapter listAdapter;

	private static Map<Integer, Track> trackCache = new HashMap<Integer, Track>();

	public static Track getTrackFromCache(int storyId) {
		Track result = trackCache.get(storyId);
		if (result == null) {
			// result = Story.StoryFactory.downloadStory(storyId);
			// storyCache.put(storyId, result);
		}
		return result;
	}

	public static void addAllToTrackCache(List<Track> tracks) {
		for (Track track : tracks) {
			trackCache.put(track.getId(), track);
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		trackCache.clear();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		artistName = getIntent().getStringExtra(Constants.EXTRA_ARTIST_NAME);
		artistUrl = getIntent().getStringExtra(Constants.EXTRA_ARTIST_URL);
		artistImg = getIntent().getStringExtra(Constants.EXTRA_ARTIST_IMG);

		super.onCreate(savedInstanceState);

		ViewGroup container = (ViewGroup) findViewById(R.id.Content);
		ViewGroup.inflate(this, R.layout.items, container);

		ListView listView = (ListView) findViewById(R.id.ListView01);
		listView.setOnItemClickListener(this);
		listAdapter = new TrackListAdapter(TrackListActivity.this);
		listView.setAdapter(listAdapter);

		addTracks();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Track s = (Track) parent.getAdapter().getItem(position);
		playTrack(s, true);
	}

	private void addTracks() {
		String trackUrl = apiUrl + "?url=" + artistUrl;
		listAdapter.addMoreTracks(trackUrl, 0);
	}

	@Override
	public CharSequence getMainTitle() {
		return artistName;
	}

	private void playTrack(Track track, boolean playNow) {
		Log.d(TAG, "play now: " + track.getUrl());
		PlaylistEntry entry = new PlaylistEntry(-1, track.getUrl(),
				track.getName(), true, -1, new Artist(artistName, artistImg,
						artistUrl));
		if (playNow) {
			this.listen(entry);
		}

		List<PlaylistEntry> entries = new ArrayList<PlaylistEntry>();
		for (Track t : trackCache.values()) {
			if (t == track)
				continue;
			if (!existInPlaylist(t.getName(), true)) {
				PlaylistEntry e = new PlaylistEntry(-1, t.getUrl(),
						t.getName(), true, -1, new Artist(artistName, artistImg,
								artistUrl));
				entries.add(e);
			}
		}

		if (entries.size() > 0)
			this.addToPlayList(entries);

	}

	private boolean existInPlaylist(String name, boolean next) {
		String selection = PlaylistProvider.Items.IS_READ + " = ?" + " and "
				+ PlaylistProvider.Items.NAME + " = ?";
		String[] selectionArgs = new String[2];
		selectionArgs[0] = "0";
		selectionArgs[1] = name;
		String sort = PlaylistProvider.Items.PLAY_ORDER
				+ (next ? " asc" : " desc");
		return retrievePlaylistItem(selection, selectionArgs, sort);
	}

	private boolean retrievePlaylistItem(String selection,
			String[] selectionArgs, String sort) {
		Cursor cursor = getContentResolver().query(
				PlaylistProvider.CONTENT_URI, null, selection, selectionArgs,
				sort);
		if (cursor.moveToFirst())
			return true;
		else
			return false;
	}

	@Override
	public boolean isRefreshable() {
		return true;
	}

	@Override
	public void refresh() {
		listAdapter.clear();
		addTracks();
	}
}
