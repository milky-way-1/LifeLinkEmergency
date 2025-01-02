package com.emergency.util;

import com.emergency.model.Booking;

import java.util.List;

public class NewBookingEvent {
    private final List<Booking> bookings;

    public NewBookingEvent(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<Booking> getBookings() {
        return bookings;
    }
}
