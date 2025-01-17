package pl.mobi.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.mobi.R;
import pl.mobi.ui.models.CartItem;
import pl.mobi.ui.models.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.pickupDate.setText("Data odbioru: " + order.getPickupDate());
        holder.status.setText("Status: " + order.getStatus());

        // Set up nested RecyclerView for products
        OrderProductAdapter productAdapter = new OrderProductAdapter(context, order.getItems());
        holder.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewProducts.setAdapter(productAdapter);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pickupDate, status, parentId;
        RecyclerView recyclerViewProducts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupDate = itemView.findViewById(R.id.pickupDate);
            status = itemView.findViewById(R.id.statusText);
            recyclerViewProducts = itemView.findViewById(R.id.recyclerViewProducts);
        }
    }
}
