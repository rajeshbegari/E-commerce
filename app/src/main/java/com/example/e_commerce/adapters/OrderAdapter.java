package com.example.e_commerce.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_commerce.R;
import com.example.e_commerce.models.Order;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdText;
        private TextView dateText;
        private TextView itemCountText;
        private TextView totalAmountText;
        private TextView statusText;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            dateText = itemView.findViewById(R.id.dateText);
            itemCountText = itemView.findViewById(R.id.itemCountText);
            totalAmountText = itemView.findViewById(R.id.totalAmountText);
            statusText = itemView.findViewById(R.id.statusText);
        }

        public void bind(Order order) {
            orderIdText.setText("Order #" + order.getOrderId());
            dateText.setText(dateFormat.format(new Date(order.getTimestamp())));
            itemCountText.setText(order.getItems().size() + " items");
            totalAmountText.setText(String.format("$%.2f", order.getTotalAmount()));
            statusText.setText(order.getStatus());
        }
    }
} 