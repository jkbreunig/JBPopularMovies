package com.breunig.jeff.project1.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.breunig.jeff.project1.R;
import com.breunig.jeff.project1.models.Movie;

public class MovieDetailActivity extends AppCompatActivity {
    private Movie mMovie;
    private TextView mTitleTextView;
    private TextView mOverviewTextView;
    private TextView mReleaseDateTextView;
    private TextView mUserRatingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mMovie = (Movie) getIntent().getSerializableExtra("MOVIE");

        mTitleTextView = (TextView) findViewById(R.id.tv_title);
        mTitleTextView.setText(mMovie.title);

        mOverviewTextView = (TextView) findViewById(R.id.tv_overview);
        mOverviewTextView.setText(mMovie.overview);

        mReleaseDateTextView = (TextView) findViewById(R.id.tv_release_date);
        String releaseDate = mMovie.releaseDate;
        if (releaseDate != null && !releaseDate.isEmpty()) {
            mReleaseDateTextView.setText(getString(R.string.release_date) + ": " + releaseDate);
        } else {
            mReleaseDateTextView.setText(getString(R.string.release_date) + getString(R.string.not_available));
        }

        mUserRatingTextView = (TextView) findViewById(R.id.tv_user_rating);
        String userRating = mMovie.userRating;
        if (userRating != null) {
            mUserRatingTextView.setText(getString(R.string.user_rating) + ": " + userRating);
        } else {
            mUserRatingTextView.setText(getString(R.string.user_rating) + getString(R.string.not_available));
        }
    }
}