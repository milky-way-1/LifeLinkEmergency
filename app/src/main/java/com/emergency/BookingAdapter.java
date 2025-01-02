package com.emergency;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emergency.model.Booking;
import com.emergency.model.Location;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookings;
    private final OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView statusText;
        private final TextView pickupLocationText;
        private final TextView dropLocationText;
        private final TextView timeText;
        private final MaterialButton actionButton;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            statusText = itemView.findViewById(R.id.statusText);
            pickupLocationText = itemView.findViewById(R.id.pickupLocationText);
            dropLocationText = itemView.findViewById(R.id.dropLocationText);
            timeText = itemView.findViewById(R.id.timeText);
            actionButton = itemView.findViewById(R.id.actionButton);
        }

        void bind(Booking booking) {
            statusText.setText(booking.getStatus().toString());
            pickupLocationText.setText(formatLocation(booking.getPickupLocation()));
            dropLocationText.setText(formatLocation(booking.getDestinationLocation()));
            timeText.setText(formatTime(booking.getCreatedAt()));

            // Set button text based on status
            switch (booking.getStatus()) {
                case PENDING:
                    actionButton.setText("Accept");
                    break;
                case ASSIGNED:
                    actionButton.setText("Start Pickup");
                    break;
                default:
                    actionButton.setVisibility(View.GONE);
                    return;
            }

            itemView.setOnClickListener(v -> listener.onBookingClick(booking));
            actionButton.setOnClickListener(v -> listener.onBookingClick(booking));
        }

        private String formatLocation(Location location) {
            return String.format("%.6f, %.6f",
                    location.getLatitude(),
                    location.getLongitude());
        }

        private String formatTime(String dateTime) {
            // TODO: Implement proper date formatting
            return dateTime;
        }
    }
}
