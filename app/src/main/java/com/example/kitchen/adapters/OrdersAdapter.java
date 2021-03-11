package com.example.kitchen.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitchen.FindDriver;
import com.example.kitchen.R;
import com.example.kitchen.modelclasses.OrdersModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private Context context;
    private List<OrdersModelClass> Orders = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;

    public OrdersAdapter(Context context, List<OrdersModelClass> Orders) {
        this.context = context;
        this.Orders = Orders;
    }

    @NonNull
    @Override
    public OrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.orders_adapter_layout, parent, false);
        return new OrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrdersModelClass ordersModelClass = Orders.get(position);

        String resId = ordersModelClass.getResId();
        String resName = ordersModelClass.getResName();
        String totalPrice = ordersModelClass.getTotalPrice();
        String status = ordersModelClass.getStatus();
        String date = ordersModelClass.getDate();

        holder.tvResNameOrders.setText(resName);
        holder.tvGradTotalOrders.setText("Price: " + totalPrice);
        holder.tvDateOrders.setText("Date: " + date);
        holder.tvStatusOrders.setText(status);

        boolean isExpanded = Orders.get(position).isExpanded();

        holder.expandablelLayoutOrders.setVisibility(isExpanded ? View.VISIBLE: View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordersModelClass.setExpanded(!ordersModelClass.isExpanded());
                notifyItemChanged(position);
            }
        });

        ArrayList<OrdersModelClass> arrayListMember = new ArrayList<>();

        firebaseFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.exists()) {
                            String id = documentSnapshot.getId();

                            firebaseFirestore.collection("Users").document(id).collection("Cart").document(resId)
                                    .collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                                    holder.rvMember.setAdapter(new MemberOrdersAdapter(arrayListMember));

                                                }
                                            }
                                        }
                                    });

                        }
                    }
                }
            }
        });

        if (status.equals("In progress"))
        {
            holder.hideLayout.setVisibility(View.GONE);
            holder.hideLayout2.setVisibility(View.VISIBLE);
        }

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                if (documentSnapshot.exists()){
                                    String id = documentSnapshot.getId();

                                    firebaseFirestore.collection("Users").document(id)
                                            .collection("Cart").document(resId)
                                            .update("status","In progress").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                Orders.get(position).setStatus("In progress");
                notifyDataSetChanged();

                Toast.makeText(context, "Status updated!", Toast.LENGTH_SHORT).show();
                holder.hideLayout.setVisibility(View.GONE);
                holder.hideLayout2.setVisibility(View.VISIBLE);
            }
        });

        holder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                if (documentSnapshot.exists()){
                                    String id = documentSnapshot.getId();

                                    firebaseFirestore.collection("Users").document(id)
                                            .collection("Cart").document(resId)
                                            .update("status","Rejected").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                Orders.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Status updated!", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FindDriver.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.btnDispatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "dispatch", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return Orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvStatusOrders, tvResNameOrders, tvDateOrders, tvGradTotalOrders;
        private LinearLayout expandablelLayoutOrders;
        private RelativeLayout hideLayout, hideLayout2;
        private RecyclerView rvMember;
        private Button btnAccept, btnReject, btnRider, btnDispatch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseFirestore = FirebaseFirestore.getInstance();
            tvStatusOrders = (TextView) itemView.findViewById(R.id.tvStatusOrders);
            tvResNameOrders = (TextView) itemView.findViewById(R.id.tvResNameOrders);
            tvDateOrders = (TextView) itemView.findViewById(R.id.tvDateOrders);
            tvGradTotalOrders = (TextView) itemView.findViewById(R.id.tvGradTotalOrders);
            expandablelLayoutOrders = (LinearLayout) itemView.findViewById(R.id.expandablelLayoutOrders);

            rvMember = (RecyclerView) itemView.findViewById(R.id.rvMember);
            rvMember.setLayoutManager(new LinearLayoutManager(context));

            btnAccept = (Button) itemView.findViewById(R.id.btnAccept);
            btnReject = (Button) itemView.findViewById(R.id.btnReject);
            hideLayout = (RelativeLayout) itemView.findViewById(R.id.hideLayout);

            btnRider = (Button) itemView.findViewById(R.id.btnRider);
            btnDispatch = (Button) itemView.findViewById(R.id.btnDispatch);
            hideLayout2 = (RelativeLayout) itemView.findViewById(R.id.hideLayout2);
        }
    }
}
