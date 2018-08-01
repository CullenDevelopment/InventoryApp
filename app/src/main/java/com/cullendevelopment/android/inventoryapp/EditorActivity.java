package com.cullendevelopment.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import com.cullendevelopment.android.inventoryapp.data.BookContract.BookEntry;

/**
 * Allows user to create a new bookshop product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    // This is the Adapter being used to display the list's data.
    //ProductCursorAdapter mCursorAdapter;

    //initialising the loader
    public static final int EXISTING_PRODUCT_LOADER = 0;

    //declaring global variable quantity
    int quantity = 0;

    //This string holds the current supplier telephone number
    private String currentSupplierNumber;

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the quantity of products available
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the product's supplier
     */
    private EditText mSupplierNameEditText;

    /**
     * EditText field to enter the product suppliers telephone number
     */
    private EditText mSupplierPhoneEditText;

    /**
     * EditText field to enter the products type
     */
    private Spinner mProductTypeSpinner;

    /**
     * Product types. The possible valid values are in the BookContract.java file:
     * {@link BookEntry#BOOKS}, {@link BookEntry#TOYS_AND_GAMES}, or
     * {@link BookEntry#ART_AND_CRAFTS}.
     */
    private int mProductType = BookEntry.BOOKS;

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        final Button increment = findViewById(R.id.increment);
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mQuantityEditText.getText())) {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                }
                mQuantityEditText.setText(String.valueOf(quantity + 1));
                Toast.makeText(getApplicationContext(), getString(R.string.increment_button_pressed), Toast.LENGTH_SHORT).show();
            }
        });

        final Button decrement = findViewById(R.id.decrement);
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mQuantityEditText.getText())) {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                }
                if (quantity == 0) {
                    quantity = 0;
                    Toast.makeText(getApplicationContext(), getString(R.string.zero_quantity_reached),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mQuantityEditText.setText(String.valueOf(quantity - 1));
                    Toast.makeText(getApplicationContext(), getString(R.string.decrement_button_pressed), Toast.LENGTH_SHORT).show();
                }

            }
        });

        // set up telephone button
        final ImageButton phoneButton = findViewById(R.id.supplier_phone);
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + currentSupplierNumber));
                if (intent.resolveActivity(getPackageManager()) != null)

                    startActivity(intent);
            }
        });

        /*
        Examine the intent that was used to launch this activity in order to figure out
        if we are creating a new product or editing an existing product.
         */

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        /*
        Set Title of EditorActivity depending on which situation we have.
        If the EditorActivity was opened with the ListView item, then we will
        have the Uri of product so change Title to say "Edit a product" otherwise
        if this is a new product, uri is null so change bar to say "Add a product"
         creating a new product.
         */
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit product"
            setTitle(getString(R.string.editor_activity_title_edit_a_product));

            /*
             Initialize a loader to read the product data from the database
             and display the current values in the editor
             */
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductTypeSpinner =  findViewById(R.id.spinner_product_type);
        mNameEditText =  findViewById(R.id.edit_product_name);
        mPriceEditText =  findViewById(R.id.edit_product_price);
        mQuantityEditText =  findViewById(R.id.edit_quantity);
        mSupplierNameEditText =  findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText =  findViewById(R.id.edit_supplier_phone);

        /*
        Setup OnTouchListeners on all the input fields, so we can determine if the user
         has touched or modified them. This will let us know if there are unsaved changes
         or not, if the user tries to leave the editor without saving.
         */
        mProductTypeSpinner.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of product
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter productSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_product_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        productSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mProductTypeSpinner.setAdapter(productSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mProductTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.toys_and_games))) {
                        mProductType = BookEntry.TOYS_AND_GAMES;
                    } else if (selection.equals(getString(R.string.art_and_crafts))) {
                        mProductType = BookEntry.ART_AND_CRAFTS;
                    } else {
                        mProductType = BookEntry.BOOKS;
                    }
                }
            }
            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mProductType = BookEntry.BOOKS;
            }
        });
    }

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {
        /*
          Read from input fields
           Use trim to eliminate leading or trailing white space
         */
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        /*
           Check if this is supposed to be a new product
           and check if all the fields in the editor are blank
         */
        if (mCurrentProductUri == null &&
                mProductType == BookEntry.BOOKS &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierNameString)
                && TextUtils.isEmpty(supplierPhoneString)) {
            /*
               Since no fields were modified, we can return early without creating a new product.
               No need to create ContentValues and no need to do any ContentProvider operations.
             */
            return;
        }

        /*
          If a user omits to fill one or more of the product fields, a toast message will pop up
          and the user will be taken back to the main activity without saving any input details.
          The user will have to start the input again but the toast message tells them why.
         */
        if (TextUtils.isEmpty(nameString)){
            Toast.makeText(this, getString(R.string.add_product_name),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if(TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.add_product_price),
                    Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.add_product_quantity),
                    Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.add_supplier_name),
                    Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(supplierPhoneString)) {
            Toast.makeText(this, getString(R.string.add_supplier_phone_number),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        /*
           Create a ContentValues object where column names are the keys,
           and product attributes from the editor are the values.
         */
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_TYPE, mProductType);
        values.put(BookEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(BookEntry.COLUMN_PRICE, priceString);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(BookEntry.COLUMN_SUPPLIER_TELEPHONE, supplierPhoneString);

        /*
           If the quantity is not provided by the user, don't try to parse the string into an
           integer value. Use 0 by default.
         */
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(BookEntry.COLUMN_QUANTITY, quantity);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            /*
               This is a NEW product, so insert a new product into the provider,
               returning the content URI for the new product.
             */
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            /*
               Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
               and pass in the new ContentValues. Pass in null for the selection and selection args
               because mCurrentProductUri will already identify the correct row in the database that
               we want to modify.
             */
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
           Inflate the menu options from the res/menu/menu_editor.xml file.
           This adds menu items to the app bar.
         */
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                // Exit activity
                finish();
                return true;
                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                /*
                   If the product hasn't changed, continue with navigating up to parent activity
                   which is the {@link CatalogActivity}.
                 */
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                /*
                   Otherwise if there are unsaved changes, setup a dialog to warn the user.
                   Create a click listener to handle the user confirming that
                   changes should be discarded.
                 */
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        /*
           Otherwise if there are unsaved changes, setup a dialog to warn the user.
           Create a click listener to handle the user confirming that changes should be discarded.
         */
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        /*
           Since the editor shows all product attributes, define a projection that contains
           all columns from the Bookshop table
         */
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_TYPE,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_TELEPHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail out early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        /*
           Proceed with moving to the first row of the cursor and reading data from it
           (This should be the only row in the cursor)
         */
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int productTypeColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_TYPE);
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_TELEPHONE);

            // Extract out the value from the Cursor for the given column index
            int productType = cursor.getInt(productTypeColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            currentSupplierNumber = cursor.getString(supplierPhoneColumnIndex);

            /*
               Update the views on the screen with the values from the database

               ProductType is a dropdown spinner, so map the constant value from the database
               into one of the dropdown options (0 is Books, 1 is Toys and Games, 2 is Art and Crafts).
               Then call setSelection() so that option is displayed on screen as the current selection.
             */
            switch (productType) {
                case BookEntry.TOYS_AND_GAMES:
                    mProductTypeSpinner.setSelection(1);
                    break;
                case BookEntry.ART_AND_CRAFTS:
                    mProductTypeSpinner.setSelection(2);
                    break;
                default:
                    mProductTypeSpinner.setSelection(0);
                    break;
            }
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(currentSupplierNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductTypeSpinner.setSelection(0); // Select "Books"
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}

