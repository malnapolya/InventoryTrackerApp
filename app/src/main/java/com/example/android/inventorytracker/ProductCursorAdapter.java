package com.example.android.inventorytracker;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorytracker.data.ProductContract.ProductEntry;

import com.example.android.inventorytracker.data.ProductContract;

import java.util.List;

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new (@link ProductCursorAdapter)
     *
     * @param context The context
     * @param c The cursor from which you get the data
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /*flags*/);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet
     *
     * @param context app context
     * @param cursor The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    /**
     * This method binds the product data (in the current row pointed to by the cursor) to the given
     * list item layout. For example the name of the current product can be set on the name TetView
     * in the list item layout
     *
     * @param view  Existing view returned earlier by the newView method
     * @param context app context
     * @param cursor The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // Find fields that we want to populate
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity_available);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        // Set up sold button
        Button soldButton = (Button) view.findViewById(R.id.sold_button);

        // Extract properties from the cursor
        String nameString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        final int quantityInt = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
        int priceInt = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        // row id in database
        final int row_id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));

        nameView.setText(nameString);
        quantityView.setText(quantityInt+ " available");
        priceView.setText(priceInt+ " USD");

        soldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                final int position = listView.getPositionForView(parentRow);
                // Move to the correct position of the cursor
                cursor.moveToPosition(position);
                // Find the current available quantity
                int currentQuantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
                String currentProduct = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));

                // Only sell 1 if we have enough
                if (currentQuantity <= 0) {
                    Toast.makeText(listView.getContext(), "Not enough "+currentProduct +". Consider reordering.",Toast.LENGTH_SHORT).show();
                } else {
                    // Decrease the available quantity by 1
                    currentQuantity--;

                    // Update the view to display one less product
                    TextView quantityView = (TextView) parentRow.findViewById(R.id.quantity_available);
                    // TODO: Update in the database
                    Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, row_id);

                    // Add the new value to the content values object
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, currentQuantity);
                    int updatedRowCount = v.getContext().getContentResolver().update(currentProductUri, values, null, null);

                    if (updatedRowCount == 1) {
                        Toast.makeText(listView.getContext(), "Sold 1 " + currentProduct, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(listView.getContext(), "Failed to sell 1 " + currentProduct, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
//        final int position = cursor.getPosition();
//        soldBbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cursor.moveToPosition(position);
//                // Decrease quantity by 1 and modify it on the screen
//                int currentQuantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
//                if (currentQuantity<=0) {
//                    Toast.makeText(v.getContext(), "No product available \n Please place order", Toast.LENGTH_SHORT).show();
//                    return;
//                } else {
//                    currentQuantity--;
//                }
//
//                // TODO: Also modify it in the database
//            }
//        });
    }
}
