package com.phatnguyen.youtubeapidemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import com.phatnguyen.youtubeapidemo.Utils.Constant;
import com.phatnguyen.youtubeapidemo.Utils.ImageLoader.ImageLoader;
import com.phatnguyen.youtubeapidemo.Utils.StringUtils;
import com.phatnguyen.youtubeapidemo.activity.MainActivity;
import com.phatnguyen.youtubeapidemo.R;

/**
 * Created by phatnguyen on 10/2/17.
 */
public class LazyAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> itemsList;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;

    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        itemsList=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return itemsList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_video_item, null);
        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView pubDate = (TextView)vi.findViewById(R.id.pubDate); // pubDate
        TextView channel = (TextView)vi.findViewById(R.id.channelTitle); // channel
        final TextView description = (TextView)vi.findViewById(R.id.description);// description
        ImageView imgVideo =(ImageView)vi.findViewById(R.id.imageVideo);
        HashMap<String, String> item = new HashMap<String, String>();
        item = itemsList.get(position);
        // Setting all values in listview
        title.setText(item.get(Constant.TAG_TITLE));
        pubDate.setText(item.get(Constant.TAG_PUBLISHDATE));
        channel.setText(item.get(Constant.TAG_CHANNEL_TITLE));
        description.setText(item.get(Constant.TAG_DESCRIPTION));
        imageLoader.DisplayImage(item.get(Constant.TAG_IMG_URL), imgVideo);
        return vi;
    }
}
