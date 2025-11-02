package fpt.edu.vn.assigment_travelapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Destination;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Destination>> destinations;
    private List<Destination> allDestinations;

    public HomeViewModel() {
        destinations = new MutableLiveData<>();
        allDestinations = new ArrayList<>();
        destinations.setValue(new ArrayList<>());
    }

    public LiveData<List<Destination>> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinationList) {
        allDestinations = destinationList;
        destinations.setValue(destinationList);
    }

    public void filterByCategory(String category) {
        if (category == null || category.isEmpty() || "All".equals(category)) {
            destinations.setValue(allDestinations);
            return;
        }

        List<Destination> filtered = new ArrayList<>();
        for (Destination destination : allDestinations) {
            if (category.equals(destination.getCategory())) {
                filtered.add(destination);
            }
        }
        destinations.setValue(filtered);
    }

    public void toggleFavorite(Destination destination) {
        // TODO: Implement favorite toggle logic with repository
        // For now, just a placeholder
    }
}