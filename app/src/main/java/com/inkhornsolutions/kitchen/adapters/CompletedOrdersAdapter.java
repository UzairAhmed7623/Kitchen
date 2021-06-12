package com.inkhornsolutions.kitchen.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.kitchen.R;
import com.inkhornsolutions.kitchen.modelclasses.OrdersModelClass;

import java.util.ArrayList;
import java.util.List;

public class CompletedOrdersAdapter extends RecyclerView.Adapter<CompletedOrdersAdapter.ViewHolder> {
    private Context context;
    private List<OrdersModelClass> Orders = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;

    public CompletedOrdersAdapter(Context context, List<OrdersModelClass> orders) {
        this.context = context;
        Orders = orders;
    }

    @NonNull
    @Override
    public CompletedOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.completed_orders_adapter, parent, false);
        return new CompletedOrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedOrdersAdapter.ViewHolder holder, int position) {
        OrdersModelClass ordersModelClass = Orders.get(position);

        String resId = ordersModelClass.getResId();
        String resName = ordersModelClass.getResName();
        String totalPrice = ordersModelClass.getTotalPrice();
        String status = ordersModelClass.getStatus();
        String date = ordersModelClass.getDate();
        Double lat = ordersModelClass.getLat();
        Double lng = ordersModelClass.getLng();
        String orderId = ordersModelClass.getOrderId();

        holder.tvResNameComOrders.setText(resName);
        holder.tvGradTotalComOrders.setText("Price: " + totalPrice);
        holder.tvDateComOrders.setText("Date: " + date);
        Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.completed_stump)).into(holder.ivStatusComOrders);

        boolean isExpanded = Orders.get(position).isExpanded();

        holder.expandablelLayoutComOrders.setVisibility(isExpanded ? View.VISIBLE: View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordersModelClass.setExpanded(!ordersModelClass.isExpanded());
                notifyItemChanged(position);
            }
        });

        ArrayList<OrdersModelClass> arrayListMember = new ArrayList<>();

        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot value) {

                for (DocumentSnapshot documentSnapshot : value){
                    if (documentSnapshot.exists()) {
                        String id = documentSnapshot.getId();

                        firebaseFirestore.collection("Users").document(id).collection("Cart").document(resId)
                                .collection("Items").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot documentSnapshot1 : task.getResult()){

                                        String id = documentSnapshot1.getId();
                                        String itemName = documentSnapshot1.getString("title");
                                        String price = documentSnapshot1.getString("price");
                                        String itemCount = documentSnapshot1.getString("items_count");
                                        String finalPrice = documentSnapshot1.getString("final_price");
                                        String pId = documentSnapshot1.getString("pId");

                                        OrdersModelClass ordersModelClass1 = new OrdersModelClass();

                                        ordersModelClass1.setId(id);
                                        ordersModelClass1.setItemName(itemName);
                                        ordersModelClass1.setPrice(price);
                                        ordersModelClass1.setItems_Count(itemCount);
                                        ordersModelClass1.setFinalPrice(finalPrice);
                                        ordersModelClass1.setpId(pId);

                                        Log.d("asdfgh2", ""+id+itemName+price+itemCount+finalPrice+pId);

                                        arrayListMember.add(ordersModelClass1);
                                    }
                                    holder.rvComMember.setAdapter(new MemeberComOrders(arrayListMember));
                                }
                            }
                        });

                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return Orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvResNameComOrders, tvDateComOrders, tvGradTotalComOrders;
        private LinearLayout expandablelLayoutComOrders;
        private RecyclerView rvComMember;
        private ImageView ivStatusComOrders;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseFirestore = FirebaseFirestore.getInstance();
            ivStatusComOrders = (ImageView) itemView.findViewById(R.id.ivStatusComOrders);
            tvResNameComOrders = (TextView) itemView.findViewById(R.id.tvResNameComOrders);
            tvDateComOrders = (TextView) itemView.findViewById(R.id.tvDateComOrders);
            tvGradTotalComOrders = (TextView) itemView.findViewById(R.id.tvGradTotalComOrders);
            expandablelLayoutComOrders = (LinearLayout) itemView.findViewById(R.id.expandablelLayoutComOrders);

            rvComMember = (RecyclerView) itemView.findViewById(R.id.rvComMember);
            rvComMember.setLayoutManager(new LinearLayoutManager(context));

        }
    }
}
