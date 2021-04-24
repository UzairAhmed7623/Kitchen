package com.inkhornsolutions.kitchen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;

public class MemeberComOrders extends RecyclerView.Adapter<MemeberComOrders.ViewHolder> {

    ArrayList<OrdersModelClass> arrayListMember;

    public MemeberComOrders(ArrayList<OrdersModelClass> arrayListMember) {
        this.arrayListMember = arrayListMember;
    }

    @NonNull
    @Override
    public MemeberComOrders.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.memeber_com_orders, parent, false);
        return new MemeberComOrders.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemeberComOrders.ViewHolder holder, int position) {
        OrdersModelClass ordersModelClass = arrayListMember.get(position);

        String id = ordersModelClass.getId();
        String itemName = ordersModelClass.getItemName();
        String price = ordersModelClass.getPrice();
        String itemCount = ordersModelClass.getItems_Count();
        String finalPrice = ordersModelClass.getFinalPrice();
        String pId = ordersModelClass.getpId();

        holder.tvItemNameComOrders.setText(itemName);
        holder.tvOrderDateComOrders.setText("Date: " + pId);
        holder.tvItemPriceComOrders.setText("Price: " + price);
        holder.tvItemCountComOrders.setText(itemCount);
        holder.tvTotalComOrders.setText(finalPrice);
    }

    @Override
    public int getItemCount() {
        return arrayListMember.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItemNameComOrders, tvItemPriceComOrders, tvItemCountComOrders, tvTotalComOrders, tvOrderDateComOrders;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemNameComOrders = (TextView) itemView.findViewById(R.id.tvItemNameComOrders);
            tvItemPriceComOrders = (TextView) itemView.findViewById(R.id.tvItemPriceComOrders);
            tvItemCountComOrders = (TextView) itemView.findViewById(R.id.tvItemCountComOrders);
            tvTotalComOrders = (TextView) itemView.findViewById(R.id.tvTotalComOrders);
            tvOrderDateComOrders = (TextView) itemView.findViewById(R.id.tvOrderDateComOrders);
        }
    }
}
