package com.cullendevelopment.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.cullendevelopment.android.inventoryapp.data.BookContract.BookEntry;


public class CatalogueActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //initialising the loader
    public static final int PRODUCT_LOADER = 0;

    // This is the Adapter being used to display the list's data.
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(CatalogueActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list_view_books);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        //Setup an adapter to create a list item for each row of product data in the cursor.
        //there is no product data yet (until the loader finishes) so pass in null for the cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //Setup item Click Listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogueActivity.this, EditorActivity.class);

                //Form the content URI that represents the specific product that was clicked on by
                //appending the id.(passed as input on this method) onto{@link BookEntry#CONTENT_URI.
                Uri currentProductUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                //set the Uri on the data field of the intent
                intent.setData(currentProductUri);

                //Start the intent
                startActivity(intent);
            }
        });


        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging purposes only.
     */
    private void insertProduct() {
         //Create a ContentValues object where column names are the keys,
         //and "War and Peace" attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_TYPE, 0);//0 = Books, 1 = Toys and games 3 = Art and crafts
        values.put(BookEntry.COLUMN_PRODUCT_NAME, "War and Peace");//dummy book title
        values.put(BookEntry.COLUMN_PRICE, 15.99); //price as REAL/Float
        values.put(BookEntry.COLUMN_QUANTITY, 15);// stock holding as integer
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, "Penguin");
        values.put(BookEntry.COLUMN_SUPPLIER_TELEPHONE, "0745 0988765");

        /*
        Insert a new row for war and peace into the provider using the ContentResolver.
        Use the {@link BookEntry#CONTENT_URI} to indicate that we want to insert
        into the Booksupplies.db bookshop database table.
        Receive the new content URI that will allow us to access War and Peace data in the future.
        */
       Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all products from the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v("CatalogueActivity", rowsDeleted + " rows deleted from product database");
        Toast.makeText(this, getString(R.string.all_entries_deleted),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
      getMenuInflater().inflate(R.menu.menu_catalogue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
           case R.id.action_insert_dummy_data:
               insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY};

        return new CursorLoader(this,// Parent activity
                BookEntry.CONTENT_URI,//Table to query
                projection,//projection to return
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }
}
