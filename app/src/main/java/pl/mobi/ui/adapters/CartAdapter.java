package pl.mobi.ui.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.mobi.R;
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
        holder.quantityEditText.setText(String.valueOf(item.getQuantity()));

        holder.quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int quantity = Integer.parseInt(s.toString());
                    item.setQuantity(quantity);
                } catch (NumberFormatException e) {
                    item.setQuantity(1); // Domyślna wartość
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        EditText quantityEditText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantityEditText = itemView.findViewById(R.id.quantityEditText);
        }
    }
}
