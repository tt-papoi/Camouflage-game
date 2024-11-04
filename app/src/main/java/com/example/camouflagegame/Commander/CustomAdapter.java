package com.example.camouflagegame.Commander;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camouflagegame.R;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private final Context context;
    private final Commander[] commanders;
    private OnItemClickListener onItemClickListener;
    private int selectedItemPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(Commander commander, int position);
    }

    public CustomAdapter(Context context, Commander[] commanders) {
        this.context = context;
        this.commanders = commanders;
    }

    public void setSelectedItemPosition(int position) {
        selectedItemPosition = position;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.thumbnail.setImageResource(commanders[position].getThumbnailID());

        if (commanders[position].getIsLock() == true){
            holder.thumbnail.setAlpha(0.3f);
            holder.iconLock.setVisibility(View.VISIBLE);
        }
        else{
            holder.thumbnail.setAlpha(1f);
            holder.iconLock.setVisibility(View.INVISIBLE);
        }

        if (position == selectedItemPosition) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.switch_states));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (onItemClickListener != null && clickedPosition != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(commanders[clickedPosition], clickedPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commanders.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        ImageView iconLock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            iconLock = itemView.findViewById(R.id.thumbnail_lock_icon);
        }
    }
}
