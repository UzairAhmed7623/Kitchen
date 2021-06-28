package com.inkhornsolutions.kitchen.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inkhornsolutions.kitchen.OrderDetails;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RecentOrdersAdapter extends RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder> {

    private Context context;
    private ArrayList<OrdersModelClass> Orders = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;

    public RecentOrdersAdapter(Context context, ArrayList<OrdersModelClass> orders) {
        this.context = context;
        Orders = orders;
    }

    @NonNull
    @Override
    public RecentOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recent_orders_adapter_layout, parent, false);
        return new RecentOrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        OrdersModelClass ordersModelClass = Orders.get(position);

        String resId = ordersModelClass.getResId();
        String resName = ordersModelClass.getResName();
        String totalPrice = ordersModelClass.getTotalPrice();
        String status = ordersModelClass.getStatus();
        String date = ordersModelClass.getDate();
        Double lat = ordersModelClass.getLat();
        Double lng = ordersModelClass.getLng();
        String orderId = ordersModelClass.getOrderId();
        String userId = ordersModelClass.getUserId();

        holder.tvResNameOrders.setText("Order ID: " + orderId);
        holder.tvGradTotalOrders.setText("Price: " + totalPrice);
        holder.tvDateOrders.setText("Date: " + date);

        switch (status) {
            case "Pending":
                holder.tvStatusOrders.setTextColor(Color.BLACK);
                break;
            case "In progress":
                holder.tvStatusOrders.setTextColor(context.getColor(R.color.Green));
                break;
            case "Rejected":
                holder.tvStatusOrders.setTextColor(Color.RED);
                break;
            case "Dispatched":
                holder.tvStatusOrders.setTextColor(context.getColor(R.color.Green));
                break;
            case "Completed":
                holder.tvStatusOrders.setTextColor(context.getColor(R.color.Green));
                break;
        }

        holder.tvStatusOrders.setText("Status: " + status);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderDetails.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("resName", resName);
                intent.putExtra("resId", resId);
                intent.putExtra("orderId", orderId);
                intent.putExtra("userId", userId);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);

                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return Orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvStatusOrders, tvResNameOrders, tvDateOrders, tvGradTotalOrders;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseFirestore = FirebaseFirestore.getInstance();
            tvStatusOrders = (TextView) itemView.findViewById(R.id.tvStatusOrders);
            tvResNameOrders = (TextView) itemView.findViewById(R.id.tvResNameOrders);
            tvDateOrders = (TextView) itemView.findViewById(R.id.tvDateOrders);
            tvGradTotalOrders = (TextView) itemView.findViewById(R.id.tvGradTotalOrders);
        }
    }
}
