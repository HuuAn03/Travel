package fpt.edu.vn.assigment_travelapp.ui.explore;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import fpt.edu.vn.assigment_travelapp.R;

public class ExploreFragment extends Fragment {

    private RecyclerView recyclerChat;
    private EditText inputPrompt;
    private ImageButton btnSend, btnMic;
    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private LinearLayout suggestionLayout;
    private ChatHistoryDbHelper dbHelper;
    private static final String BACKEND_URL = "https://virtigo-api.onrender.com/api/Gemini/chat";
    private static final int RECORD_AUDIO_PERMISSION_CODE = 101;

    private final ActivityResultLauncher<Intent> speechRecognizerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> speechResult = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (speechResult != null && !speechResult.isEmpty()) {
                        inputPrompt.setText(speechResult.get(0));
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_explore, container, false);

        recyclerChat = v.findViewById(R.id.recyclerChat);
        inputPrompt = v.findViewById(R.id.inputPrompt);
        btnSend = v.findViewById(R.id.btnSend);
        btnMic = v.findViewById(R.id.btnMic);
        suggestionLayout = v.findViewById(R.id.suggestionLayout);

        dbHelper = new ChatHistoryDbHelper(getContext());
        messages = dbHelper.getAllMessages();
        adapter = new ChatAdapter(getContext(), messages);
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(adapter);

        btnSend.setOnClickListener(view -> {
            String prompt = inputPrompt.getText().toString().trim();
            if (prompt.isEmpty()) return;
            addMessage(new ChatMessage(prompt, true), true);
            inputPrompt.setText("");
            callGeminiAPI(prompt);
        });

        btnMic.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startSpeechToText();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateSuggestionVisibility();
    }

    private void updateSuggestionVisibility() {
        if (messages.isEmpty()) {
            suggestionLayout.setVisibility(View.VISIBLE);
            recyclerChat.setVisibility(View.GONE);
        } else {
            suggestionLayout.setVisibility(View.GONE);
            recyclerChat.setVisibility(View.VISIBLE);
        }
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói gì đó...");
        try {
            speechRecognizerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechToText();
            } else {
                Toast.makeText(getContext(), "Cần cấp quyền ghi âm để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem clearChatItem = menu.findItem(R.id.action_clear_chat);
        if (clearChatItem != null) {
            clearChatItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_chat) {
            dbHelper.clearHistory();
            messages.clear();
            adapter.notifyDataSetChanged();
            updateSuggestionVisibility();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addMessage(ChatMessage message, boolean saveToDb) {
        if (saveToDb) {
            dbHelper.addMessage(message);
        }
        messages.add(message);
        requireActivity().runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerChat.scrollToPosition(messages.size() - 1);
            updateSuggestionVisibility();
        });
    }

    private void callGeminiAPI(String prompt) {
        ChatMessage loadingMessage = new ChatMessage("🤖 ...", false);
        addMessage(loadingMessage, false);
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

                String htmlReply = markdownToHtml("🤖 " + reply);
                ChatMessage replyMessage = new ChatMessage(htmlReply, false);

                requireActivity().runOnUiThread(() -> {
                    messages.set(loadingIndex, replyMessage);
                    dbHelper.addMessage(replyMessage); 
                    adapter.notifyItemChanged(loadingIndex);
                    recyclerChat.scrollToPosition(messages.size() - 1);
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    ChatMessage errorMessage = new ChatMessage("⚠️ Lỗi: " + e.getMessage(), false);
                    messages.set(loadingIndex, errorMessage);
                    dbHelper.addMessage(errorMessage); 
                    adapter.notifyItemChanged(loadingIndex);
                });
            }
        }).start();
    }

    private String markdownToHtml(String text) {
        String html = text.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
        html = html.replace("\n", "<br>");
        return html;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
