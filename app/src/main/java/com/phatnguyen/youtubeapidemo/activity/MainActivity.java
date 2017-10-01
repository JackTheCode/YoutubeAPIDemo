package com.phatnguyen.youtubeapidemo.activity;

import com.phatnguyen.youtubeapidemo.Utils.Constant;
import com.phatnguyen.youtubeapidemo.R;
import com.phatnguyen.youtubeapidemo.Service.ServiceHandler;
import com.phatnguyen.youtubeapidemo.Utils.StringUtils;
import com.phatnguyen.youtubeapidemo.adapter.LazyAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Created by phatnguyen on 10/2/17.
 */
public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener {
	ProgressDialog pDialog;
	//contact JSONArray
	JSONArray items = null;
	//Hashmap for ListView
	ArrayList<HashMap<String,String>> itemsList;
	// used to store app title
	private CharSequence mTitle;
	Boolean flag_loading = false;
	int default_offset = 25;
	Parcelable state;
	ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTitle = getTitle();
		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActionBar().setHomeButtonEnabled(true);
		}
		ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#992416"));
		getActionBar().setBackgroundDrawable(colorDrawable);

		itemsList = new ArrayList<HashMap<String, String>>();
		lv = getListView();
		//Listview on item click listener
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Intent in = new Intent(MainActivity.this, YoutubePlayer.class);
				HashMap<String, String> item = new HashMap<String, String>();
				item = itemsList.get(i);
				in.putExtra("videoId", item.get(Constant.TAG_VIDEO_ID));
				startActivity(in);
			}
		});
		//Load more data
		lv.setOnScrollListener(new AbsListView.OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {


			}

			public void onScroll(AbsListView view, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {

				if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
				{
					if(flag_loading == false)
					{
						flag_loading = true;
						default_offset += 25;
						new GetItems().execute(Integer.toString(default_offset));
					}
				}
			}
		});


		//Default get
		new GetItems().execute(Integer.toString(default_offset));

	}

	@Override
	public boolean onQueryTextSubmit(String s) {
		Intent searchIntent = new Intent(MainActivity.this, SearchResultsActivity.class);
		searchIntent.putExtra(SearchManager.QUERY, s);
		searchIntent.setAction(Intent.ACTION_SEARCH);
		startActivity(searchIntent);

		return true; //Start the search activity manually
	}

	@Override
	public boolean onQueryTextChange(String s) {
		return false;
	}

	class GetItems extends AsyncTask<String,Void,Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			state = lv.onSaveInstanceState();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Loading data ...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Void doInBackground(String... offset) {

			String requestURL = Constant.SERVER_URL + "playlistItems?part=snippet&playlistId=" + Constant.PLAY_LIST_API + "&maxResults=" + offset[0] + "&key=" + Constant.API_KEY;

			//Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			//Making a request to url and getting response
			String jsonStr = sh.makeServiceCall(requestURL,ServiceHandler.GET);

			Log.d("Response: ", "> " + jsonStr);

			if(jsonStr != null)
			{
				try
				{
					JSONObject jsonObj = new JSONObject(jsonStr);

					//Getting JSON Array node
					items = jsonObj.getJSONArray(Constant.TAG_ITEMS);

					//looping through all items
					for (int i = 0; i < items.length(); i++) {
						JSONObject it = items.getJSONObject(i);
						//Snippet node
						JSONObject snippet = it.getJSONObject(Constant.TAG_SNIPPET);
						String title = snippet.getString(Constant.TAG_TITLE);
						String pubDate = snippet.getString(Constant.TAG_PUBLISHDATE);
						String channel = snippet.getString(Constant.TAG_CHANNEL_TITLE);
						String description = snippet.getString(Constant.TAG_DESCRIPTION);
						//Thumbnail node
						JSONObject thumbnails = snippet.getJSONObject(Constant.TAG_THUMBNAILS);
						JSONObject df = thumbnails.getJSONObject(Constant.TAG_DEFAULT);
						String imgUrl = df.getString(Constant.TAG_IMG_URL);
						JSONObject resId = snippet.getJSONObject(Constant.TAG_RES_ID);
						Constant.VIDEO_ID = resId.getString(Constant.TAG_VIDEO_ID);
						// tmp hashmap for single contact
						HashMap<String, String> item = new HashMap<String, String>();
						// adding each child node to HashMap key => value
						item.put(Constant.TAG_IMG_URL,imgUrl);
						item.put(Constant.TAG_TITLE, title);
						item.put(Constant.TAG_PUBLISHDATE, pubDate);
						item.put(Constant.TAG_CHANNEL_TITLE, channel);
						item.put(Constant.TAG_DESCRIPTION, description);
                        item.put(Constant.TAG_VIDEO_ID,Constant.VIDEO_ID);
						// adding contact to contact list
						itemsList.add(item);
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
			else {
				Log.e("ServiceHandler","Couldn't get any data from the url");
			}
			return null;
		}



		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			//Dismiss the progress dialog
			if(pDialog.isShowing()) pDialog.dismiss();
			//Updating data
			ListAdapter adapter = new LazyAdapter(MainActivity.this,itemsList);
			setListAdapter(adapter);
			if(state != null) {
				lv.onRestoreInstanceState(state);
			}
			}
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(new ComponentName(MainActivity.this,SearchResultsActivity.class)));
		searchView.setOnQueryTextListener(MainActivity.this);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar actions click
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onPause() {
		// Save ListView state @ onPause
		state = lv.onSaveInstanceState();
		super.onPause();
	}

}
