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
import pl.mobi.ui.activities.PickUpActivity;
import pl.mobi.ui.models.Order;

public class PickupAdapter extends RecyclerView.Adapter<PickupAdapter.ViewHolder> {
    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db;

    public PickupAdapter(Context context, List<Order> orderList) {
        db = FirebaseFirestore.getInstance();
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pickup, parent, false);
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

        holder.readyButton.setOnClickListener(v -> {
            if (context instanceof PickUpActivity) {
                ((PickUpActivity) context).updateOrderStatus(order.getOrderId(), "Odebrane");
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView child, pickupDate;
        Button readyButton;
        RecyclerView recyclerViewProducts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            child = itemView.findViewById(R.id.child);
            pickupDate = itemView.findViewById(R.id.pickupDate);
            readyButton = itemView.findViewById(R.id.readyButton);
            recyclerViewProducts = itemView.findViewById(R.id.recyclerViewProducts);
        }
    }
}
