package fpt.edu.vn.assigment_travelapp.ui.booking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Booking;
import fpt.edu.vn.assigment_travelapp.data.repository.BookingRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.IBookingRepository;

public class MyBookingsViewModel extends ViewModel {

    private final BookingRepository bookingRepository;
    private final MutableLiveData<List<Booking>> bookings = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public MyBookingsViewModel() {
        bookingRepository = new BookingRepository();
    }

    public LiveData<List<Booking>> getBookings() {
        return bookings;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadUserBookings(String userId) {
        isLoading.setValue(true);
        bookingRepository.getUserBookings(userId, new IBookingRepository.OnBookingsFetchedListener() {
            @Override
            public void onSuccess(List<Booking> bookingList) {
                isLoading.postValue(false);
                bookings.postValue(bookingList);
            }

            @Override
            public void onFailure(String errorMessage) {
                isLoading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }
}

