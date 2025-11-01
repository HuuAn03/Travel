package fpt.edu.vn.assigment_travelapp.ui.discover;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import fpt.edu.vn.assigment_travelapp.R;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private List<Destination> items;
    private final Context context;

    public DestinationAdapter(Context context, List<Destination> items) {
        this.context = context;
        this.items = items;
    }

    // ✅ thêm hàm để cập nhật danh sách (dùng khi nhận dữ liệu từ API)
    public void setDestinations(List<Destination> newItems) {
        this.items = newItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination d = items.get(position);

        holder.name.setText(d.name);
        holder.country.setText(d.country);
        holder.price.setText(d.price);
        holder.rating.setText("⭐ " + d.rating + " / 5");

        Glide.with(context)
                .load(d.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, country, price, rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgDestination);
            name = itemView.findViewById(R.id.txtName);
            country = itemView.findViewById(R.id.txtCountry);
            price = itemView.findViewById(R.id.txtPrice);
            rating = itemView.findViewById(R.id.txtRating);
        }
    }
}
