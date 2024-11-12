package com.example.safety;

import com.google.firebase.database.PropertyName;

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



    public User() {
        // Empty constructor needed for Firestore deserialization
    }

    public User(String name, String profileImageUrl,String last_seen,String home_check_in,String home_check_out,String office_check_in,String office_check_out,String emp_id,boolean is_loc_enabled) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.last_seen =last_seen;
        this.home_check_in = home_check_in;
        this.home_check_out = home_check_out;
        this.office_check_in = office_check_in;
        this.office_check_out = office_check_out;
        this.emp_id = emp_id;
        this.is_loc_enabled = is_loc_enabled;

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


  public String getHome_check_in(){
return home_check_in;


  }
  public String getHome_check_out(){
        return home_check_out;

}
  public String getOffice_check_in() {
      return office_check_in;
  }
  public String getOffice_check_out() {
      return office_check_out;
  }

  public String getEmp_id() {
      return emp_id;
  }
  public boolean getIs_loc_enabled() {
      return is_loc_enabled;
  }



}


