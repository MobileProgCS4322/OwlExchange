package cs4322si.myapplication;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Owlitem {

/*    static public final String [] categories = {"Books", "Tech", "Food", "Services", "Transportation", "Clothes", "Home", "Entertainment", "Other"};
    static public final int catBooks = 0;
    static public final int catTech = 1;
    static public final int catFood = 2;
    static public final int catServices = 3;
    static public final int catTransportation = 4;
    static public final int catClothes = 5;
    static public final int catHome = 6;
    static public final int catEntertainment = 7;
    static public final int catOther = 8;*/


    public String owner;  //who posted it
    public String title;
    public String description;
    public String category;
    public long datePosted;
    public String imageLoc; //address of image in firebase storage.

    public boolean traded = false;  //sold yet?

    public Owlitem() {}

    public Owlitem(String owner, String title, String description, String category, long datePosted, String imageLoc) {
        this.title = title;
        this.owner = owner;
        this.description = description;
        this.category = category;
        this.datePosted = datePosted;
        this.imageLoc = imageLoc;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("owner", owner);
        result.put("title", title);
        result.put("description", description);
        result.put("category", category);
        result.put("datePosted", datePosted);
        result.put("imageLoc", imageLoc);
        result.put("traded", traded);

        return result;
    }

}
