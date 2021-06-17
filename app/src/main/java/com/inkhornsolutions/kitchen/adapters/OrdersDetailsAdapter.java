package com.inkhornsolutions.kitchen.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OrdersDetailsAdapter extends RecyclerView.Adapter<OrdersDetailsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<OrdersModelClass> ordersDetails = new ArrayList<>();

    public OrdersDetailsAdapter(Context context, ArrayList<OrdersModelClass> ordersDetails) {
        this.context = context;
        this.ordersDetails = ordersDetails;
    }

    @NonNull
    @NotNull
    @Override
    public OrdersDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_details_adapter_layout, parent, false);
        return new OrdersDetailsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull OrdersDetailsAdapter.ViewHolder holder, int position) {
        OrdersModelClass ordersModelClass = ordersDetails.get(position);

        holder.tvItemName.setText(ordersModelClass.getItemName());
        holder.tvItemPrice.setText(ordersModelClass.getPrice());
        holder.tvItemCount.setText(ordersModelClass.getItems_Count());
        holder.tvItemTotalPrice.setText(ordersModelClass.getFinalPrice());
    }

    @Override
    public int getItemCount() {
        return ordersDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItemName, tvItemPrice, tvItemCount, tvItemTotalPrice;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvItemName = (TextView) itemView.findViewById(R.id.tvItemName);
            tvItemPrice = (TextView) itemView.findViewById(R.id.tvItemPrice);
            tvItemCount = (TextView) itemView.findViewById(R.id.tvItemCount);
            tvItemTotalPrice = (TextView) itemView.findViewById(R.id.tvItemTotalPrice);

        }
    }
}
