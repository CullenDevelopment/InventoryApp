package com.cullendevelopment.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cullendevelopment.android.inventoryapp.data.BookContract.BookEntry;
import java.text.DecimalFormat;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    private static DecimalFormat decimalFormat = new DecimalFormat("#.00");


    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        ImageView saleButton = view.findViewById(R.id.sale_button);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        //declare local variable quantityString
       String quantityAsString;

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        double price = cursor.getDouble(priceColumnIndex);
        final Integer quantity = cursor.getInt(quantityColumnIndex);
        quantityAsString = quantity.toString();
        int productIdIndex = cursor.getColumnIndex(BookEntry._ID);
        final String productId = cursor.getString(productIdIndex);

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        //format price to display correctly in list view
        priceTextView.setText("£" + decimalFormat.format(price));
        quantityTextView.setText(quantityAsString);
        //Create click handler to remove one product from stock when "Sale" button clicked
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reduce relevant stock levels by 1 when clicked
                Integer newQuantity;
                if(quantity >= 1){
                    newQuantity = quantity - 1;
                    //update database with new quantity after sale
                    quantityTextView.setText(newQuantity.toString());
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_QUANTITY, newQuantity);
                    //create new Uri for updated row
                    Uri updatedUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, Integer.valueOf(productId));
                    //Update product in database with new quantity
                    int rowsUpdated = context.getContentResolver().update(updatedUri,
                            values, null, null);

                }
            }
        });
    }
}
