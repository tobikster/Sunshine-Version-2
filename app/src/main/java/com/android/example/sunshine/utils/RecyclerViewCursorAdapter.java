package com.android.example.sunshine.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.widget.Filter;
import android.widget.FilterQueryProvider;

public abstract class RecyclerViewCursorAdapter<VH extends android.support.v7.widget.RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements CursorFilter.CursorFilterClient {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = RecyclerViewCursorAdapter.class.getSimpleName();
	/**
	 * If set the adapter will register a content observer on the cursor and will call {@link #onContentChanged()} when
	 * a notification comes in.  Be careful when using this flag: you will need to unset the current Cursor from the
	 * adapter to avoid leaks due to its registered observers.  This flag is not needed when using a CursorAdapter with
	 * a {@link android.content.CursorLoader}.
	 */
	public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;

	protected final Context mContext;
	private FilterQueryProvider mFilterQueryProvider;
	private boolean mDataValid;
	private Cursor mCursor;
	private int mRowIDColumn;
	private ChangeObserver mChangeObserver;
	private DataSetObserver mDataSetObserver;
	private CursorFilter mCursorFilter;

	/**
	 * Recommended constructor.
	 *
	 * @param c     The cursor from which to get the data.
	 * @param flags Flags used to determine the behavior of the adapter; may be {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
	 */
	public RecyclerViewCursorAdapter(final Context context, final Cursor c, final int flags) {
		mContext = context;
		init(c, flags);
	}

	private void init(final Cursor cursor, final int flags) {
		final boolean registerContentObserver = (flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER;
		mChangeObserver = (registerContentObserver) ? new ChangeObserver() : null;
		mDataSetObserver = (registerContentObserver) ? new MyDataSetObserver() : null;
		mCursor = null;
		swapCursor(cursor);
	}

	/**
	 * Returns the cursor.
	 *
	 * @return the cursor.
	 */
	@Override
	public Cursor getCursor() {
		return mCursor;
	}

	/**
	 * <p>Converts the cursor into a CharSequence. Subclasses should override this method to convert their results. The
	 * default implementation returns an empty String for null values or the default String representation of the
	 * value.</p>
	 *
	 * @param cursor the cursor to convert to a CharSequence
	 *
	 * @return a CharSequence representing the value
	 */
	@Override
	public CharSequence convertToString(final Cursor cursor) {
		return cursor == null ? "" : cursor.toString();
	}

	/**
	 * Runs a query with the specified constraint. This query is requested by the filter attached to this adapter.
	 * <p/>
	 * The query is provided by a {@link android.widget.FilterQueryProvider}. If no provider is specified, the current
	 * cursor is not filtered and returned.
	 * <p/>
	 * After this method returns the resulting cursor is passed to {@link #changeCursor(Cursor)} and the previous cursor
	 * is closed.
	 * <p/>
	 * This method is always executed on a background thread, not on the application's main thread (or UI thread.)
	 * <p/>
	 * Contract: when constraint is null or empty, the original results, prior to any filtering, must be returned.
	 *
	 * @param constraint the constraint with which the query must be filtered
	 *
	 * @return a Cursor representing the results of the new query
	 *
	 * @see #getFilter()
	 * @see #getFilterQueryProvider()
	 * @see #setFilterQueryProvider(android.widget.FilterQueryProvider)
	 */
	@Override
	public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
		if (mFilterQueryProvider != null) {
			return mFilterQueryProvider.runQuery(constraint);
		}

		return mCursor;
	}

