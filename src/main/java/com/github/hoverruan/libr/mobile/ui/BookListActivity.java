package com.github.hoverruan.libr.mobile.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.hoverruan.libr.mobile.LibrConstants;
import static com.github.hoverruan.libr.mobile.LibrConstants.TAG;
import com.github.hoverruan.libr.mobile.R;
import com.github.hoverruan.libr.mobile.domain.Book;
import com.github.hoverruan.libr.mobile.domain.BookList;
import com.github.hoverruan.libr.mobile.domain.BookParser;
import com.github.hoverruan.libr.mobile.util.DownloadJsonTask;
import com.github.hoverruan.libr.mobile.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hover Ruan
 */
public class BookListActivity extends SherlockListActivity {

    private List<Book> books = new ArrayList<Book>();
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        refreshNewBooks();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(getString(R.string.search))
                .setIcon(R.drawable.ic_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add(getString(R.string.refresh))
                .setIcon(R.drawable.ic_refresh)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        refreshNewBooks();
                        return true;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Book book = books.get(position);
        Log.i(TAG, book.getName() + " selected");
    }

    private class DownloadBooksInfo extends DownloadJsonTask {
        @Override
        protected void onPostExecute(String booksInfoText) {
            if (booksInfoText != null) {
                BookList bookList = new BookParser().parseBookList(booksInfoText);
                if (bookList != null) {
                    books = bookList.getBooks();
                    showBookList();
                }
                hideProgressDialog();
            }
        }
    }

    private void refreshNewBooks() {
        if (isConnected()) {
            showProgressDialog();
            new DownloadBooksInfo().execute(LibrConstants.API_BOOKS);
        } else {
            ToastUtils.show(this, R.string.failed_not_connected);
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.update);
            progressDialog.setMessage(getString(R.string.fetching_books));
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    private void showBookList() {
        setListAdapter(new BookListAdapter(LayoutInflater.from(this), getCacheDir(), books));
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}
