package pl.mobi.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import pl.mobi.ui.models.CartItem;
import pl.mobi.ui.models.Product;
import pl.mobi.ui.utils.CartManager;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productPrice.setText("Cena: " + String.format("%.2f zł", product.getPrice()));

        holder.addToCartButton.setOnClickListener(v -> {
            CartManager.getInstance().addItem(new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    1
            ));
        });

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(product.getImg());

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the image using Glide after retrieving the download URL
                Glide.with(holder.productImageView.getContext())
                        .load(uri.toString())
                        .into(holder.productImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure (e.g., show a placeholder image or handle error)
                Glide.with(holder.productImageView.getContext())
                        .load(R.drawable.no_image) // Assuming you have a placeholder image
                        .into(holder.productImageView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        Button addToCartButton;
        ImageView productImageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImageView = itemView.findViewById(R.id.productImg);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }
}
