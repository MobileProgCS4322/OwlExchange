package cs4322si.myapplication;

public class Owlmessage {

    public String senderUserid;
    public String senderUsername;       //for convenience

    public String receiverUserid;
    public String receiverUsername;     //for convenience

    public String itemId;
    public String itemDescription;      //for convenience

    public long timestamp = System.currentTimeMillis();


    public Owlmessage() {}

    public Owlmessage(String senderUsername, String senderUserid, String receiverUsername, String receiverUserid, String itemId, String itemDescription) {
        this.senderUsername = senderUsername;
        this.senderUserid = senderUserid;
        this.receiverUsername = receiverUsername;
        this.receiverUserid = receiverUserid;
        this.itemId = itemId;
        this.itemDescription = itemDescription;
    }

}
