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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.client.ClientProtocolException;
import org.dbartists.api.ArtistFactory;
import org.dbartists.api.Track;
import org.dbartists.api.TrackFactory;
import org.dbartists.utils.PlaylistProvider;
import org.dbartists.utils.PlaylistProvider.Items;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class TrackListAdapter extends ArrayAdapter<Track> {

	private static final String TAG = TrackListAdapter.class.getName();
	private LayoutInflater inflater;

	public TrackListAdapter(Context context) {
		super(context, R.layout.track_item);
		inflater = LayoutInflater.from(getContext());
	}

	private List<Track> moreTracks;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (moreTracks != null) {
				for (Track s : moreTracks) {
					if (getPosition(s) < 0) {
						add(s);
					}
				}
			}
		}
	};

	public void refresh() {
		clear();
		if (moreTracks != null) {
			for (Track s : moreTracks) {
				if (getPosition(s) < 0) {
					add(s);
				}
			}
		}
	}

	private boolean isDownloaded(Track track) {
		// TODO: implement download manager
		return false;
	}

	private boolean isPlaying(String name) {

		String selection = PlaylistProvider.Items.NAME + " = ? and "
				+ PlaylistProvider.Items.IS_PLAYING + " = ?";
		String[] selectionArgs = new String[2];
		selectionArgs[0] = name;
		selectionArgs[1] = "1";
		String sortOrder = PlaylistProvider.Items.PLAY_ORDER + " asc";

		Cursor c = getContext().getContentResolver().query(
				PlaylistProvider.CONTENT_URI, null, selection, selectionArgs,
				sortOrder);
		if (c.moveToFirst()) {
			c.close();
			return true;
		}
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.track_item, parent, false);
		}

		Track track = getItem(position);

		ImageView image = (ImageView) convertView
				.findViewById(R.id.TrackItemStatusImage);
		TextView name = (TextView) convertView
				.findViewById(R.id.TrackItemNameText);

		ProgressBar titleProgressBar;
		titleProgressBar = (ProgressBar) parent.getRootView().findViewById(
				R.id.leadProgressBar);
		// hide the progress bar if it is not needed
		titleProgressBar.setVisibility(ProgressBar.GONE);

		if (track != null) {
			// image.setImageDrawable(getContext().getResources().getDrawable(
			// isPlayable(story) ? R.drawable.icon_listen_main
			// : R.drawable.bullet));
			if (isPlaying(track.getName()))
				image.setImageResource(R.drawable.icon_listen_main);
			else
				image.setImageResource(R.drawable.icon);
			image.setVisibility(View.VISIBLE);
			name.setText(track.getName());
		}
		return convertView;
	}

	public void addMoreTracks(final String url, final int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				getMoreTracks(url, startId);
				handler.sendEmptyMessage(0);
			}
		}).start();
	}

	private void getMoreTracks(String url, int startId) {
		moreTracks = TrackFactory.downloadTracks(url, startId);
		if (moreTracks != null) {
			TrackListActivity.addAllToTrackCache(moreTracks);
		}
	}
}
