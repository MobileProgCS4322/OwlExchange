package cs4322si.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Owlitem implements Parcelable{

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

    public String itemId;   //this is both a unique key identifier, and the location string in JSON firebase db.

    public String username;  //username of who posted it
    public String ownerKey;

    public String title;
    public String description;
    public String category;
    public long datePosted;
    public String imageLoc; //address of image in firebase storage.

    public boolean traded = false;  //sold yet?

    public Owlitem() {}

    public Owlitem(String itemId, String username, String ownerKey, String title, String description, String category, long datePosted, String imageLoc) {
        this.itemId = itemId;
        this.title = title;
        this.username = username;
        this.ownerKey = ownerKey;
        this.description = description;
        this.category = category;
        this.datePosted = datePosted;
        this.imageLoc = imageLoc;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("itemId", itemId);
        result.put("username", username);
        result.put("ownerKey", ownerKey);
        result.put("title", title);
        result.put("description", description);
        result.put("category", category);
        result.put("datePosted", datePosted);
        result.put("imageLoc", imageLoc);
        result.put("traded", traded);
        return result;
    }


    // Parcelling part
    public Owlitem(Parcel in){
        this.itemId = in.readString();
        this.title = in.readString();
        this.username = in.readString();
        this.ownerKey = in.readString();
        this.category = in.readString();
        this.description =  in.readString();
        this.datePosted =  in.readLong();
        this.imageLoc =  in.readString();
    }

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.itemId);
        dest.writeString(this.title);
        dest.writeString(this.username);
        dest.writeString(this.ownerKey);
        dest.writeString(this.category);
        dest.writeString(this.description);
        dest.writeLong(this.datePosted);
        dest.writeString(this.imageLoc);
    }

    @Exclude
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Owlitem createFromParcel(Parcel in) {
            return new Owlitem(in);
        }

        public Owlitem[] newArray(int size) {
            return new Owlitem[size];
        }
    };

}
