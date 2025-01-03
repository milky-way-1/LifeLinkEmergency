package com.emergency;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emergency.model.Booking;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookings;
    private final OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = new ArrayList<>(bookings);
        this.listener = listener;
    }

    public void updateBookings(List<Booking> newBookings) {
        if (newBookings == null) return;

        this.bookings = new ArrayList<>(newBookings);
        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
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
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView pickupAddressText;
        private final TextView destinationAddressText;

        private final TextView statusText;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.bookingCard);
            pickupAddressText = itemView.findViewById(R.id.pickupAddressText);
            destinationAddressText = itemView.findViewById(R.id.destinationAddressText);
            statusText = itemView.findViewById(R.id.statusText);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookingClick(bookings.get(position));
                }
            });
        }

        public void bind(Booking booking) {
            pickupAddressText.setText(booking.getPickupLocation().toString());
            destinationAddressText.setText(booking.getDestinationLocation().toString());

            statusText.setText(booking.getStatus().toString());



        }
    }
}

