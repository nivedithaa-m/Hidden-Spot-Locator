package com.example.androidpractice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationSuggestionAdapter extends RecyclerView.Adapter<LocationSuggestionAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(LocationGeocoder.GeocodeResult result);
    }

    private final Context context;
    private List<LocationGeocoder.GeocodeResult> suggestions;
    private final OnSuggestionClickListener listener;

    public LocationSuggestionAdapter(Context context, List<LocationGeocoder.GeocodeResult> suggestions, OnSuggestionClickListener listener) {
        this.context = context;
        this.suggestions = suggestions;
        this.listener = listener;
    }

    public void updateSuggestions(List<LocationGeocoder.GeocodeResult> newSuggestions) {
        this.suggestions = newSuggestions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        LocationGeocoder.GeocodeResult result = suggestions.get(position);
        holder.tvSuggestionTitle.setText(result.displayName);
        holder.tvSuggestionDetail.setText(result.addressDetail != null ? result.addressDetail : result.displayName);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions != null ? suggestions.size() : 0;
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestionTitle;
        TextView tvSuggestionDetail;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSuggestionTitle = itemView.findViewById(R.id.tvSuggestionTitle);
            tvSuggestionDetail = itemView.findViewById(R.id.tvSuggestionDetail);
        }
    }
}