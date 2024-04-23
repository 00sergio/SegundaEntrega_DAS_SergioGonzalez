package com.example.cookit;



import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.RecipeViewHolder> {

    private List<Receta> recipeList;
    private OnItemClickListener mListener;
    private static final String CHANNEL_ID = "CookItChannel";


    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public RecetaAdapter(List<Receta> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receta, parent, false);
        return new RecipeViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Receta receta = recipeList.get(position);
        holder.recipeNameTextView.setText(receta.getNombre());
        holder.recipeDescriptionTextView.setText(receta.getIngredientes());


    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView recipeNameTextView;
        TextView recipeDescriptionTextView;
        Button buttonEdit;
        Button buttonDelete;


        RecipeViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            recipeNameTextView = itemView.findViewById(R.id.recipe_name);
            recipeDescriptionTextView = itemView.findViewById(R.id.recipe_ingredientes);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);

            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEditClick(position);
                        }
                    }
                }
            });

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

        }
    }
    private void sendNotification(Context context, String recipeName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "CookIt Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Nueva Receta Agregada")
                .setContentText("Se ha agregado una nueva receta: " + recipeName)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setAutoCancel(true);

        notificationManager.notify(0, builder.build());
    }

    public void showAddRecipeDialog(Context context, String recipeName) {
        sendNotification(context, recipeName);
    }
}


