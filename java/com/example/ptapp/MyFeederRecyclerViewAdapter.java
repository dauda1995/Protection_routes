package com.example.ptapp;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.ptapp.Feeder.Feeder;
import com.example.ptapp.asset.Asset;
import com.example.ptapp.utils.Contracts;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class MyFeederRecyclerViewAdapter extends RecyclerView.Adapter<MyFeederRecyclerViewAdapter.ViewHolder> {

    private final List<Asset> mValues;

    public MyFeederRecyclerViewAdapter(List<Asset> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mAsset.setText(mValues.get(position).asset);
        holder.mLocate.setText(mValues.get(position).location);
        holder.mStatus.setText(mValues.get(position).status);
        holder.mNo.setText("...");

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.goToDetails(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mAsset;
        public final TextView mLocate;
        public final TextView mStatus;
        public final TextView mNo;
        public Asset mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAsset = (TextView) view.findViewById(R.id.item_name);
            mLocate = (TextView) view.findViewById(R.id.item_locate);
            mStatus = view.findViewById(R.id.item_status);
            mNo = view.findViewById(R.id.item_no);
        }

       public void goToDetails(Asset asset){
            asset.latLng = new LatLng(asset.latitude, asset.longitude);

            asset.imageUrl = Contracts.FEEDER_ASSET_LIST;
           Intent intent = new Intent(itemView.getContext(), DetailsActivity.class);
           intent.putExtra(Contracts.LOCATION_BUNDLE, asset);
           itemView.getContext().startActivity(intent);

       }


    }
}