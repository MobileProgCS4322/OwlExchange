package cs4322si.myapplication;

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
    public String description;
    public String category;
    public long datePosted;
    public boolean traded = false;  //sold yet?

    public Owlitem() {}

    public Owlitem(String owner, String description, String category, long datePosted) {
        this.owner = owner;
        this.description = description;
        this.category = category;
        this.datePosted = datePosted;
    }
}
