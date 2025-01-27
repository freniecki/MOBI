package pl.mobi.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pl.mobi.R;
import pl.mobi.ui.activities.ConfirmActivity;
import pl.mobi.ui.models.Order;

public class ConfirmOrderAdapter extends RecyclerView.Adapter<ConfirmOrderAdapter.ViewHolder> {
    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db;

    public ConfirmOrderAdapter(Context context, List<Order> orderList) {
        db = FirebaseFirestore.getInstance();
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_confirm_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        if (order.getPickupDate() != null) {
            Date date = order.getPickupDate().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String formattedDate = sdf.format(date);
            holder.pickupDate.setText("Data odbioru: " + formattedDate);
        }

        db.collection("users")
                .document(order.getChildId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.child.setText("Dziecko: " + documentSnapshot.getString("email"));
                    } else {
                        holder.child.setText("Dziecko: nie-znaleziono emaila na bazie id");
                    }
                });

        OrderProductAdapter productAdapter = new OrderProductAdapter(context, order.getItems());
        holder.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewProducts.setAdapter(productAdapter);

        holder.cancelButton.setOnClickListener(v -> {
            if (context instanceof ConfirmActivity) {
                ((ConfirmActivity) context).updateOrderStatus(order.getOrderId(), "Anulowane");
            }
        });

        holder.readyButton.setOnClickListener(v -> {
            if (context instanceof ConfirmActivity) {
                ((ConfirmActivity) context).updateOrderStatus(order.getOrderId(), "Gotowe do odbioru");
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView child, pickupDate;
        Button cancelButton, readyButton;
        RecyclerView recyclerViewProducts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            child = itemView.findViewById(R.id.child);
            pickupDate = itemView.findViewById(R.id.pickupDate);
            cancelButton = itemView.findViewById(R.id.cancelButton);
            readyButton = itemView.findViewById(R.id.readyButton);
            recyclerViewProducts = itemView.findViewById(R.id.recyclerViewProducts);
        }
    }
}
