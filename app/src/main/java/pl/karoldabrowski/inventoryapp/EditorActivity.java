package pl.karoldabrowski.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import pl.karoldabrowski.inventoryapp.data.ProductContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri currentProductUri;
    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierNameEditText;
    private EditText supplierPhoneEditText;
    private String currentPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        nameEditText = (EditText) findViewById(R.id.product_name);
        priceEditText = (EditText) findViewById(R.id.product_price);
        quantityEditText = (EditText) findViewById(R.id.product_quantity);
        supplierNameEditText = (EditText) findViewById(R.id.product_supplier_name);
        supplierPhoneEditText = (EditText) findViewById(R.id.product_supplier_phone);

        if (currentProductUri != null) {
            setTitle("Edit product");
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        ImageButton quantityPlus = (ImageButton) findViewById(R.id.quantity_plus);
        quantityPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = quantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(quantityString)) {
                    quantityEditText.setText("1");
                } else {
                    int quantity = Integer.parseInt(quantityString);
                    quantity++;
                    quantityEditText.setText(String.valueOf(quantity));
                }
            }
        });

        ImageButton quantityMinus = (ImageButton) findViewById(R.id.quantity_minus);
        quantityMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = quantityEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(quantityString)) {
                    int quantity = Integer.parseInt(quantityString);
                    if (quantity > 0) {
                        quantity--;
                        quantityEditText.setText(String.valueOf(quantity));
                    }
                }
            }
        });

        Button callButton = (Button) findViewById(R.id.call_button);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + currentPhoneNumber));
                startActivity(intent);
            }
        });

        if(currentProductUri == null) {
            callButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save_product, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if(saveProduct()) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete_dialog_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.delete_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean saveProduct() {
        String productName = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierPhoneNumber = supplierPhoneEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, priceString);
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, quantityString);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);

        if (currentProductUri == null) {
            Uri newUri;

            try {
                newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected;

            try {
                rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    private void deleteProduct() {
        if(currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_QUANTITY,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(
                this,
                currentProductUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            int supplierPhone = cursor.getInt(supplierPhoneColumnIndex);

            nameEditText.setText(name);
            priceEditText.setText(String.valueOf(price));
            quantityEditText.setText(String.valueOf(quantity));
            supplierNameEditText.setText(supplierName);
            supplierPhoneEditText.setText(String.valueOf(supplierPhone));
            currentPhoneNumber = String.valueOf(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        supplierNameEditText.setText("");
        supplierPhoneEditText.setText("");
    }
}
