package asu.edu.cse535.locationawarereminder.activities;

/**
 * Created by Sooraj on 10/27/2016.
 * This class is used create a customized list view for the Nearby Places screen.
 */

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import asu.edu.cse535.locationawarereminder.R;

public class NearbyCustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final ArrayList<String> placeName;
    private final ArrayList<String> imageId;
    private final HashMap<String, Drawable> imageMap = new HashMap<>(); // Hashmap for caching images

    public NearbyCustomList(Activity context, ArrayList<String> placeName, ArrayList<String> imageId) {
        super(context, R.layout.nearby_place_item, placeName);
        this.context = context;
        this.placeName = placeName;
        this.imageId = imageId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.nearby_place_item, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.item_name);
        txtTitle.setText(placeName.get(position));

        ImageView imageView = (ImageView) rowView.findViewById(R.id.item_icon);
        String url = imageId.get(position);

        new ImageDownloadTask(imageView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

        return rowView;
    }

    // Thread to download image asynchronously
    class ImageDownloadTask extends AsyncTask<String, Integer, Drawable> {
        private ImageView mView;
        ImageDownloadTask(ImageView view){
            mView = view;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            InputStream is = null;
            String url = params[0];
            if(imageMap.containsKey(url))
                return imageMap.get(url);
            try {
                is = (InputStream) new URL(url).getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Drawable d = Drawable.createFromStream(is, "image");
            imageMap.put(url,d);
            return d;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            mView.setImageDrawable(result);
        }
    }
}