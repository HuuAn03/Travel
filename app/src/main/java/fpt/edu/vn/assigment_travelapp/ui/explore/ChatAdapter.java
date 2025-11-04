package fpt.edu.vn.assigment_travelapp.ui.explore;

import android.content.Context;
import android.text.Html;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fpt.edu.vn.assigment_travelapp.R;
import java.util.*;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messages;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(viewType, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        // Dùng Html.fromHtml để render định dạng
        holder.textMessage.setText(
                Html.fromHtml(msg.getMessage(), Html.FROM_HTML_MODE_LEGACY)
        );
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser()
                ? R.layout.item_chat_user
                : R.layout.item_chat_ai;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
        }
    }
}
