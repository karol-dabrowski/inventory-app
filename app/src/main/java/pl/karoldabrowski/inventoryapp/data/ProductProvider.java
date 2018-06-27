package pl.karoldabrowski.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import pl.karoldabrowski.inventoryapp.R;

public class ProductProvider extends ContentProvider {

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final int DECREMENT_ID = 201;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private ProductDbHelper dbHelper;

    static {
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_DECREMENT + "/#", DECREMENT_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;
        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        validateProductName(name);

        String priceString = values.getAsString(ProductContract.ProductEntry.COLUMN_PRICE);
        validateProductPrice(priceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, Double.parseDouble(priceString));

        String quantityString = values.getAsString(ProductContract.ProductEntry.COLUMN_QUANTITY);
        validateProductQuantity(quantityString);
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, Integer.parseInt(quantityString));

        String supplierName = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
        validateProductSupplierName(supplierName);

        String supplierPhoneString = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        validateProductSupplierPhone(supplierPhoneString);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, Integer.parseInt(supplierPhoneString));

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long id = database.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        if (id == -1) {
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = uriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, values, selection, selectionArgs);
            case DECREMENT_ID:
                long id = ContentUris.parseId(uri);
                return decrementProductQuantity(id);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }

        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        validateProductName(name);

        String priceString = values.getAsString(ProductContract.ProductEntry.COLUMN_PRICE);
        validateProductPrice(priceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, Double.parseDouble(priceString));

        String quantityString = values.getAsString(ProductContract.ProductEntry.COLUMN_QUANTITY);
        validateProductQuantity(quantityString);
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, Integer.parseInt(quantityString));

        String supplierName = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
        validateProductSupplierName(supplierName);

        String supplierPhoneString = values.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        validateProductSupplierPhone(supplierPhoneString);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, Integer.parseInt(supplierPhoneString));

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private int decrementProductQuantity(long id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String[] projection = { ProductContract.ProductEntry.COLUMN_QUANTITY };
        String selection = ProductContract.ProductEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(id) };
        Cursor cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection,
                selection, selectionArgs, null, null, null);
        if(cursor.moveToFirst()) {
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);
            int quantity = cursor.getInt(quantityColumnIndex);

            if(quantity > 0) {
                String sqlStatement = "UPDATE " + ProductContract.ProductEntry.TABLE_NAME +
                        " SET " + ProductContract.ProductEntry.COLUMN_QUANTITY + " = " +
                        ProductContract.ProductEntry.COLUMN_QUANTITY + " - 1 WHERE " +
                        ProductContract.ProductEntry._ID + "=?";
                database.execSQL(sqlStatement, selectionArgs);
                getContext().getContentResolver().notifyChange(ProductContract.ProductEntry.CONTENT_URI, null);
                return 1;
            }
        }

        return 0;
    }

    private void validateProductName(String productName) {
        if(TextUtils.isEmpty(productName)) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_name_is_required_text));
        } else if (productName.length() < 3) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_name_is_too_short_text));
        } else if (productName.length() > 10) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_name_is_too_long_text));
        }
    }

    private void validateProductPrice(String productPrice) {
        if(TextUtils.isEmpty(productPrice)) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_price_is_required_text));
        }

        double price = Double.parseDouble(productPrice);
        if(price <= 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_price_must_be_positive_text));
        }
    }

    private void validateProductQuantity(String productQuantity) {
        if(TextUtils.isEmpty(productQuantity)) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_quantity_is_required_text));
        }
    }

    private void validateProductSupplierName(String productSupplierName) {
        if(TextUtils.isEmpty(productSupplierName)) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_supplier_name_is_required_text));
        }
    }

    private void validateProductSupplierPhone(String productSupplierPhone) {
        if(TextUtils.isEmpty(productSupplierPhone)) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_supplier_phone_is_required_text));
        } else if(productSupplierPhone.length() != 9) {
            throw new IllegalArgumentException(getContext().getString(R.string.product_supplier_phone_must_have_9_digits_text));
        }
    }
}
