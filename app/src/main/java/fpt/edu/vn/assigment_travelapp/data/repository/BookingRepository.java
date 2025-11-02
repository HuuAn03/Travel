package fpt.edu.vn.assigment_travelapp.data.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fpt.edu.vn.assigment_travelapp.data.model.Booking;

public class BookingRepository {

    private final DatabaseReference mDatabase;

    public BookingRepository() {
        mDatabase = FirebaseDatabase.getInstance("https://swp391-fkoi-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    /**
     * Save booking to Firebase
     */
    public Task<Void> saveBooking(Booking booking) {
        // Convert Booking to Map for Firebase
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("bookingId", booking.getBookingId());
        bookingMap.put("destinationId", booking.getDestinationId());
        bookingMap.put("userId", booking.getUserId());
        bookingMap.put("checkInDate", booking.getCheckInDate() != null ? booking.getCheckInDate().getTime() : null);
        bookingMap.put("checkOutDate", booking.getCheckOutDate() != null ? booking.getCheckOutDate().getTime() : null);
        bookingMap.put("numberOfPeople", booking.getNumberOfPeople());
        bookingMap.put("message", booking.getMessage());
        bookingMap.put("totalPrice", booking.getTotalPrice());
        bookingMap.put("paymentMethodId", booking.getPaymentMethodId());
        bookingMap.put("status", booking.getStatus());
        bookingMap.put("timestamp", booking.getTimestamp());

        // Save to bookings/{bookingId} and also to user-bookings/{userId}/{bookingId}
        String bookingId = booking.getBookingId();
        Task<Void> saveTask = mDatabase.child("bookings").child(bookingId).setValue(bookingMap);
        
        // Also save reference under user-bookings for quick lookup
        mDatabase.child("user-bookings").child(booking.getUserId()).child(bookingId).setValue(true);
        
        return saveTask;
    }

    /**
     * Get all bookings for a specific user
     */
    public void getUserBookings(String userId, OnGetBookingsCompleteListener listener) {
        mDatabase.child("user-bookings").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    listener.onSuccess(new ArrayList<>());
                    return;
                }

                List<String> bookingIds = new ArrayList<>();
                for (DataSnapshot bookingIdSnapshot : snapshot.getChildren()) {
                    String bookingId = bookingIdSnapshot.getKey();
                    if (bookingId != null) {
                        bookingIds.add(bookingId);
                    }
                }

                // Now fetch all booking details
                if (bookingIds.isEmpty()) {
                    listener.onSuccess(new ArrayList<>());
                    return;
                }

                fetchBookingDetails(bookingIds, listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    private void fetchBookingDetails(List<String> bookingIds, OnGetBookingsCompleteListener listener) {
        List<Booking> bookings = new ArrayList<>();
        int totalCount = bookingIds.size();
        int[] completedCount = {0};

        for (String bookingId : bookingIds) {
            mDatabase.child("bookings").child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Manually deserialize to handle Date conversion
                    if (!snapshot.exists()) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCount) {
                            bookings.sort((b1, b2) -> Long.compare(b2.getTimestamp(), b1.getTimestamp()));
                            listener.onSuccess(bookings);
                        }
                        return;
                    }

                    try {
                        Booking booking = new Booking();
                        booking.setBookingId(snapshot.child("bookingId").getValue(String.class));
                        booking.setDestinationId(snapshot.child("destinationId").getValue(String.class));
                        booking.setUserId(snapshot.child("userId").getValue(String.class));
                        booking.setNumberOfPeople(snapshot.child("numberOfPeople").getValue(Integer.class) != null 
                                ? snapshot.child("numberOfPeople").getValue(Integer.class) : 0);
                        booking.setMessage(snapshot.child("message").getValue(String.class));
                        booking.setTotalPrice(snapshot.child("totalPrice").getValue(Double.class) != null
                                ? snapshot.child("totalPrice").getValue(Double.class) : 0.0);
                        booking.setPaymentMethodId(snapshot.child("paymentMethodId").getValue(String.class));
                        booking.setStatus(snapshot.child("status").getValue(String.class));
                        booking.setTimestamp(snapshot.child("timestamp").getValue(Long.class) != null
                                ? snapshot.child("timestamp").getValue(Long.class) : 0L);

                        // Convert Long timestamps to Date objects
                        Long checkInTimestamp = snapshot.child("checkInDate").getValue(Long.class);
                        if (checkInTimestamp != null) {
                            booking.setCheckInDate(new java.util.Date(checkInTimestamp));
                        }
                        Long checkOutTimestamp = snapshot.child("checkOutDate").getValue(Long.class);
                        if (checkOutTimestamp != null) {
                            booking.setCheckOutDate(new java.util.Date(checkOutTimestamp));
                        }

                        bookings.add(booking);
                    } catch (Exception e) {
                        // Log error but continue processing other bookings
                        android.util.Log.e("BookingRepository", "Error deserializing booking: " + e.getMessage(), e);
                    } finally {
                        // Always increment counter, even if deserialization failed
                        completedCount[0]++;
                        if (completedCount[0] == totalCount) {
                            // Sort by timestamp descending (newest first)
                            bookings.sort((b1, b2) -> Long.compare(b2.getTimestamp(), b1.getTimestamp()));
                            listener.onSuccess(bookings);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    completedCount[0]++;
                    if (completedCount[0] == totalCount) {
                        listener.onSuccess(bookings); // Return what we have
                    }
                }
            });
        }
    }

