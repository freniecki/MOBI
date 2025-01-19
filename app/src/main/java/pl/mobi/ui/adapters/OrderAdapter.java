package pl.mobi.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        holder.status.setText("Status: " + order.getStatus());
        holder.child.setText("Dziecko: " + order.getChildId());
        holder.parent.setText("Rodzic: " + order.getParentId());

        // Set up nested RecyclerView for products
        OrderProductAdapter productAdapter = new OrderProductAdapter(context, order.getItems());
        holder.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewProducts.setAdapter(productAdapter);

        if (order.getPickupDate() != null) {
            Date date = order.getPickupDate().toDate(); // Convert Timestamp to Date
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()); // Set desired format
            String formattedDate = sdf.format(date); // Format the Date
            holder.pickupDate.setText("Data odbioru: " + formattedDate); // Display formatted date
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pickupDate, status, parent, child;
        RecyclerView recyclerViewProducts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupDate = itemView.findViewById(R.id.pickupDate);
            status = itemView.findViewById(R.id.statusText);
            child = itemView.findViewById(R.id.childName);
            parent = itemView.findViewById(R.id.parentName);
            recyclerViewProducts = itemView.findViewById(R.id.recyclerViewProducts);
        }
    }
}
