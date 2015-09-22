package com.kaustubh.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private String apikey;
    private ImageAdapter imageAdapter = new ImageAdapter(this);
    private static final int SETTINGS_SELECTED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apikey = getString(R.string.api_key);







    }

    @Override
    protected void onStart() {
        super.onStart();
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(imageAdapter);

        updateMovies();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent detailMovie = new Intent(getApplicationContext(), DetailMovie.class);
                detailMovie.putExtra(Intent.EXTRA_TEXT, imageAdapter.getMovieID(position));
                startActivity(detailMovie);
            }
        });

    }

    private void updateMovies() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!isNetworkAvailable()) {

            Toast.makeText(getApplicationContext(), "Network connection not available. Please check your internet settings.", Toast.LENGTH_LONG).show();

        } else {

            String sortorder = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));

            if (sortorder.equalsIgnoreCase("Highest Rated First"))
                sortorder = "vote_average.desc";
            else
                sortorder = "popularity.desc";


            FetchMovieTask fetchMovieTask = new FetchMovieTask();
            fetchMovieTask.execute(sortorder);
       }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SETTINGS_SELECTED) {
            if (resultCode == RESULT_OK) {
                updateMovies();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            //startActivity(settings);

            startActivityForResult(settings, SETTINGS_SELECTED);
            return true;
        }




        return super.onOptionsItemSelected(item);
    }


    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

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

                String path1 = "http://api.themoviedb.org/3/discover/movie?";

                Uri builtUri = Uri.parse(path1).buildUpon()
                        .appendQueryParameter("sort_by", params[0])
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
            if (result != null) {

                String ppath[] = new String[result.length];
                String ids[] = new String[result.length];


                for (int i = 0; i < result.length; i++) {

                    ids[i] = result[i].substring(0, result[i].indexOf(":"));
                    ppath[i] = result[i].substring(result[i].indexOf(":") + 1, result[i].length());

                }


                    ArrayList<String> posterPaths = imageAdapter.getPosterPaths();
                    ArrayList<String> movieIDs = imageAdapter.getMovieIDs();
                    posterPaths.clear();
                    movieIDs.clear();
                    for (int i = 0; i < result.length; i++) {
                        posterPaths.add(ppath[i]);
                        movieIDs.add(ids[i]);



                    }
                imageAdapter.notifyDataSetChanged();
            }


        }


    }

    private String[] getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String M_RESULTS = "results";
        final String M_ID = "id";
        final String M_POSTER = "poster_path";


        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(M_RESULTS);

        String[] resultStrs = new String[movieArray.length()];
        for (int i = 0; i < movieArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            resultStrs[i] = movieArray.getJSONObject(i).getString(M_ID) + ":";

            resultStrs[i] += movieArray.getJSONObject(i).getString(M_POSTER);
        }

        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Poster paths: " + s);
        }
        //numPosters = resultStrs.length;
        return resultStrs;

    }


}

