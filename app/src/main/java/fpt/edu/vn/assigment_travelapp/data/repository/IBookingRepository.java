package fpt.edu.vn.assigment_travelapp.data.repository;

import java.util.List;
import fpt.edu.vn.assigment_travelapp.data.model.Booking;

public interface IBookingRepository {
    interface OnBookingCompleteListener {
        void onSuccess(Booking booking);
        void onFailure(String error);
    }

    interface OnBookingsFetchedListener {
        void onSuccess(List<Booking> bookings);
        void onFailure(String error);
    }

    interface OnBookingFetchedListener {
        void onSuccess(Booking booking);
        void onFailure(String error);
    }

    void createBooking(Booking booking, OnBookingCompleteListener listener);
    void getBookingById(String bookingId, OnBookingFetchedListener listener);
    void getUserBookings(String userId, OnBookingsFetchedListener listener);
    void updateBookingStatus(String bookingId, String status, OnBookingCompleteListener listener);
    void updatePaymentStatus(String bookingId, String paymentStatus, OnBookingCompleteListener listener);
    void checkIn(String bookingId, OnBookingCompleteListener listener);
    void checkOut(String bookingId, OnBookingCompleteListener listener);
}