	/**
	 * Change the underlying cursor to a new cursor. If there is an existing cursor it will be closed.
	 *
	 * @param cursor The new cursor to be used
	 */
	@Override
	public void changeCursor(final Cursor cursor) {
		final Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	/**
	 * Swap in a new Cursor, returning the old Cursor.  Unlike {@link #changeCursor(Cursor)}, the returned old Cursor is
	 * <em>not</em> closed.
	 *
	 * @param newCursor The new cursor to be used.
	 *
	 * @return Returns the previously set Cursor, or null if there wasa not one. If the given new Cursor is the same
	 * instance is the previously set Cursor, null is also returned.
	 */
	public Cursor swapCursor(final Cursor newCursor) {
		Cursor oldCursor = null;
		if (newCursor != mCursor) {
			oldCursor = mCursor;
			if (oldCursor != null) {
				if (mChangeObserver != null) {
					oldCursor.unregisterContentObserver(mChangeObserver);
				}
				if (mDataSetObserver != null) {
					oldCursor.unregisterDataSetObserver(mDataSetObserver);
				}
			}
			mCursor = newCursor;
			mDataValid = mCursor != null;
			mRowIDColumn = (mCursor != null) ? mCursor.getColumnIndexOrThrow("_id") : -1;

			if (newCursor != null) {
				if (mChangeObserver != null) {
					newCursor.registerContentObserver(mChangeObserver);
				}
				if (mDataSetObserver != null) {
					newCursor.registerDataSetObserver(mDataSetObserver);
				}
			}
			notifyDataSetChanged();
		}
		return oldCursor;
	}

	public Filter getFilter() {
		if (mCursorFilter == null) {
			mCursorFilter = new CursorFilter(this);
		}
		return mCursorFilter;
	}

	/**
	 * Returns the query filter provider used for filtering. When the provider is null, no filtering occurs.
	 *
	 * @return the current filter query provider or null if it does not exist
	 *
	 * @see #setFilterQueryProvider(android.widget.FilterQueryProvider)
	 * @see #runQueryOnBackgroundThread(CharSequence)
	 */
	public FilterQueryProvider getFilterQueryProvider() {
		return mFilterQueryProvider;
	}

	/**
	 * Sets the query filter provider used to filter the current Cursor. The provider's {@link
	 * android.widget.FilterQueryProvider#runQuery(CharSequence)} method is invoked when filtering is requested by a
	 * client of this adapter.
	 *
	 * @param filterQueryProvider the filter query provider or null to remove it
	 *
	 * @see #getFilterQueryProvider()
	 * @see #runQueryOnBackgroundThread(CharSequence)
	 */
	public void setFilterQueryProvider(final FilterQueryProvider filterQueryProvider) {
		mFilterQueryProvider = filterQueryProvider;
	}

	/**
	 * This method will move the Cursor to the correct position and call {@link #onBindViewHolder(RecyclerView.ViewHolder,
	 * Cursor)}.
	 *
	 * @param holder {@inheritDoc}
	 * @param i      {@inheritDoc}
	 */
	@Override
	public void onBindViewHolder(final VH holder, final int i) {
		if (!mDataValid) {
			throw new IllegalStateException("this should only be called when the cursor is valid");
		}
		if (!mCursor.moveToPosition(i)) {
			throw new IllegalStateException("couldn't move cursor to position " + i);
		}
		onBindViewHolder(holder, mCursor);
	}

	@Override
	public void setHasStableIds(final boolean hasStableIds) {
		super.setHasStableIds(true);
	}

	@Override
	public long getItemId(final int position) {
		return (mDataValid && mCursor != null) ?
		       (mCursor.moveToPosition(position)) ? mCursor.getLong(mRowIDColumn) : -1 :
		       -1;
	}

	@Override
	public int getItemCount() {
		return (mDataValid && mCursor != null) ? mCursor.getCount() : 0;
	}

	public abstract void onBindViewHolder(final VH viewHolder, final Cursor cursor);

	/**
	 * Called when the {@link ContentObserver} on the cursor receives a change notification. Can be overridden by sub
	 * classes.
	 *
	 * @see ContentObserver#onChange(boolean)
	 */
	protected void onContentChanged() {
	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(final boolean selfChange) {
			onContentChanged();
		}
	}

	private class MyDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			mDataValid = true;
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			mDataValid = false;
			notifyDataSetChanged();
		}
	}
}

class CursorFilter extends Filter {

	CursorFilterClient mClient;

	CursorFilter(CursorFilterClient client) {
		mClient = client;
	}

	@Override
	protected FilterResults performFiltering(final CharSequence constraint) {
		final Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

		final FilterResults results = new FilterResults();
		if (cursor != null) {
			results.count = cursor.getCount();
			results.values = cursor;
		}
		else {
			results.count = 0;
			results.values = null;
		}
		return results;
	}

	@Override
	protected void publishResults(final CharSequence constraint, final FilterResults results) {
		final Cursor oldCursor = mClient.getCursor();

		if (results.values != null && results.values != oldCursor) {
			mClient.changeCursor((Cursor) results.values);
		}
	}

	@Override
	public CharSequence convertResultToString(Object resultValue) {
		return mClient.convertToString((Cursor) resultValue);
	}

	interface CursorFilterClient {
		Cursor getCursor();

		CharSequence convertToString(Cursor cursor);

		Cursor runQueryOnBackgroundThread(CharSequence constraint);

		void changeCursor(Cursor cursor);
	}
}