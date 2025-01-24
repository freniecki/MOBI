package pl.mobi.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pl.mobi.R;
import pl.mobi.ui.models.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;

    public OrderAdapter(Context context, List<Order> orderList) {
        db = FirebaseFirestore.getInstance();
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
        db.collection("users").document(order.getParentId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.parent.setText("Rodzic: " + documentSnapshot.getString("email"));
                    } else {
                        holder.parent.setText("Rodzic: nie-znaleziono emaila na bazie id");
                    }
                });

        holder.status.setText("Status: " + order.getStatus());

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
        String orderId = order.getOrderId();
        Bitmap qrCodeBitmap = generateQRCode(orderId);
        holder.qrCodeImageView.setImageBitmap(qrCodeBitmap);
    }

    public Bitmap generateQRCode(String orderId) {
        try {
            if (orderId == null || orderId.isEmpty()) {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.minus);
            }
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(orderId, BarcodeFormat.QR_CODE, 200, 200);
        } catch (WriterException e) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.plus);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView qrCodeImageView;
        TextView pickupDate, status, parent, child;
        RecyclerView recyclerViewProducts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupDate = itemView.findViewById(R.id.pickupDate);
            status = itemView.findViewById(R.id.statusText);
            child = itemView.findViewById(R.id.childName);
            parent = itemView.findViewById(R.id.parentName);
            recyclerViewProducts = itemView.findViewById(R.id.recyclerViewProducts);
            qrCodeImageView = itemView.findViewById(R.id.qrCodeImageView);
        }
    }
}
