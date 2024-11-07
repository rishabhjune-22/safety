package com.example.safety;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

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

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.ArrowOrientation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static final String TAG = "UserAdapter";
    private final Context context;
    private final List<User> userList; // Original user list
    private List<User> filteredList; // Filtered list for searching

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList);
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
        String lastSeenString = user.getLastSeen();
        Log.d(TAG, "User last_seen: " + lastSeenString);
        String formattedLastSeen = formatLastSeen(parseDate(lastSeenString));
        holder.lastseen.setText(formattedLastSeen != null ? formattedLastSeen : "N/A");

        String truncatedName = (fullName != null && fullName.length() > 7)
                ? fullName.substring(0, 7) + "..."
                : (fullName != null ? fullName : "Unknown");

        Balloon balloon = new Balloon.Builder(context)
                .setText(fullName != null ? fullName : "No Name Available")
                .setTextSize(15f)
                .setTextColorResource(R.color.white)
                .setBackgroundColorResource(R.color.light_sky_blue)
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

    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatLastSeen(Date lastSeenDate) {
        if (lastSeenDate == null) return null;

        Calendar lastSeenCalendar = Calendar.getInstance();
        lastSeenCalendar.setTime(lastSeenDate);

        Calendar today = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        if (today.get(Calendar.YEAR) == lastSeenCalendar.get(Calendar.YEAR)) {
            if (today.get(Calendar.DAY_OF_YEAR) == lastSeenCalendar.get(Calendar.DAY_OF_YEAR)) {
                return "Last seen today at " + timeFormat.format(lastSeenDate);
            } else if (today.get(Calendar.DAY_OF_YEAR) - lastSeenCalendar.get(Calendar.DAY_OF_YEAR) == 1) {
                return "Last seen yesterday at " + timeFormat.format(lastSeenDate);
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d 'at' hh:mm a", Locale.getDefault());
        return "Last seen " + dateFormat.format(lastSeenDate);
    }


    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastseen;
        ShapeableImageView profileImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            profileImage = itemView.findViewById(R.id.profile_image);
            lastseen = itemView.findViewById(R.id.last_seen);
        }
    }

    // Filter method with DiffUtil
    public void filter(String text) {
        List<User> newFilteredList = new ArrayList<>();
        if (text.isEmpty()) {
            newFilteredList.addAll(userList);
        } else {
            text = text.toLowerCase();
            for (User user : userList) {
                if (user.getName().toLowerCase().startsWith(text)) {
                    newFilteredList.add(user);
                }
            }
        }

        // Calculate differences between old and new filtered lists
        UserDiffCallback diffCallback = new UserDiffCallback(filteredList, newFilteredList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // Update the filtered list and dispatch the updates
        filteredList.clear();
        filteredList.addAll(newFilteredList);
        diffResult.dispatchUpdatesTo(this);

        Log.d(TAG, "filter: Filtered list size after filtering = " + filteredList.size());
    }

    // Method to update the user list using DiffUtil
    public void updateUserList(List<User> newUserList) {
        UserDiffCallback diffCallback = new UserDiffCallback(this.filteredList, newUserList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.userList.clear();
        this.userList.addAll(newUserList);
        this.filteredList.clear();
        this.filteredList.addAll(newUserList);

        diffResult.dispatchUpdatesTo(this);
    }

    // Static inner class for UserDiffCallback
    private static class UserDiffCallback extends DiffUtil.Callback {

        private final List<User> oldList;
        private final List<User> newList;

        public UserDiffCallback(List<User> oldList, List<User> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Assuming User has a unique name or ID
            return oldList.get(oldItemPosition).getName().equals(newList.get(newItemPosition).getName());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            User oldUser = oldList.get(oldItemPosition);
            User newUser = newList.get(newItemPosition);

            // Compare the contents of each user to see if they are the same
            return oldUser.getName().equals(newUser.getName()) &&
                    oldUser.getProfileImageUrl().equals(newUser.getProfileImageUrl()) &&
                    oldUser.getLastSeen().equals(newUser.getLastSeen());
        }
    }


}
