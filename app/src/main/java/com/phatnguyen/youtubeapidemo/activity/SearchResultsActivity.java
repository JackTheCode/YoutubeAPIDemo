package com.phatnguyen.youtubeapidemo.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.phatnguyen.youtubeapidemo.R;
import com.phatnguyen.youtubeapidemo.Service.ServiceHandler;
import com.phatnguyen.youtubeapidemo.Utils.Constant;
import com.phatnguyen.youtubeapidemo.adapter.LazyAdapter;

/**
 * Created by phatnguyen on 10/2/17.
 */
public class SearchResultsActivity extends ListActivity {
    public static final String TAG = "com.phatnguyen.youtubeapidemo.search";
    ProgressDialog pDialog;
    //URL to get contacts JSON
    private static String url;
    public static String VIDEO_ID = null;
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
        itemsList = new ArrayList<HashMap<String, String>>();
        lv = getListView();
        //Listview on item click listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent in = new Intent(SearchResultsActivity.this, YoutubePlayer.class);
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
                        handleIntent(getIntent(),Integer.toString(default_offset));
                        new SearchTask().execute();
                    }
                }
            }
        });
        //Calling search
        handleIntent(getIntent(),Integer.toString(default_offset));
        //Calling async task to get json
        new SearchTask().execute();
        mTitle = getTitle();
        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(true);
        }
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#992416"));
        getActionBar().setBackgroundDrawable(colorDrawable);
    }
    class SearchTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            state = lv.onSaveInstanceState();
            pDialog = new ProgressDialog(SearchResultsActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            //Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url,ServiceHandler.GET);

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
                         //Id node
                         JSONObject id = it.getJSONObject(Constant.TAG_ID);
                         VIDEO_ID = id.getString(Constant.TAG_VIDEO_ID);
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
                        // tmp hashmap for single contact
                        HashMap<String, String> item = new HashMap<String, String>();
                        // adding each child node to HashMap key => value
                        item.put(Constant.TAG_IMG_URL,imgUrl);
                        item.put(Constant.TAG_TITLE, title);
                        item.put(Constant.TAG_PUBLISHDATE, pubDate);
                        item.put(Constant.TAG_CHANNEL_TITLE, channel);
                        item.put(Constant.TAG_DESCRIPTION, description);
                        item.put(Constant.TAG_VIDEO_ID,VIDEO_ID);
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
            ListAdapter adapter = new LazyAdapter(SearchResultsActivity.this,itemsList);
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
                searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        return super.onOptionsItemSelected(item);
    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent,Integer.toString(default_offset));
    }

    /**
     * Handling intent data
     */
    private void handleIntent(Intent intent,String offset) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            url = Constant.SERVER_URL + "search?part=snippet&q=" + query + "&maxResults=" + offset + "&key=" + Constant.API_KEY;
            url = url.replaceAll(" ","%20");
            }
        }


}
