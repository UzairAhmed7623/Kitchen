package com.inkhornsolutions.kitchen.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.inkhornsolutions.kitchen.OrderDetails;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;

public class InProgressOrdersAdapter extends RecyclerView.Adapter<InProgressOrdersAdapter.ViewHolder>{

    private final Context context;
    private ArrayList<OrdersModelClass> orders = new ArrayList<>();

    public InProgressOrdersAdapter(Context context, ArrayList<OrdersModelClass> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.in_progress_orders_adapter_layout, parent, false);
        return new InProgressOrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrdersModelClass ordersModelClass = orders.get(position);

        String resId = ordersModelClass.getResId();
        String resName = ordersModelClass.getResName();
        String subTotal = ordersModelClass.getSubTotal();
        String status = ordersModelClass.getStatus();
        String date = ordersModelClass.getDate();
        Double lat = ordersModelClass.getLat();
        Double lng = ordersModelClass.getLng();
        String orderId = ordersModelClass.getOrderId();
        String userId = ordersModelClass.getUserId();
        String promotedOrder = ordersModelClass.getPromotedOrder();

        holder.tvResNameOrders.setText("Order ID: " + orderId);
        holder.tvGradTotalOrders.setText("Price: " + subTotal);
        holder.tvDateOrders.setText("Date: " + date);

        switch (status) {
            case "Pending":
                holder.tvStatusOrders.setTextColor(context.getColor(R.color.pending));
                break;
            case "In progress":
                holder.tvStatusOrders.setTextColor(context.getColor(R.color.inProgress));
                break;
            case "Rejected":
                holder.tvStatusOrders.setTextColor(context.getColor(R.color.rejected));
                break;
            case "Dispatched":
                holder.tvStatusOrders.setTextColor(context.getColor(R.color.dispatched));
                break;
            case "Completed":
                holder.tvStatusOrders.setTextColor(context.getColor(R.color.completed));
                break;
        }

        holder.tvStatusOrders.setText("Status: " + status);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderDetails.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("resName", resName);
                intent.putExtra("resId", resId);
                intent.putExtra("orderId", orderId);
                intent.putExtra("userId", userId);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("promotedOrder", promotedOrder);

                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStatusOrders, tvResNameOrders, tvDateOrders, tvGradTotalOrders;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            tvStatusOrders = (TextView) itemView.findViewById(R.id.tvStatusOrders);
            tvResNameOrders = (TextView) itemView.findViewById(R.id.tvResNameOrders);
            tvDateOrders = (TextView) itemView.findViewById(R.id.tvDateOrders);
            tvGradTotalOrders = (TextView) itemView.findViewById(R.id.tvGradTotalOrders);
        }
    }
}
