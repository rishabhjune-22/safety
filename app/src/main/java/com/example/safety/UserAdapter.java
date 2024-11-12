package com.example.safety;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewConfiguration;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
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

        // Fetch user details and check for null
        String fullName = user.getName() != null ? user.getName() : "Unknown";
        String lastSeenString = user.getLastSeen();
        boolean is_loc_enabled=user.getIs_loc_enabled();
        Log.d(TAG, "User last_seen: " + lastSeenString);

        // Parse lastSeen date and format it, with a fallback to "N/A"
        Date lastSeenDate = parseDate(lastSeenString);
        String formattedLastSeen = lastSeenDate != null ? formatLastSeen(lastSeenDate) : "N/A";
        holder.lastseen.setText(formattedLastSeen);


        String formattedoffice_checkin = formatCheckinCheckout(user.getOffice_check_in());
        String formattedoffice_checkout = formatCheckinCheckout(user.getOffice_check_out());
        String formattedhome_checkin= formatCheckinCheckout(user.getHome_check_in());
        String formattedhome_checkout = formatCheckinCheckout(user.getHome_check_out());
        holder.office_checkin.setText(formattedoffice_checkin);
        holder.office_checkout.setText(formattedoffice_checkout);
        holder.home_checkin.setText(formattedhome_checkin);
        holder.home_checkout.setText(formattedhome_checkout);
        holder.emp_id.setText(user.getEmp_id());




        // Truncate name if it's too long
        String truncatedName = fullName.length() > 20 ? fullName.substring(0, 7) + "..." : fullName;

        // Initialize balloon tooltip with fallback text
        Balloon balloon = new Balloon.Builder(context)
                .setText(fullName)
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


        String balloonText = is_loc_enabled ? "GPS Enabled" : "GPS Disabled";
        Balloon balloon2 = new Balloon.Builder(context)
                .setText(balloonText)
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


//        holder.is_loc_enabled.setOnLongClickListener(v -> {
//            // Show balloon tooltip on long press
//            Toast.makeText(context,balloonText, Toast.LENGTH_SHORT).show();
//            return true; // Return true to indicate the long click was handled
//        });

        holder.is_loc_enabled.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.postDelayed(() -> {
                    // Show the balloon on long press
                    balloon2.showAlignTop(holder.is_loc_enabled);
                }, ViewConfiguration.getLongPressTimeout());
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.removeCallbacks(null);
            }
            return true; // Prevents the switch from toggling
        });



        // Set tooltip or Toast for displaying full name based on SDK version
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

        // Load profile image with Glide, with a placeholder in case of null URL
        String profileImageUrl = user.getProfileImageUrl();
        Glide.with(context)
                .load(profileImageUrl != null ? profileImageUrl : R.drawable.baseline_person_24)
                .placeholder(R.drawable.baseline_person_24)
                .into(holder.profileImage);
    }


    private Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            // Return null or handle the case when dateString is null or empty
            return null;
        }

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

    public String formatCheckinCheckout(String time) {
        if (time == null || time.isEmpty()) {
            return "N/A"; // Default value for empty or null timestamps
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        outputFormat.setDateFormatSymbols(new java.text.DateFormatSymbols(Locale.getDefault()) {
            @Override
            public String[] getAmPmStrings() {
                return new String[] { "AM", "PM" }; // Capitalize AM and PM
            }
        });

        try {
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Time"; // Fallback for parsing errors
        }
    }






    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastseen, office_checkin, office_checkout, home_checkin, home_checkout, emp_id;
        ShapeableImageView profileImage;
        SwitchMaterial is_loc_enabled;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            profileImage = itemView.findViewById(R.id.profile_image);
            lastseen = itemView.findViewById(R.id.last_seen);
            office_checkin = itemView.findViewById(R.id.office_checkin);
            office_checkout = itemView.findViewById(R.id.office_checkout);
            home_checkin = itemView.findViewById(R.id.home_checkin);
            home_checkout = itemView.findViewById(R.id.home_checkout);
            emp_id = itemView.findViewById(R.id.emp_id);
            is_loc_enabled = itemView.findViewById(R.id.is_loc_enabled);


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

            // Check if oldUser or newUser is null
            if (oldUser == null || newUser == null) {
                return oldUser == newUser; // Only true if both are null
            }

            // Safely compare each field with null checks
            boolean isNameSame = (oldUser.getName() != null && oldUser.getName().equals(newUser.getName())) ||
                    (oldUser.getName() == null && newUser.getName() == null);

            boolean isProfileImageUrlSame = (oldUser.getProfileImageUrl() != null && oldUser.getProfileImageUrl().equals(newUser.getProfileImageUrl())) ||
                    (oldUser.getProfileImageUrl() == null && newUser.getProfileImageUrl() == null);

            boolean isLastSeenSame = (oldUser.getLastSeen() != null && oldUser.getLastSeen().equals(newUser.getLastSeen())) ||
                    (oldUser.getLastSeen() == null && newUser.getLastSeen() == null);

            boolean isHomecheckInSame = (oldUser.getHome_check_in() != null && oldUser.getHome_check_in().equals(newUser.getHome_check_in())) ||
                    (oldUser.getHome_check_in() == null && newUser.getHome_check_in() == null);

            boolean isHomecheckOutSame = (oldUser.getHome_check_out() != null && oldUser.getHome_check_out().equals(newUser.getHome_check_out())) ||
                    (oldUser.getHome_check_out() == null && newUser.getHome_check_out() == null);

            boolean isOfficeCheckInSame = (oldUser.getOffice_check_in() != null && oldUser.getOffice_check_in().equals(newUser.getOffice_check_in())) ||
                    (oldUser.getOffice_check_in() == null && newUser.getOffice_check_in() == null);
            boolean isOfficeCheckOutSame = (oldUser.getOffice_check_out() != null && oldUser.getOffice_check_out().equals(newUser.getOffice_check_out())) ||
                    (oldUser.getOffice_check_out() == null && newUser.getOffice_check_out() == null);
            boolean isEmpidSame = (oldUser.getEmp_id() != null && oldUser.getEmp_id().equals(newUser.getEmp_id())) ||
                    (oldUser.getEmp_id() == null && newUser.getEmp_id() == null);

            boolean isGetlocSame = oldUser.getIs_loc_enabled() == newUser.getIs_loc_enabled();





            return isNameSame && isProfileImageUrlSame && isLastSeenSame && isHomecheckInSame && isHomecheckOutSame && isOfficeCheckInSame && isOfficeCheckOutSame && isEmpidSame && isGetlocSame;
        }

    }


}
