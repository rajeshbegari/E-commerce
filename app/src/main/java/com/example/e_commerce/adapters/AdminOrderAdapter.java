package com.example.e_commerce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_commerce.R;
import com.example.e_commerce.models.Order;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    private OrderActionListener listener;
    private FirebaseFirestore db;

    public interface OrderActionListener {
        void onUpdateStatus(Order order, String newStatus);
        void onViewDetails(Order order);
    }

    public AdminOrderAdapter(Context context, List<Order> orders, OrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdText;
        private TextView customerIdText;
        private TextView dateText;
        private TextView itemCountText;
        private TextView totalAmountText;
        private TextView statusText;
        private MaterialButton viewDetailsButton;
        private MaterialButton updateStatusButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            customerIdText = itemView.findViewById(R.id.customerIdText);
            dateText = itemView.findViewById(R.id.dateText);
            itemCountText = itemView.findViewById(R.id.itemCountText);
            totalAmountText = itemView.findViewById(R.id.totalAmountText);
            statusText = itemView.findViewById(R.id.statusText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            updateStatusButton = itemView.findViewById(R.id.updateStatusButton);
        }

        void bind(Order order) {
            orderIdText.setText("Order #" + order.getOrderId());
            dateText.setText(dateFormat.format(new Date(order.getTimestamp())));
            itemCountText.setText(order.getItems().size() + " items");
            totalAmountText.setText(String.format("$%.2f", order.getTotalAmount()));
            statusText.setText("Status: " + order.getStatus());

            // Fetch and set customer name
            db.collection("users").document(order.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String customerName = documentSnapshot.getString("name");
                    customerIdText.setText("Customer: " + (customerName != null ? customerName : "N/A"));
                })
                .addOnFailureListener(e -> {
                    customerIdText.setText("Customer: N/A");
                });

            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(order);
                }
            });

            updateStatusButton.setOnClickListener(v -> {
                if (listener != null) {
                    String newStatus = getNextStatus(order.getStatus());
                    listener.onUpdateStatus(order, newStatus);
                }
            });
        }

        private String getNextStatus(String currentStatus) {
            switch (currentStatus) {
                case "PAID":
                    return "PROCESSING";
                case "PROCESSING":
                    return "SHIPPED";
                case "SHIPPED":
                    return "DELIVERED";
                default:
                    return "PAID";
            }
        }
    }
} 