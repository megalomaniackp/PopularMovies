package com.kaustubh.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailMovie extends AppCompatActivity {

    private final String LOG_TAG = FetchDetailsTask.class.getSimpleName();
    private String apikey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        apikey = getString(R.string.api_key);

        Intent intent = getIntent();

        String message = intent.getStringExtra(Intent.EXTRA_TEXT);

        //TextView textView = (TextView) findViewById(R.id.detail_movie_title);

        //textView.setText(message);
        showMovieDetails(message);


    }

    private void showMovieDetails(String id) {
        FetchDetailsTask fetchDetailsTask = new FetchDetailsTask();
        fetchDetailsTask.execute(id);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_movie, menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchDetailsTask extends AsyncTask<String, Void, String[]> {

        //private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String[] movieID;

        @Override
        protected String[] doInBackground(String... params) {


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                String path1 = "http://api.themoviedb.org/3/movie/";

                Uri builtUri = Uri.parse(path1).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter("api_key", apikey)
                        .build();

                URL url = new URL(builtUri.toString());



                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            String baseURL = "http://image.tmdb.org/t/p/w500";

            TextView title = (TextView) findViewById(R.id.detail_movie_title);
            ImageView poster = (ImageView) findViewById(R.id.detail_movie_poster);
            TextView releasedate = (TextView) findViewById(R.id.detail_movie_releasedate);
            TextView rating = (TextView) findViewById(R.id.detail_movie_rating);
            TextView overview = (TextView) findViewById(R.id.detail_movie_overview);

            poster.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Uri imgUri = Uri.parse(baseURL + result[2]);
            //Picasso.with(mContext).load(imgUri).resize(185, 185).centerCrop().into(imageView);
            Picasso.with(getApplicationContext()).load(imgUri).into(poster);

            title.setText(result[0]);
            overview.setText(result[1]);
            rating.setText(result[3] + "/10");
            releasedate.setText(result[4].substring(0,4));




            //super.onPostExecute(strings);

        }


    }

    private String[] getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        String[] movieDetails = new String[5];
        // These are the names of the JSON objects that need to be extracted.
        final String M_TITLE = "title";
        final String M_OVERVIEW = "overview";
        final String M_POSTER = "poster_path";
        final String M_RATING = "vote_average";
        final String M_DATE = "release_date";


        JSONObject movieJson = new JSONObject(movieJsonStr);

        movieDetails[0] = movieJson.getString(M_TITLE);
        movieDetails[1] = movieJson.getString(M_OVERVIEW);
        movieDetails[2] = movieJson.getString(M_POSTER);
        movieDetails[3] = movieJson.getString(M_RATING);
        movieDetails[4] = movieJson.getString(M_DATE);


        //numPosters = resultStrs.length;
        return movieDetails;

    }

}
