package fpt.edu.vn.assigment_travelapp.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Booking;

public class BookingRepository implements IBookingRepository {

    private final DatabaseReference mDatabase;

    public BookingRepository() {
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    @Override
    public void createBooking(Booking booking, OnBookingCompleteListener listener) {
        String bookingId = mDatabase.child("bookings").push().getKey();
        if (bookingId != null) {
            booking.setBookingId(bookingId);
            mDatabase.child("bookings").child(bookingId).setValue(booking)
                    .addOnSuccessListener(aVoid -> listener.onSuccess(booking))
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        } else {
            listener.onFailure("Failed to generate booking ID");
        }
    }

    @Override
    public void getBookingById(String bookingId, OnBookingFetchedListener listener) {
        mDatabase.child("bookings").child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null) {
                        listener.onSuccess(booking);
                    } else {
                        listener.onFailure("Booking data is null");
                    }
                } else {
                    listener.onFailure("Booking not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void getUserBookings(String userId, OnBookingsFetchedListener listener) {
        Query query = mDatabase.child("bookings").orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Booking> bookings = new ArrayList<>();
                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (booking != null) {
                        bookings.add(booking);
                    }
                }
                listener.onSuccess(bookings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    @Override
    public void updateBookingStatus(String bookingId, String status, OnBookingCompleteListener listener) {
        mDatabase.child("bookings").child(bookingId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    getBookingById(bookingId, new OnBookingFetchedListener() {
                        @Override
                        public void onSuccess(Booking booking) {
                            listener.onSuccess(booking);
                        }

                        @Override
                        public void onFailure(String error) {
                            listener.onFailure(error);
                        }
                    });
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void updatePaymentStatus(String bookingId, String paymentStatus, OnBookingCompleteListener listener) {
        mDatabase.child("bookings").child(bookingId).child("paymentStatus").setValue(paymentStatus)
                .addOnSuccessListener(aVoid -> {
                    // Also update booking status to confirmed if payment is paid
                    if ("paid".equals(paymentStatus)) {
                        mDatabase.child("bookings").child(bookingId).child("status").setValue("confirmed")
                                .addOnSuccessListener(aVoid2 -> {
                                    getBookingById(bookingId, new OnBookingFetchedListener() {
                                        @Override
                                        public void onSuccess(Booking booking) {
                                            listener.onSuccess(booking);
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            listener.onFailure(error);
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                    } else {
                        getBookingById(bookingId, new OnBookingFetchedListener() {
                            @Override
                            public void onSuccess(Booking booking) {
                                listener.onSuccess(booking);
                            }

                            @Override
                            public void onFailure(String error) {
                                listener.onFailure(error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    @Override
    public void checkIn(String bookingId, OnBookingCompleteListener listener) {
        updateBookingStatus(bookingId, "checked_in", listener);
    }

    @Override
    public void checkOut(String bookingId, OnBookingCompleteListener listener) {
        updateBookingStatus(bookingId, "checked_out", listener);
    }
}

