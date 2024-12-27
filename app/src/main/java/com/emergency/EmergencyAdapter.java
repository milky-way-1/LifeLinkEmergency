package com.emergency;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder> {
    private List<EmergencyDTO> emergencies;
    private final OnEmergencyClickListener listener;

    public interface OnEmergencyClickListener {
        void onEmergencyClicked(EmergencyDTO emergency);
    }

    public EmergencyAdapter(List<EmergencyDTO> emergencies, OnEmergencyClickListener listener) {
        this.emergencies = emergencies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency, parent, false);
        return new EmergencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position) {
        EmergencyDTO emergency = emergencies.get(position);
        holder.bind(emergency);
    }

    @Override
    public int getItemCount() {
        return emergencies.size();
    }

    public void updateEmergencies(List<EmergencyDTO> newEmergencies) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new EmergencyDiffCallback(emergencies, newEmergencies)
        );
        emergencies = newEmergencies;
        diffResult.dispatchUpdatesTo(this);
    }

    class EmergencyViewHolder extends RecyclerView.ViewHolder {
        private final TextView emergencyTypeText;
        private final TextView locationText;
        private final TextView timeText;
        private final TextView distanceText;

        EmergencyViewHolder(View itemView) {
            super(itemView);
            emergencyTypeText = itemView.findViewById(R.id.emergencyTypeText);
            locationText = itemView.findViewById(R.id.locationText);
            timeText = itemView.findViewById(R.id.timeText);
            distanceText = itemView.findViewById(R.id.distanceText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEmergencyClicked(emergencies.get(position));
                }
            });
        }

        void bind(EmergencyDTO emergency) {
            emergencyTypeText.setText(emergency.getEmergencyType());
            locationText.setText(emergency.getLocation());
            timeText.setText(emergency.getTimestamp());
            distanceText.setText(String.format("%.1f km", emergency.getDistance()));
        }
    }
}
