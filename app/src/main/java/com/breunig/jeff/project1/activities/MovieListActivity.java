package com.breunig.jeff.project1.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.breunig.jeff.project1.R;
import com.breunig.jeff.project1.adapters.MovieListAdapter;
import com.breunig.jeff.project1.adapters.MovieListAdapter.MovieListAdapterOnClickHandler;
import com.breunig.jeff.project1.adapters.MovieListCursorAdapter;
import com.breunig.jeff.project1.database.MovieContract;
import com.breunig.jeff.project1.listeners.AsyncTaskCompleteListener;
import com.breunig.jeff.project1.models.Movie;
import com.breunig.jeff.project1.models.MovieSortType;
import com.breunig.jeff.project1.models.Movies;
import com.breunig.jeff.project1.network.FetchMovieTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jkbreunig on 2/2/17.
 */

public class MovieListActivity extends AppCompatActivity implements MovieListAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>, MovieListCursorAdapter.MovieListAdapterOnClickHandler  {
    private int mColumnWidth;
    private MovieListAdapter mMovieListAdapter;
    private MovieListCursorAdapter mFavoriteMovieListAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    private MovieSortType mMovieSortType = MovieSortType.POPULAR;
    private static final int MOVIE_LOADER_ID = 0;
    private static final String TAG = MovieListActivity.class.getSimpleName();
    private GridLayoutManager mLayoutManager;
    private Movies mMovies = new Movies();
    @BindView(R.id.recyclerview_movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message_display) TextView mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;

    private int calculateColumnWidth(Context context, int numberOfColumns) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int width = (int) (displayMetrics.widthPixels / numberOfColumns);
        return width;
    }

    private int calculateNumberOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels;
        int numberOfColumns = (int) (dpWidth / (int) getResources().getDimension(R.dimen.movie_poster_min_width));
        return numberOfColumns;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        ButterKnife.bind(this);

        int numberOfColumns = calculateNumberOfColumns(mRecyclerView.getContext());
        mColumnWidth = calculateColumnWidth(mRecyclerView.getContext(), numberOfColumns);

        mLayoutManager = new GridLayoutManager(this, numberOfColumns);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieListAdapter = new MovieListAdapter(this, mColumnWidth);

        mFavoriteMovieListAdapter = new MovieListCursorAdapter(this, this, mColumnWidth);

        addOnScrollListener();

        retrieveMovieSortType();
        updateMovieSortType(mMovieSortType);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMovieSortType == MovieSortType.FAVORITES) {
            loadFavoriteMovieData();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        retrieveMovieSortType();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void updateMovieSortType(MovieSortType movieSortType) {
        mMovieSortType = movieSortType;
        storeMovieSortType();
        updateTitle();
        mMovieListAdapter.setMovies(null);
        if (mMovieSortType == MovieSortType.FAVORITES) {
            mRecyclerView.setAdapter(mFavoriteMovieListAdapter);
            loadFavoriteMovieData();
        } else {
            mRecyclerView.setAdapter(mMovieListAdapter);
            loadMovieData();
        }
    }

    private void loadMovieData() {
        mMovies.isLoading = true;
        showMoviesView();
        mLoadingIndicator.setVisibility(View.VISIBLE);
        new FetchMovieTask(this, new MovieListActivity.FetchMovieTaskCompleteListener(), mMovieSortType, mMovies.getPage()).execute();
    }

    private void loadFavoriteMovieData() {
        showMoviesView();
        mLoadingIndicator.setVisibility(View.VISIBLE);
        LoaderManager manager = getSupportLoaderManager();
        if (manager.getLoader(MOVIE_LOADER_ID) == null) {
            manager.initLoader(MOVIE_LOADER_ID, null, this);
        } else {
            manager.restartLoader(MOVIE_LOADER_ID, null, this);
        }
    }

    @Override
    public void onClick(Movie movie) {
        Context context = this;
        Class destinationClass = MovieDetailActivity.class;
        Intent intent = new Intent(context, destinationClass);
        intent.putExtra(getString(R.string.EXTRA_MOVIE), movie);
        intent.putExtra(getString(R.string.EXTRA_POSTER_WIDTH), mColumnWidth);
        startActivity(intent);
    }

    private void showMoviesView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        if (mMovieSortType == MovieSortType.FAVORITES) {
            mErrorMessageDisplay.setText(R.string.no_database_content_message);
        } else {
            mErrorMessageDisplay.setText(R.string.network_error_message);
        }
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public class FetchMovieTaskCompleteListener implements AsyncTaskCompleteListener<Movies> {

        @Override
        public void onTaskComplete(Movies movies) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mMovies.updatePageResults(movies);
            if (mMovies.results != null) {
                showMoviesView();
                updateMovieListAdapter();
            } else {
                showErrorMessage();
            }
            mMovies.isLoading = false;
        }
    }

    private void updateMovieListAdapter() {
        Movie[] movieArray = mMovies.results.toArray(new Movie[(mMovies.results.size())]);
        mMovieListAdapter.setMovies(movieArray);
    }

    private void storeMovieSortType() {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.SHARED_PREFERENCES), MODE_PRIVATE).edit();
        editor.putInt(getString(R.string.MOVIE_SORT_TYPE_KEY), mMovieSortType.getIntValue());
        editor.commit();
    }

    private void retrieveMovieSortType() {
        SharedPreferences settings = getSharedPreferences(getString(R.string.SHARED_PREFERENCES), MODE_PRIVATE);
        int value = settings.getInt(getString(R.string.MOVIE_SORT_TYPE_KEY), 0);
        mMovieSortType = MovieSortType.fromInt(value);
        if (mMovieSortType == null) {
            mMovieSortType = MovieSortType.POPULAR;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movie_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_popular) {
            updateMovieSortType(MovieSortType.POPULAR);
            return true;
        } else if (id == R.id.action_top_rated) {
            updateMovieSortType(MovieSortType.TOP_RATED);
            return true;
        } else if (id == R.id.action_favorites) {
            updateMovieSortType(MovieSortType.FAVORITES);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateTitle() {
        String sortTypeTitle;
        if (mMovieSortType == MovieSortType.POPULAR) {
            sortTypeTitle = getString(R.string.popular_movies);
        } else if (mMovieSortType == MovieSortType.TOP_RATED) {
            sortTypeTitle = getString(R.string.top_rated_movies);
        } else {
            sortTypeTitle = getString(R.string.favorite_movies);
        }
        setTitle(sortTypeTitle);
    }

    private void addOnScrollListener() {
        mRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

            if (mMovieSortType != MovieSortType.FAVORITES &&
                    !mMovies.isLoading &&
                    mMovies.isMoreContent()) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                    loadMovieData();
                }
            }
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            Cursor mTaskData = null;

            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    deliverResult(mTaskData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {

                try {
                    return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mFavoriteMovieListAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
        }
        mRecyclerView.smoothScrollToPosition(mPosition);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data.getCount() != 0) {
            showMoviesView();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mFavoriteMovieListAdapter.swapCursor(null);
    }
}