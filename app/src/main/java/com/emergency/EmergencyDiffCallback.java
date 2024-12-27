package com.emergency;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class EmergencyDiffCallback extends DiffUtil.Callback {
    private final List<EmergencyDTO> oldList;
    private final List<EmergencyDTO> newList;

    public EmergencyDiffCallback(List<EmergencyDTO> oldList, List<EmergencyDTO> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Compare unique identifiers
        return oldList.get(oldItemPosition).getId().equals(
                newList.get(newItemPosition).getId()
        );
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        EmergencyDTO oldEmergency = oldList.get(oldItemPosition);
        EmergencyDTO newEmergency = newList.get(newItemPosition);

        // Compare all relevant fields
        return oldEmergency.getLocation().equals(newEmergency.getLocation()) &&
                oldEmergency.getDescription().equals(newEmergency.getDescription()) &&
                oldEmergency.getPatientCondition().equals(newEmergency.getPatientCondition()) &&
                oldEmergency.getDistance() == newEmergency.getDistance() &&
                oldEmergency.getTimestamp().equals(newEmergency.getTimestamp());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Optional: Provide specific change information
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
