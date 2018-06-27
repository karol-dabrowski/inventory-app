package pl.karoldabrowski.inventoryapp;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import pl.karoldabrowski.inventoryapp.data.ProductContract;

public class ProductCursorAdapter extends CursorAdapter {

    private final Context context;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        Button addButton = (Button) view.findViewById(R.id.add_button);

        int idColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);

        final int id = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        nameTextView.setText(productName);

        double price = cursor.getDouble(priceColumnIndex);
        priceTextView.setText(String.format("%.2f", price));

        int quantity = cursor.getInt(quantityColumnIndex);
        quantityTextView.setText(String.valueOf(quantity));

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri clickedProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.DECREMENT_URI, id);
                int result = context.getContentResolver().update(clickedProductUri, null, null, null);

                if(result == 0) {
                    Toast.makeText(context, context.getString(R.string.product_quantity_can_not_be_updated),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.product_quantity_updated),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
