package com.breunig.jeff.project1.adapters;

/**
 * Created by jkbreunig on 2/20/17.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.breunig.jeff.project1.R;
import com.breunig.jeff.project1.database.MovieContract;
import com.breunig.jeff.project1.models.Movie;
import com.breunig.jeff.project1.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;


public class MovieListCursorAdapter extends RecyclerView.Adapter<MovieListCursorAdapter.MovieListCursorAdapterViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private int mColumnWidth;

    private final MovieListCursorAdapter.MovieListAdapterOnClickHandler mClickHandler;

    public interface MovieListAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    public MovieListCursorAdapter(Context mContext, MovieListCursorAdapter.MovieListAdapterOnClickHandler clickHandler, int columnWidth) {

        this.mContext = mContext;
        mClickHandler = clickHandler;
        mColumnWidth = columnWidth;
    }

    class MovieListCursorAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMovieImageView;

        public MovieListCursorAdapterViewHolder(View view) {
            super(view);
            mMovieImageView = (ImageView) view.findViewById(R.id.iv_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            int posterIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
            String posterPath = mCursor.getString(posterIndex);
            Movie movie = new Movie();
            movie.posterPath = posterPath;
            mClickHandler.onClick(movie);
        }
    }

    @Override
    public MovieListCursorAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MovieListCursorAdapter.MovieListCursorAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieListCursorAdapterViewHolder viewHolder, int position) {

        int posterIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        mCursor.moveToPosition(position);
        String posterPath = mCursor.getString(posterIndex);

        ImageView imageView = viewHolder.mMovieImageView;
        int imageHeight = (int) (mColumnWidth * 1.5);
        Picasso.with(imageView.getContext())
                .load(NetworkUtils.buildMoviePosterUrlString(posterPath, mColumnWidth))
                .resize(mColumnWidth, imageHeight)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        if (mCursor == c) {
            return null;
        }
        Cursor temp = mCursor;
        this.mCursor = c;

        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }
}
