package pl.mobi.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.mobi.R;
import pl.mobi.ui.activities.ConfirmActivity;
import pl.mobi.ui.models.Order;

public class ConfirmOrderAdapter extends RecyclerView.Adapter<ConfirmOrderAdapter.ViewHolder> {
    private Context context;
    private List<Order> orderList;

    public ConfirmOrderAdapter(Context context, List<Order> orderList) {
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
//        holder.childName.setText(order.getChild());
        holder.pickupDate.setText(order.getPickupDate().toDate().toString());

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
        TextView childName, pickupDate;
        Button cancelButton, readyButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            childName = itemView.findViewById(R.id.childName);
            pickupDate = itemView.findViewById(R.id.pickupDate);
            cancelButton = itemView.findViewById(R.id.cancelButton);
            readyButton = itemView.findViewById(R.id.readyButton);
        }
    }
}
