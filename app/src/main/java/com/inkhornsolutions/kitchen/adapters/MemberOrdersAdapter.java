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

public class MemberOrdersAdapter extends RecyclerView.Adapter<MemberOrdersAdapter.ViewHolder> {

    ArrayList<OrdersModelClass> arrayListMember;

    public MemberOrdersAdapter(ArrayList<OrdersModelClass> arrayListMember) {
        this.arrayListMember = arrayListMember;
    }

    @NonNull
    @Override
    public MemberOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.member_orders_adapter, parent, false);
        return new MemberOrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberOrdersAdapter.ViewHolder holder, int position) {
        OrdersModelClass ordersModelClass = arrayListMember.get(position);

        String id = ordersModelClass.getId();
        String itemName = ordersModelClass.getItemName();
        String price = ordersModelClass.getPrice();
        String itemCount = ordersModelClass.getItems_Count();
        String finalPrice = ordersModelClass.getFinalPrice();
        String pId = ordersModelClass.getpId();

        holder.tvItemNameOrders.setText(itemName);
        holder.tvOrderDateOrders.setText("Date: " + pId);
        holder.tvItemPriceOrders.setText("Price: " + price);
        holder.tvItemCountOrders.setText(itemCount);
        holder.tvTotalOrders.setText(finalPrice);
    }

    @Override
    public int getItemCount() {
        return arrayListMember.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItemNameOrders, tvItemPriceOrders, tvItemCountOrders, tvTotalOrders, tvOrderDateOrders;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemNameOrders = (TextView) itemView.findViewById(R.id.tvItemNameOrders);
            tvItemPriceOrders = (TextView) itemView.findViewById(R.id.tvItemPriceOrders);
            tvItemCountOrders = (TextView) itemView.findViewById(R.id.tvItemCountOrders);
            tvTotalOrders = (TextView) itemView.findViewById(R.id.tvTotalOrders);
            tvOrderDateOrders = (TextView) itemView.findViewById(R.id.tvOrderDateOrders);

        }
    }
}
