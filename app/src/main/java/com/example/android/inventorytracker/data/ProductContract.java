package com.example.android.inventorytracker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {

    /** The Content Authority String**/
    public static final String CONTENT_AUTHORITY = "com.example.android.inventorytracker";

    /** The base Content URI**/
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Define the constant for the ProductEntry table**/
    public static final String PATH_PRODUCTS = "products";

    public static abstract class ProductEntry implements BaseColumns {

        /** Name of the database**/
        public static final String TABLE_NAME = "products";

        /** Concatenate the uri path**/
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PRODUCTS);

        /**
         * Unique id number for the product.
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of product
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME = "name";

        /**
         * Price of product
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Supplier name
         *
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_NAME = "supplierName";

        /**
         * Supplier email
         *
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_EMAIL = "supplierEmail";

        /**
         * Available quantity
         *
         * Type: INTEGER
         */
        public static final String COLUMN_QUANTITY = "quantity";

        /**
         * Picture file
         *
         * Type: String
         */
        public static final String COLUMN_PICTURE = "picture";

        /**
         * The MIME type of the (@link #CONTENT_URI) for a list of products
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the (@link #CONTENT_URI) for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

    }
}
