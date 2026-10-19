package com.example.androidpractice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class NearbyPlaceAdapter extends RecyclerView.Adapter<NearbyPlaceAdapter.PlaceViewHolder> {

    public interface OnPlaceClickListener {
        void onPlaceClick(NearbyPlace place);
    }

    private final Context context;
    private List<NearbyPlace> placeList;
    private final OnPlaceClickListener listener;

    public NearbyPlaceAdapter(Context context, List<NearbyPlace> placeList, OnPlaceClickListener listener) {
        this.context = context;
        this.placeList = placeList;
        this.listener = listener;
    }

    public void updateData(List<NearbyPlace> newPlaces) {
        this.placeList = newPlaces;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nearby_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        NearbyPlace place = placeList.get(position);
        holder.tvPlaceName.setText(place.getName());
        holder.tvPlaceCategory.setText(place.getCategory());
        holder.tvPlaceRating.setText(String.format(Locale.getDefault(), "%.1f", place.getRating()));
        holder.tvPlaceDistance.setText(String.format(Locale.getDefault(), "%.1f km away", place.getDistanceKm()));
        holder.ivPlaceIcon.setImageResource(place.getIconResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaceClick(place);
            }
        });

        holder.btnNavToPlace.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaceClick(place);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList != null ? placeList.size() : 0;
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceIcon;
        TextView tvPlaceName;
        TextView tvPlaceCategory;
        TextView tvPlaceRating;
        TextView tvPlaceDistance;
        TextView btnNavToPlace;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceIcon = itemView.findViewById(R.id.ivPlaceIcon);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceCategory = itemView.findViewById(R.id.tvPlaceCategory);
            tvPlaceRating = itemView.findViewById(R.id.tvPlaceRating);
            tvPlaceDistance = itemView.findViewById(R.id.tvPlaceDistance);
            btnNavToPlace = itemView.findViewById(R.id.btnNavToPlace);
        }
    }
}