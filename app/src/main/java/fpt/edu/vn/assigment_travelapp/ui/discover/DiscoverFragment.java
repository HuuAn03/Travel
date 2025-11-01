package fpt.edu.vn.assigment_travelapp.ui.discover;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import fpt.edu.vn.assigment_travelapp.R;

public class DiscoverFragment extends Fragment {

    private RecyclerView recyclerRecentlyViewed, recyclerAISuggest;
    private DestinationAdapter adapterRecently, adapterAISuggest;
    private EditText searchEditText;
    private ImageButton btnSearch;
    private TextView textGeminiReply;
    private CardView cardGeminiResult;

    private static final String BACKEND_URL = "https://virtigo-api.onrender.com/api/Gemini/suggest-destination";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_discover, container, false);

        recyclerRecentlyViewed = v.findViewById(R.id.recyclerRecentlyViewed);
        recyclerAISuggest = v.findViewById(R.id.recyclerAISuggest);
        searchEditText = v.findViewById(R.id.searchEditText);
        btnSearch = v.findViewById(R.id.btnSearch);
        textGeminiReply = v.findViewById(R.id.textGeminiReply);
        cardGeminiResult = v.findViewById(R.id.cardGeminiResult);

        recyclerRecentlyViewed.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAISuggest.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dữ liệu mặc định cho Recently Viewed
        List<Destination> data = new ArrayList<>();
        data.add(new Destination("Moraine Lake", "Canada",
                "https://i.imgur.com/q0WcJr1.jpeg", 4.4, "$83/Person"));
        data.add(new Destination("Queen Anne Beach", "Hawaii",
                "https://i.imgur.com/FZpW6MU.jpeg", 4.5, "$78/Person"));
        adapterRecently = new DestinationAdapter(getContext(), data);
        recyclerRecentlyViewed.setAdapter(adapterRecently);

        // Adapter trống cho AI Suggest
        adapterAISuggest = new DestinationAdapter(getContext(), new ArrayList<>());
        recyclerAISuggest.setAdapter(adapterAISuggest);

        // Khi bấm nút Search
        btnSearch.setOnClickListener(v1 -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                processGeminiRequest(query);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập nội dung tìm kiếm", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    private void processGeminiRequest(String query) {
        new Thread(() -> {
            try {
                List<Destination> destinations = callBackendGemini(query);

                requireActivity().runOnUiThread(() -> {
                    if (destinations != null && !destinations.isEmpty()) {
                        adapterAISuggest.setDestinations(destinations);
                        adapterAISuggest.notifyDataSetChanged();
                        cardGeminiResult.setVisibility(View.VISIBLE);
                        textGeminiReply.setText("✅ AI đề xuất " + destinations.size() + " địa điểm cho bạn!");
                    } else {
                        cardGeminiResult.setVisibility(View.VISIBLE);
                        textGeminiReply.setText("🤖 Không tìm thấy địa điểm phù hợp.");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    cardGeminiResult.setVisibility(View.VISIBLE);
                    textGeminiReply.setText("⚠️ Lỗi khi gọi API: " + e.getMessage());
                });
            }
        }).start();
    }

    private List<Destination> callBackendGemini(String query) throws Exception {
        URL url = new URL(BACKEND_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        JSONObject body = new JSONObject();
        body.put("prompt", query);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        conn.disconnect();

        JSONObject jsonResponse = new JSONObject(sb.toString());
        JSONArray dataArray = jsonResponse.optJSONArray("data");
        List<Destination> destinations = new ArrayList<>();

        if (dataArray != null) {
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject obj = dataArray.getJSONObject(i);
                String name = obj.optString("name");
                String country = obj.optString("country");
                String imageUrl = obj.optString("imageUrl");
                double rating = obj.optDouble("rating", 0.0);
                String price = obj.optString("price");
                destinations.add(new Destination(name, country, imageUrl, rating, price));
            }
        }

        return destinations;
    }
}
