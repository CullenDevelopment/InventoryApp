package com.cullendevelopment.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private BookContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.cullendevelopment.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.cullendevelopment.android.inventoryapp/bookshop/ is a valid path for
     * looking at bookshop data. content://com.cullendevelopment.android.inventoryapp/bookshop/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_BOOKSHOP = "bookshop";

    /**
     * Inner class that defines constant values for the bookshop products database table.
     * Each entry in the table represents a single product.
     */
    public static final class BookEntry implements BaseColumns {

        /** The content URI to access the product data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKSHOP);

        /** Name of database table for Products */
        public final static String TABLE_NAME = "bookshop";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKSHOP;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKSHOP;

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Type of product
         *
         * The only possible values are {@link #BOOKS}, {@link #TOYS_AND_GAMES},
         * or {@link #ART_AND_CRAFTS}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_TYPE = "product_type";

        /**
         * Returns whether or not the given gender is {@link #BOOKS}, {@link #TOYS_AND_GAMES},
         * or {@link #ART_AND_CRAFTS}.
         */
        public static boolean isValidType(int productType) {
            if (productType == BOOKS || productType == TOYS_AND_GAMES || productType == ART_AND_CRAFTS) {
                return true;
            }
            return false;
        }

        /**
         * Product name
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * Price of book
         *
         * Type: TEXT/REAL
         */
        public final static String COLUMN_PRICE = "price";

        /**
         * Quantity held
         *
         * Type: INTEGER
         */
        public final static String COLUMN_QUANTITY = "quantity";

        /**
         * Supplier Name
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_NAME = "supplier";

        /**
         * Supplier telephone number
         *
         * Type: entered as a number but formatted as string
         */
        public final static String COLUMN_SUPPLIER_TELEPHONE = "supplier_number";

        /**
         * Possible values for the Product type.
         */
        public static final int BOOKS = 0;
        public static final int TOYS_AND_GAMES = 1;
        public static final int ART_AND_CRAFTS = 2;

    }
}
