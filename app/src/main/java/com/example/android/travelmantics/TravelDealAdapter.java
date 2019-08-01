package com.example.android.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TravelDealAdapter extends RecyclerView.Adapter<TravelDealAdapter.TravelDealViewholder> {

    private final Context context;
    private final List<TravelDeal> travelDeals;

    public TravelDealAdapter(Context context) {
        this.context = context;
        this.travelDeals = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("deals").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                travelDeals.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    TravelDeal travelDeal = child.getValue(TravelDeal.class);
                    travelDeals.add(travelDeal);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @NonNull
    @Override
    public TravelDealViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_deal, parent, false);
        return new TravelDealViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TravelDealViewholder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (travelDeals != null) {
            return travelDeals.size();
        }
        return 0;
    }

    class TravelDealViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewPrice;

        public TravelDealViewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_item_traveldeal);
            textViewTitle = itemView.findViewById(R.id.textView_item_title);
            textViewDescription = itemView.findViewById(R.id.textView_item_description);
            textViewPrice = itemView.findViewById(R.id.textView_item_cost);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Intent intent = new Intent(context, TravelDealActivity.class);
            intent.putExtra("travelDeal", travelDeals.get(pos));
            context.startActivity(intent);
        }

        void bind(int pos) {
            TravelDeal travelDeal = travelDeals.get(pos);
            Picasso.get().load(travelDeal.getImageUrl()).placeholder(R.drawable.ic_grade).into(imageView);
            textViewTitle.setText(travelDeal.getTitle());
            textViewDescription.setText(travelDeal.getDescription());
            textViewPrice.setText(travelDeal.getPrice());
        }

    }
}
