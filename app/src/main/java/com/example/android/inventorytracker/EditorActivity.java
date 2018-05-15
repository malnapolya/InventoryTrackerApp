package com.example.android.inventorytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventorytracker.data.ProductContract.ProductEntry;

import java.io.FileDescriptor;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field for the product name
     */
    private EditText mNameEditText;

    /**
     * EditText field for the product price
     */
    private EditText mPriceEditText;

    /**
     * Supplier name EditText
     */
    private EditText mSupplierNameEditText;

    /**
     * Supplier email EditText
     */
    private EditText mSupplierEmailEditText;

    /**
     * Available quantity EditText
     */
    private EditText mQuantityEditText;

    /**
     * Button for Order More
     */
    private Button mOrderButton;

    /**
     * Button for decrementing the quantity
     */
    private Button mDecrementButton;

    /**
     * Button for incrementing the quantity
     */
    private Button mIncrementButton;

    /**
     * EditText that holds the quantity to order
     */
    private EditText mOrderMoreEditText;

    /**
     * Button that opens the gallery to browse for the image
     */
    private Button mUploadImageButton;

    /**
     * The ImageView that shows the image
     */
    private ImageView mProductImageView;

    // The image that we want to display in the ImageView
    private Bitmap mCurrentImage;

    // Request code that will go with the open the gallery intent
    private static final int GALLERY_INTENT_REQUEST_CODE = 1;

    // The URI for the image source
    Uri mPhotoUri = null;

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private final int LOADER_INT = 0;

    // when you load the image
    private static final int PICK_IMAGE_REQUEST = 100;

    private int quantity = 0;

    private Uri currentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if (currentProductUri== null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(LOADER_INT,null,this);
        }



        // Find all relevant edit text fields
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_product_supplier_email);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mOrderButton = (Button) findViewById(R.id.button_order_more);
        mDecrementButton = (Button) findViewById(R.id.button_quantity_decrement);
        mIncrementButton = (Button) findViewById(R.id.button_quantity_increment);
        mOrderMoreEditText = (EditText) findViewById(R.id.quantity_to_order_edit);
        mUploadImageButton = (Button) findViewById(R.id.upload_picture_button);
        mProductImageView = (ImageView) findViewById(R.id.product_picture);

        // Click listener for decrementing the quantity with the - Button
        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // decrement the quantity by one if it is not 0 or less
                if (quantity<=0) {
                    return;
                } else {
                    quantity--;
                    mQuantityEditText.setText(""+quantity);
                }
            }
        });

        // Click listener for incrementing the quantity with the + Button
        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // incrementing the quantity by one and updating the view
                quantity++;
                mQuantityEditText.setText(""+quantity);
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderMore();
            }
        });

        mUploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setType("image/*");
                photoPickerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(photoPickerIntent,GALLERY_INTENT_REQUEST_CODE);
            }
        });

        // Listening to changes in the quantity text field
        mQuantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String quantityText = mQuantityEditText.getText().toString().trim();
                if (quantityText.length() != 0) {
                    quantity = Integer.parseInt(quantityText);
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCurrentImage != null) {
            mCurrentImage.recycle();
            mCurrentImage = null;
            System.gc();
        }
    }

    // After we opened the gallery, we will get back to this with a request code GALLERY_INTENT_REQUEST_CODE
    // If there was an image selected, then display it on the ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode== RESULT_OK) {
            if (requestCode == GALLERY_INTENT_REQUEST_CODE) {
                mPhotoUri = data.getData();
                if (mPhotoUri != null) {
                    try {
                        mCurrentImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mPhotoUri);
                        mProductImageView.setImageBitmap(mCurrentImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_save:
                // Save the product to the database
                // If save product returns true - meaning successful product save, or ready for
                // exit otherwise - then exit
                if (saveProduct()) {
                    // Exit activity
                    finish();
                }
                return true;
            case R.id.action_delete:
                //  Delete the current product
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        // Add if it is a new pet and everything is empty
        if (currentProductUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString) && quantityString.equals("0")) {
            Log.v(LOG_TAG,"Empty form");
            return true;
        }

        int priceInt = 0;
        if (!TextUtils.isEmpty(priceString)) {
            priceInt = Integer.parseInt(priceString);
        }

        int quantityInt = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantityInt = Integer.parseInt(quantityString);
        }

        // Perform validation of form
        // Check that the name is not null or empty
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.name_string_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check that the supplier is not null or empty
        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.supplier_name_string_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check that the supplier email is not null or empty
        if (TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, getString(R.string.supplier_email_string_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }


        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME,nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE,priceInt);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME,supplierNameString);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL,supplierEmailString);
        values.put(ProductEntry.COLUMN_QUANTITY,quantityInt);
        if (mPhotoUri!= null) {
            String photoUriString = mPhotoUri.toString();
            values.put(ProductEntry.COLUMN_PICTURE,photoUriString);
        }


        // If it was a new pet, insert, else update
        if (currentProductUri == null) {
            Uri uriResult = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (uriResult == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.error_inserting),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_saved),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int updatedRowCount = getContentResolver().update(currentProductUri, values, null, null);
            if (updatedRowCount == 1) {
                // If successfully inserted
                Toast.makeText(this, getString(R.string.successful_update),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.failed_update),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentProductUri != null) {
            int deletedRowCount = getContentResolver().delete(currentProductUri,null, null);
            if (deletedRowCount == 1) {
                // If successfully inserted
                Toast.makeText(this, getString(R.string.successful_deletion),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.failed_deletion),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Exit activity
        finish();
    }

    private void orderMore() {

        // Send an intent to an email client with the email address
        String [] supplierEmail = new String[]{mSupplierEmailEditText.getText().toString().trim()};
        String productName = mNameEditText.getText().toString().trim();
        String quantityToOrder = mOrderMoreEditText.getText().toString().trim();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL,supplierEmail);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Ordering "+quantityToOrder+ " of "+ productName);
        intent.putExtra(Intent.EXTRA_TEXT,"Dear "+ supplierName+ "! \n" +
                "I would like to order " + quantityToOrder + " pieces of your product: " + productName + "\n" +
                "Please kindly send the invoice to my attention! \n" +
                "Best regards, \n" +
                "The Inventory App Team");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.no_email_client), Toast.LENGTH_SHORT).show();
        }

    }

    // Hopefully this loads the image
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define projection that shows all attributes
        String [] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PICTURE};

        return new CursorLoader(
                this,
                currentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Get data from cursor
            String currentName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
            int currentPrice = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
            String currentSupplierName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME));
            String currentSupplierEmail = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL));
            int currentQuantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
            String currentProductImage = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PICTURE));

            mNameEditText.setText(currentName);
            mPriceEditText.setText(Integer.toString(currentPrice));
            mSupplierNameEditText.setText(currentSupplierName);
            mSupplierEmailEditText.setText(currentSupplierEmail);
            mQuantityEditText.setText(Integer.toString(currentQuantity));
            quantity = currentQuantity;
            if (currentProductImage!=null && !currentProductImage.equals("")) {
                try {
                    mPhotoUri = Uri.parse(currentProductImage);
                    // If the photoUri is of android.resource type
                    if (mPhotoUri.getScheme().equals("android.resource")) {
                        mProductImageView.setImageURI(mPhotoUri);
                    } else {
                        // this references the above method, hopefully this loads the image
                        mCurrentImage = getBitmapFromUri(mPhotoUri);
                        mProductImageView.setImageBitmap(mCurrentImage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierEmailEditText.setText("");
        mQuantityEditText.setText("");
    }
}
