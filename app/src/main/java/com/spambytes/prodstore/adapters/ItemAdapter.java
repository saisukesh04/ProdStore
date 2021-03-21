package com.spambytes.prodstore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.spambytes.prodstore.R;
import com.spambytes.prodstore.database.ItemDatabase;
import com.spambytes.prodstore.models.Item;

import java.util.List;

import static com.spambytes.prodstore.MainActivity.no_items_text;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private Context context;
    private final List<Item> itemList;

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_layout, parent, false);
        if (itemList.size() == 0){
            no_items_text.setVisibility(View.VISIBLE);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemName.setText(item.getItemName());
        holder.itemQuantityText.setText(String.valueOf(item.getQuantity()));
        String date = String.valueOf(item.getDateOfExpiry());
        String expiry = date.substring(0,10) + " " + date.substring(date.length() - 4);
        holder.expiryText.setText(expiry);
        holder.main_item_card.setOnLongClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Yes", (paramDialogInterface, paramInt) -> {
                        ItemDatabase.getInstance(context).ItemDao().deleteItem(item.getPrimary_key());
                        itemList.remove(item);
                        notifyDataSetChanged();
                    }).setNegativeButton("No", (dialog, which) ->
                        Toast.makeText(context, "Please be sure from next time :)", Toast.LENGTH_LONG).show())
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemName, expiryText, itemQuantityText;
        private ConstraintLayout main_item_card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemName = itemView.findViewById(R.id.itemName);
            expiryText = itemView.findViewById(R.id.expiryText);
            itemQuantityText = itemView.findViewById(R.id.itemQuantityText);
            main_item_card = itemView.findViewById(R.id.main_item_card);
        }
    }
}
