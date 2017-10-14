package cs4322si.myapplication;

//this class not really used yet - placeholder

import java.util.HashMap;
import java.util.Map;

public class Owluser {

    public String username;
    public Map<String, Boolean> myItems = new HashMap<>();

    //private String firstname;
    //private String lastname;
    //private String phone;

    public Owluser () {

    }

    public Owluser(String username) {
        this.username = username;
    }

}
