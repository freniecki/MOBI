package pl.mobi.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import pl.mobi.R;
import pl.mobi.ui.activities.CartActivity;
import pl.mobi.ui.models.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private Context context;
    private List<CartItem> cartItems;

    public CartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.productName.setText(item.getProductName());
        holder.productPrice.setText(String.format("%.2f zł", item.getProductPrice()));
        holder.quantityText.setText(String.valueOf(item.getQuantity()));

        holder.plusButton.setOnClickListener(v -> {
            int quantity = item.getQuantity() + 1;
            item.setQuantity(quantity);
            holder.quantityText.setText(String.valueOf(quantity));
            notifyItemChanged(position);
            updateTotalPrice();
        });

        holder.minusButton.setOnClickListener(v -> {
            int quantity = item.getQuantity();
            if (quantity > 1) {
                quantity--;
                item.setQuantity(quantity);
                holder.quantityText.setText(String.valueOf(quantity));
                notifyItemChanged(position);
                updateTotalPrice();
            }
        });

        holder.binButton.setOnClickListener(v -> {
            cartItems.remove(position);
            notifyItemRemoved(position);
            updateTotalPrice();
        });
    }

    private void updateTotalPrice() {
        double total = CartActivity.calculateTotal(cartItems);
        TextView totalPrice = ((CartActivity) context).findViewById(R.id.totalPrice);
        totalPrice.setText(String.format("Suma: %.2f zł", total));
    }


    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, quantityText;
        ImageButton minusButton, plusButton, binButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantityText = itemView.findViewById(R.id.quantityText);
            minusButton = itemView.findViewById(R.id.minusButton);
            plusButton = itemView.findViewById(R.id.plusButton);
            binButton = itemView.findViewById(R.id.binButton);
        }
    }
}
