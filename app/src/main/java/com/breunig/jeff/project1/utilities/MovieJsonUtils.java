package com.breunig.jeff.project1.utilities;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import com.breunig.jeff.project1.models.Movie;

/**
 * Created by jkbreunig on 2/2/17.
 */

public final class MovieJsonUtils {

    public static Movie[] getMoviesFromJson(Context context, String jsonStr)
            throws JSONException {

        Movie[] movies = null;

        JSONObject moviesJson = new JSONObject(jsonStr);

        if (moviesJson.has("status_code")) {
            int errorCode = moviesJson.getInt("status_code");

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray moviesJsonArray = moviesJson.getJSONArray("results");
        movies = new Movie[moviesJsonArray.length()];

        for (int i = 0; i < moviesJsonArray.length(); i++) {
            String date;
            String highAndLow;
            JSONObject movieJsonObject = moviesJsonArray.getJSONObject(i);
            Movie movie = new Movie(movieJsonObject);
            movies[i] = movie;
        }

        return movies;
    }
}
