package com.example.safety;

import com.google.firebase.database.PropertyName;

import java.util.List;
import java.util.Map;

public class User {
    private String name;
    private String profileImageUrl;
    private String last_seen;
    private String home_check_in;
    private String home_check_out;
    private String office_check_in;
    private String office_check_out;
    private String emp_id;
    private boolean is_loc_enabled;
    private Map<String, List<String>> homeCheckIn;
    private Map<String, List<String>> homeCheckOut;
    private Map<String, List<String>> officeCheckIn;
    private Map<String, List<String>> officeCheckOut;


    public User() {
        // Empty constructor needed for Firestore deserialization
    }

    public User(String name, String profileImageUrl,String last_seen,String home_check_in,String home_check_out,String office_check_in,String office_check_out,String emp_id,boolean is_loc_enabled,
                Map<String, List<String>> homeCheckIn,
                Map<String, List<String>> homeCheckOut,
                Map<String, List<String>> officeCheckIn,
                Map<String, List<String>> officeCheckOut

    ) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.last_seen =last_seen;
        this.home_check_in = home_check_in;
        this.home_check_out = home_check_out;
        this.office_check_in = office_check_in;
        this.office_check_out = office_check_out;
        this.emp_id = emp_id;
        this.is_loc_enabled = is_loc_enabled;


        this.homeCheckIn = homeCheckIn;
        this.homeCheckOut = homeCheckOut;
        this.officeCheckIn = officeCheckIn;
        this.officeCheckOut = officeCheckOut;

    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }



    @PropertyName("last_seen")
    public String getLastSeen() {
        return last_seen;
    }


    @PropertyName("last_seen")
    public void setLastSeen(String last_seen) {
        this.last_seen = last_seen;
    }




  public String getEmp_id() {
      return emp_id;
  }
  public boolean getIs_loc_enabled() {
      return is_loc_enabled;
  }

    @PropertyName("HOME_GEOFENCE_checkin")
    public Map<String, List<String>> getHomeCheckIn() {
        return this.homeCheckIn;
    }

    @PropertyName("HOME_GEOFENCE_checkin")
    public void setHomeCheckIn(Map<String, List<String>> homeCheckIn) {
        this.homeCheckIn = homeCheckIn;
    }

    // Getter and Setter for home_checkout
    @PropertyName("HOME_GEOFENCE_checkout")
    public Map<String, List<String>> getHomeCheckOut() {
        return this.homeCheckOut;
    }

    @PropertyName("HOME_GEOFENCE_checkout")
    public void setHomeCheckOut(Map<String, List<String>> homeCheckOut) {
        this.homeCheckOut = homeCheckOut;
    }

    // Getter and Setter for office_checkin
    @PropertyName("OFFICE_GEOFENCE_checkin")
    public Map<String, List<String>> getOfficeCheckIn() {
        return this.officeCheckIn;
    }

    @PropertyName("OFFICE_GEOFENCE_checkin")
    public void setOfficeCheckIn(Map<String, List<String>> officeCheckIn) {
        this.officeCheckIn = officeCheckIn;
    }

    // Getter and Setter for office_checkout
    @PropertyName("OFFICE_GEOFENCE_checkout")
    public Map<String, List<String>> getOfficeCheckOut() {
        return this.officeCheckOut;
    }

    @PropertyName("OFFICE_GEOFENCE_checkout")
    public void setOfficeCheckOut(Map<String, List<String>> officeCheckOut) {
        this.officeCheckOut = officeCheckOut;
    }
}


