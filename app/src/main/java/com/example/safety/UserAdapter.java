package com.example.safety;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.ArrowOrientation;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static final String TAG = "UserAdapter"; // Tag for logging

    private final Context context;
    private final List<User> userList;
    private List<User> filteredList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList);  // Initialize filteredList with userList
        Log.d(TAG, "Constructor: userList size = " + userList.size());
        Log.d(TAG, "Constructor: filteredList initialized with size = " + filteredList.size());
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_users, parent, false);
        return new UserViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredList.get(position);

        String fullName = user.getName();
        String truncatedName = fullName.length() > 7 ? fullName.substring(0, 7) + "..." : fullName;

        // Set up the tooltip using Balloon
        Balloon balloon = new Balloon.Builder(context)
                .setText(fullName)
                .setTextSize(15f)
                .setTextColorResource(R.color.white)
                .setBackgroundColorResource(R.color.TabSelected)
                .setArrowSize(20)
                .setArrowOrientation(ArrowOrientation.BOTTOM)
                .setArrowPosition(0.5f)
                .setPadding(8)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
                .setCornerRadius(8f)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setDismissWhenTouchOutside(true)
                .build();

        holder.userName.setText(truncatedName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.userName.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        balloon.showAlignTop(holder.userName);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        balloon.dismiss();
                        return true;
                }
                return false;
            });
        } else {
            holder.userName.setOnLongClickListener(v -> {
                Toast.makeText(context, fullName, Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        Glide.with(context)
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.baseline_person_24)
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: filteredList size = " + filteredList.size());
        return filteredList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ShapeableImageView profileImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }

    // Simplified filter method with additional logging
    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            text = text.toLowerCase();
            for (User user : userList) {
                if (user.getName().toLowerCase().startsWith(text)) {
                    filteredList.add(user);
                }
            }
        }

        Log.d(TAG, "filter: Filtered list size after filtering = " + filteredList.size());
        notifyDataSetChanged(); // Refresh the RecyclerView with the updated filtered list
    }
}