    /**
     * Update booking status (for check-in/check-out)
     */
    public Task<Void> updateBookingStatus(String bookingId, String status) {
        return mDatabase.child("bookings").child(bookingId).child("status").setValue(status);
    }

    /**
     * Get a single booking by ID
     */
    public void getBookingById(String bookingId, OnGetBookingCompleteListener listener) {
        mDatabase.child("bookings").child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    listener.onFailure("Booking not found");
                    return;
                }

                try {
                    Booking booking = new Booking();
                    booking.setBookingId(snapshot.child("bookingId").getValue(String.class));
                    booking.setDestinationId(snapshot.child("destinationId").getValue(String.class));
                    booking.setUserId(snapshot.child("userId").getValue(String.class));
                    booking.setNumberOfPeople(snapshot.child("numberOfPeople").getValue(Integer.class) != null 
                            ? snapshot.child("numberOfPeople").getValue(Integer.class) : 0);
                    booking.setMessage(snapshot.child("message").getValue(String.class));
                    booking.setTotalPrice(snapshot.child("totalPrice").getValue(Double.class) != null
                            ? snapshot.child("totalPrice").getValue(Double.class) : 0.0);
                    booking.setPaymentMethodId(snapshot.child("paymentMethodId").getValue(String.class));
                    booking.setStatus(snapshot.child("status").getValue(String.class));
                    booking.setTimestamp(snapshot.child("timestamp").getValue(Long.class) != null
                            ? snapshot.child("timestamp").getValue(Long.class) : 0L);

                    // Convert Long timestamps to Date objects
                    Long checkInTimestamp = snapshot.child("checkInDate").getValue(Long.class);
                    if (checkInTimestamp != null) {
                        booking.setCheckInDate(new java.util.Date(checkInTimestamp));
                    }
                    Long checkOutTimestamp = snapshot.child("checkOutDate").getValue(Long.class);
                    if (checkOutTimestamp != null) {
                        booking.setCheckOutDate(new java.util.Date(checkOutTimestamp));
                    }

                    listener.onSuccess(booking);
                } catch (Exception e) {
                    android.util.Log.e("BookingRepository", "Error deserializing booking: ", e);
                    listener.onFailure("Error loading booking: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public interface OnGetBookingsCompleteListener {
        void onSuccess(List<Booking> bookings);
        void onFailure(String errorMessage);
    }

    public interface OnGetBookingCompleteListener {
        void onSuccess(Booking booking);
        void onFailure(String errorMessage);
    }
}

