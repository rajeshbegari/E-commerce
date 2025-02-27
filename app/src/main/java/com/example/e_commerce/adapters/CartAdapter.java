package com.example.e_commerce.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_commerce.R;
import com.example.e_commerce.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private final CartItemListener itemListener;
    private final CartRemoveListener removeListener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
    }

    public interface CartRemoveListener {
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, CartItemListener itemListener, CartRemoveListener removeListener) {
        this.cartItems = cartItems;
        this.itemListener = itemListener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartItems.get(position));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView productSize;
        private final TextView quantityText;
        private final ImageButton decreaseQuantity;
        private final ImageButton increaseQuantity;
        private final ImageButton removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productSize = itemView.findViewById(R.id.productSize);
            quantityText = itemView.findViewById(R.id.quantityText);
            decreaseQuantity = itemView.findViewById(R.id.decreaseQuantity);
            increaseQuantity = itemView.findViewById(R.id.increaseQuantity);
            removeButton = itemView.findViewById(R.id.removeButton);
        }

        void bind(CartItem item) {
            productName.setText(item.getName());
            productPrice.setText(String.format("$%.2f", item.getPrice()));
            productSize.setText("Size: " + item.getSize());
            quantityText.setText(String.valueOf(item.getQuantity()));

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage);

            decreaseQuantity.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    itemListener.onQuantityChanged(item, item.getQuantity() - 1);
                }
            });

            increaseQuantity.setOnClickListener(v -> 
                itemListener.onQuantityChanged(item, item.getQuantity() + 1));

            removeButton.setOnClickListener(v -> removeListener.onItemRemoved(item));
        }
    }
} 