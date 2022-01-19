package com.example.ptapp.adaptors;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ptapp.MyFeederRecyclerViewAdapter;
import com.example.ptapp.R;
import com.example.ptapp.flir.FrameDataHolder;

import java.util.List;

public class imageAdaptor extends RecyclerView.Adapter<imageAdaptor.ViewHolder> {


    private final List<FrameDataHolder> mValues;

    public imageAdaptor(List<FrameDataHolder> items) {
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull imageAdaptor.ViewHolder holder, int position) {

        holder.mxc.setImageBitmap(mValues.get(position).msxBitmap);
        holder.desc.setText(mValues.get(position).msxBitmap.describeContents());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final ImageView mxc;
        public final TextView desc;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mxc = view.findViewById(R.id.mxcimage);
            desc = view.findViewById(R.id.desc_item);
        }
    }
}
