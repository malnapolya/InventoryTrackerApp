package com.example.android.inventorytracker;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventorytracker.data.ProductContract.ProductEntry;
import com.example.android.inventorytracker.data.ProductDbHelper;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Database helper that will provide us access to the database
    private ProductDbHelper mDbHelper;

    private final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private final int LOADER_INT = 0;

    private String chewablePath = "android.resource://com.example.android.inventorytracker/drawable/chew_toy";

    private ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Set up that the fab opens the EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        // Temporary method to check if our db is ok
        mDbHelper = new ProductDbHelper(this);

        // Find listView to use for populating the data
        ListView listView = (ListView) findViewById(R.id.list);
        // Set up cursor adapter
        mCursorAdapter = new ProductCursorAdapter(this, null);
        // Attach cursor adapter to listview
        listView.setAdapter(mCursorAdapter);

        // Set up empty text view
        // Credit goes to https://www.freevector.com/vector-shelf-21403 for shelf image
        // Find empty view
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });

        // Initialize the loader manager
        getLoaderManager().initLoader(LOADER_INT, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Insert a new pet
                insertPet();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void insertPet() {

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Chew Toy");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, "10");
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Toys\'R\'Us");
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, "order@toysrus.com");
        values.put(ProductEntry.COLUMN_QUANTITY, 12);
        values.put(ProductEntry.COLUMN_PICTURE, chewablePath);

        Uri uriResult = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        Log.v(LOG_TAG, "new row inserted: " + uriResult);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies that we want all columns from the database
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        // Return a new (@link CursorLoader)
        // Use the (@link ProductEntry#CONTENT_URI) to access the product data
        return new CursorLoader(
                this,
                ProductEntry.CONTENT_URI,   // The content URI of the pets table
                projection,                 // The columns to refer to for each row
                null,               // Selection criteria
                null,           // Selection criteria
                null);              // The sort order for the returned rows

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}
