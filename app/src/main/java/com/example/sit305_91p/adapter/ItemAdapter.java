package com.example.sit305_91p.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sit305_91p.R;
import com.example.sit305_91p.data.DatabaseHelper;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    private Cursor cursor;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(long id);
    }

    public ItemAdapter(Context context, Cursor cursor, OnItemClickListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return; // Bail if cursor is empty or invalid position
        }

        // Extract data from cursor
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_ID));
        String postType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_POST_TYPE));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ITEM_NAME));

        // Combine post type and name for display
        String itemText = postType + ": " + name;

        holder.tvItemDescription.setText(itemText);
        holder.itemView.setTag(id); // Store the item ID in the view's tag

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            long itemId = (long) v.getTag();
            if (listener != null) {
                listener.onItemClick(itemId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }

    // Method to swap the cursor and refresh the list
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        } else {
             notifyItemRangeRemoved(0, getItemCount()); // Clear the list display
        }
    }

    // ViewHolder class
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemDescription;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
        }
    }
}
