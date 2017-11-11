package cs4322si.myapplication;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.View.GONE;

public class OwlmessageHolder extends RecyclerView.ViewHolder {

    private final TextView mNameField;
    private final TextView mTextField;
    private final TextView mTimeStamp;
    private final FrameLayout mLeftArrow;
    private final FrameLayout mRightArrow;
    private final RelativeLayout mMessageContainer;
    private final LinearLayout mMessage;
    private final int mGreen300;
    private final int mGray300;


    public OwlmessageHolder(View itemView) {
        super(itemView);
        mNameField = itemView.findViewById(R.id.name_text);
        mTextField = itemView.findViewById(R.id.message_text);
        mTimeStamp = itemView.findViewById(R.id.timestamp_text);
        mLeftArrow = itemView.findViewById(R.id.left_arrow);
        mRightArrow = itemView.findViewById(R.id.right_arrow);
        mMessageContainer = itemView.findViewById(R.id.message_container);
        mMessage = itemView.findViewById(R.id.message);
        mGreen300 = ContextCompat.getColor(itemView.getContext(), R.color.material_green_300);
        mGray300 = ContextCompat.getColor(itemView.getContext(), R.color.material_gray_300);
    }

    public void bind(Owlmessage msg, Owlitem currItem) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //if currentUser is the owner of item, show all messages.
        //  if (currentUser.getUid() == currItem.ownerKey)
        //otherwise, only show message if currentUser=sender/receiver.
        // or sender=receiver=owner (sent to all)

        String currUserId = currentUser.getUid();
        if ((currUserId.equals(currItem.ownerKey)) ||
            (currUserId.equals(msg.senderUserid)) ||
            (currUserId.equals(msg.receiverUserid)) ||
           ((msg.senderUserid.equals(msg.receiverUserid)) && msg.senderUserid.equals(currItem.ownerKey))) {

            mNameField.setText(msg.senderUsername);
            mTextField.setText(msg.message);

            Date timestamp = new Date(msg.timestamp);
            SimpleDateFormat spf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            mTimeStamp.setText(spf.format(timestamp));


            setIsSender(currentUser != null && msg.senderUserid.equals(currentUser.getUid()));
        }
        else {
            mMessageContainer.getLayoutParams().height = 0;
            mMessageContainer.requestLayout();
            mMessageContainer.setVisibility(GONE);
        }
    }

    private void setIsSender(boolean isSender) {
        final int color;
        if (isSender) {
            color = mGreen300;
            mLeftArrow.setVisibility(GONE);
            mRightArrow.setVisibility(View.VISIBLE);
            mMessageContainer.setGravity(Gravity.END);
        } else {
            color = mGray300;
            mLeftArrow.setVisibility(View.VISIBLE);
            mRightArrow.setVisibility(GONE);
            mMessageContainer.setGravity(Gravity.START);
        }

        ((GradientDrawable) mMessage.getBackground()).setColor(color);
        ((RotateDrawable) mLeftArrow.getBackground()).getDrawable()
                .setColorFilter(color, PorterDuff.Mode.SRC);
        ((RotateDrawable) mRightArrow.getBackground()).getDrawable()
                .setColorFilter(color, PorterDuff.Mode.SRC);
    }
}

