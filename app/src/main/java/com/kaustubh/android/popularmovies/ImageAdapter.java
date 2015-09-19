package com.kaustubh.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by HP Owner on 14/09/2015.
 */
public class ImageAdapter extends BaseAdapter {

    private final String baseURL = "http://image.tmdb.org/t/p/w185";

    private Context mContext;
    private ArrayList<String> posterPaths;
    private ArrayList<String> movieID;



    public ImageAdapter(Context c) {
        mContext = c;
        posterPaths = new ArrayList<>();
        movieID = new ArrayList<>();
    }

    public int getCount() {

        return posterPaths.size();
    }

    public String getItem(int position) {

        return posterPaths.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);

            //imageView.setPadding(0, 0, 0, 0);


        } else {
            imageView = (ImageView) convertView;
        }
        //Log.v("ImageAdapter", "trying position " + position);
        //imageView.setImageResource(mThumbIds[position]);
        Uri imgUri = Uri.parse(baseURL + posterPaths.get(position));
        //Picasso.with(mContext).load(imgUri).resize(185, 185).centerCrop().into(imageView);
        Picasso.with(mContext).load(imgUri).into(imageView);


        return imageView;
    }

    /*
    public void addPosterPath(String path) {
        posterPaths.add(path);
    }
    */
    public ArrayList<String> getPosterPaths() {

        return posterPaths;
    }

    public String getMovieID (int position){
        return movieID.get(position);
    }

    public void setMovieID (String id) {
        movieID.add(id);
    }

    public ArrayList<String> getMovieIDs() {
        return movieID;
    }



}
