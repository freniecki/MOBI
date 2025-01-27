package pl.mobi.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.mobi.R;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.ViewHolder> {
    private List<ProductStat> productList;

    public StatsAdapter(List<ProductStat> productList) {
        this.productList = productList;
    }

    public void updateData(List<ProductStat> newData) {
        this.productList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_stats, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductStat product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productQuantity.setText(String.valueOf(product.getQuantity()));
        holder.productTotalPrice.setText(String.format("%.2f", product.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity, productTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            productTotalPrice = itemView.findViewById(R.id.productTotalPrice);
        }
    }

    public static class ProductStat {
        private String name;
        private int quantity;
        private double totalPrice;

        public ProductStat(String name, int quantity, double totalPrice) {
            this.name = name;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotalPrice() {
            return totalPrice;
        }
    }
}
