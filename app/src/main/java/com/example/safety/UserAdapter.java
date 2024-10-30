package com.example.safety;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
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

import java.util.List;

import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.ArrowOrientation;
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
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
        User user = userList.get(position);

        // Get full and truncated name
        String fullName = user.getName();
        String truncatedName = fullName.length() > 7 ? fullName.substring(0, 7) + "..." : fullName;
        Balloon balloon = new Balloon.Builder(context)
                .setText(fullName)
                .setTextSize(15f)
                .setTextColorResource(R.color.white)
                .setBackgroundColorResource(R.color.TabSelected) // Change to your preferred color
                .setArrowSize(20) // Size of the pointer arrow
                .setArrowOrientation(ArrowOrientation.BOTTOM)
                .setArrowPosition(0.5f) // Center the arrow horizontally
                .setPadding(8)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
                .setCornerRadius(8f)
                .setBalloonAnimation(BalloonAnimation.FADE) // Choose an animation
                .setDismissWhenTouchOutside(true)
                .build();
        // Set initial truncated name
        holder.userName.setText(truncatedName);

        // Set tooltip or fallback for older versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API level 26 and above
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


        } else { // For API level below 26
            holder.userName.setOnLongClickListener(v -> {
                Toast.makeText(context, fullName, Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // Load profile image using Glide
        Glide.with(context)
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.baseline_person_24) // Fallback image
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
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





}
