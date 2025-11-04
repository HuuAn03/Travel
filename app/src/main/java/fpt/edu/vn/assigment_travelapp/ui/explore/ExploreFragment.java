package fpt.edu.vn.assigment_travelapp.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import fpt.edu.vn.assigment_travelapp.R;

public class ExploreFragment extends Fragment {

    private RecyclerView recyclerChat;
    private EditText inputPrompt;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private static final String BACKEND_URL = "https://virtigo-api.onrender.com/api/Gemini/chat";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_explore, container, false);

        recyclerChat = v.findViewById(R.id.recyclerChat);
        inputPrompt = v.findViewById(R.id.inputPrompt);
        btnSend = v.findViewById(R.id.btnSend);

        messages = new ArrayList<>();
        adapter = new ChatAdapter(getContext(), messages);
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(adapter);

        btnSend.setOnClickListener(view -> {
            String prompt = inputPrompt.getText().toString().trim();
            if (prompt.isEmpty()) return;
            addMessage(prompt, true);
            inputPrompt.setText("");
            callGeminiAPI(prompt);
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
                actionBar.setTitle("Explore");
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }
  
    private void addMessage(String message, boolean isUser) {
        messages.add(new ChatMessage(message, isUser));
        requireActivity().runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerChat.scrollToPosition(messages.size() - 1);
        });
    }

    // 👇 Hàm gọi API có hiệu ứng "..." và xử lý Markdown
    private void callGeminiAPI(String prompt) {
        ChatMessage loadingMessage = new ChatMessage("🤖 ...", false);
        addMessage(loadingMessage.getMessage(), loadingMessage.isUser());
        int loadingIndex = messages.size() - 1;

        new Thread(() -> {
            try {
                URL url = new URL(BACKEND_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("prompt", prompt);

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
                String reply = jsonResponse.optString("reply", "Không nhận được phản hồi từ AI.");

                // ✨ Chuyển Markdown -> HTML
                String htmlReply = markdownToHtml("🤖 " + reply);

                requireActivity().runOnUiThread(() -> {
                    messages.set(loadingIndex, new ChatMessage(htmlReply, false));
                    adapter.notifyItemChanged(loadingIndex);
                    recyclerChat.scrollToPosition(messages.size() - 1);
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    messages.set(loadingIndex, new ChatMessage("⚠️ Lỗi: " + e.getMessage(), false));
                    adapter.notifyItemChanged(loadingIndex);
                });
            }
        }).start();
    }

    // 👇 Chuyển Markdown cơ bản sang HTML
    private String markdownToHtml(String text) {
        // Thay **bold**
        String html = text.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        // Thay *italic*
        html = html.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
        // Xuống dòng
        html = html.replace("\n", "<br>");
        return html;
    }
  
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
