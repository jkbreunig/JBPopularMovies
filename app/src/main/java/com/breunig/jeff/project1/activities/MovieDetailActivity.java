package com.breunig.jeff.project1.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.breunig.jeff.project1.R;
import com.breunig.jeff.project1.adapters.MovieReviewListAdapter;
import com.breunig.jeff.project1.models.Movie;
import com.breunig.jeff.project1.models.MovieReview;
import com.breunig.jeff.project1.models.MovieReviews;
import com.breunig.jeff.project1.network.AsyncTaskCompleteListener;
import com.breunig.jeff.project1.network.FetchMovieReviewTask;
import com.breunig.jeff.project1.utilities.MovieJsonUtils;
import com.breunig.jeff.project1.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity {
    private Movie mMovie;
    private MovieReviews mMovieReviews = new MovieReviews();
    private int mPosterWidth;
    private MovieReviewListAdapter mMovieReviewListAdapter;
    @BindView(R.id.recyclerview_movie_review_list) RecyclerView mRecyclerView;
    @BindView(R.id.iv_poster) ImageView mPosterImageView;
    @BindView(R.id.tv_title) TextView mTitleTextView;
    @BindView(R.id.tv_overview) TextView mOverviewTextView;
    @BindView(R.id.tv_release_date) TextView mReleaseDateTextView;
    @BindView(R.id.tv_user_rating) TextView mUserRatingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.movie_detail));

        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        mMovie = (Movie) getIntent().getParcelableExtra("MOVIE");
        mPosterWidth = getIntent().getIntExtra("POSTER_WIDTH", 0);
        Picasso.with(this).load(NetworkUtils.buildMoviePosterUrlString(mMovie.posterPath, mPosterWidth))
                .into(mPosterImageView);

        mTitleTextView.setText(mMovie.title);

        mOverviewTextView.setText(mMovie.overview);

        String formattedReleaseDate = MovieJsonUtils.formatDateString(mMovie.releaseDate);
        if (formattedReleaseDate != null && !formattedReleaseDate.isEmpty()) {
            mReleaseDateTextView.setText(getString(R.string.release_date) + ": " + formattedReleaseDate);
        } else {
            mReleaseDateTextView.setText(getString(R.string.release_date) + getString(R.string.not_available));
        }

        String userRating = mMovie.userRating;
        if (userRating != null) {
            mUserRatingTextView.setText(getString(R.string.user_rating) + ": " + userRating + "/ 10");
        } else {
            mUserRatingTextView.setText(getString(R.string.user_rating) + getString(R.string.not_available));
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieReviewListAdapter = new MovieReviewListAdapter();

        mRecyclerView.setAdapter(mMovieReviewListAdapter);

        loadMovieReviewData();
    }

    private void loadMovieReviewData() {
        //showMoviesView();
        //mLoadingIndicator.setVisibility(View.VISIBLE);
        new FetchMovieReviewTask(this, new FetchMovieReviewTaskCompleteListener(), mMovie.movieId).execute();
    }

    public class FetchMovieReviewTaskCompleteListener implements AsyncTaskCompleteListener<MovieReviews> {

        @Override
        public void onTaskComplete(MovieReviews movieReviews) {
            //mLoadingIndicator.setVisibility(View.INVISIBLE);
            mMovieReviews.updatePageResults(movieReviews);
            if (mMovieReviews.results != null) {
                //showMoviesView();
                MovieReview[] movieReviewArray = mMovieReviews.results.toArray(new MovieReview[(mMovieReviews.results.size())]);
                mMovieReviewListAdapter.setMovies(movieReviewArray);
            } else {
                //showErrorMessage();
            }
        }
    }
}