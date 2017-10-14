package cs4322si.myapplication;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class OwlitemHolder extends RecyclerView.ViewHolder {

    private final TextView mTitle;
    private final TextView mCategory;
    private final TextView mPoster;
    private final ImageView mPicture;

    //StorageReference storageRef = storage.getReference();

    //private final FrameLayout mLeftArrow;
    //private final FrameLayout mRightArrow;
    //private final RelativeLayout mMessageContainer;
    //private final LinearLayout mMessage;
    //private final int mGreen300;
    //private final int mGray300;

    public OwlitemHolder(View itemView) {
        super(itemView);
        mTitle = itemView.findViewById(R.id.mTitle);
        mCategory = itemView.findViewById(R.id.mCategory);
        mPoster = itemView.findViewById(R.id.mPoster);
        mPicture = itemView.findViewById(R.id.mPicture);

        //mGreen300 = ContextCompat.getColor(itemView.getContext(), R.color.material_green_300);
        //mGray300 = ContextCompat.getColor(itemView.getContext(), R.color.material_gray_300);
    }

    public void bind(Owlitem item) {
        mTitle.setText(item.title);
        mCategory.setText(item.category);
        mPoster.setText(item.username);

        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //setIsSender(currentUser != null && chat.getUid().equals(currentUser.getUid()));
    }


/*    private void setName(String name) {
        mNameField.setText(name);
    }

    private void setText(String text) {
        mTextField.setText(text);
    }*/

/*
    private void setIsSender(boolean isSender) {
        final int color;
        if (isSender) {
            color = mGreen300;
            mLeftArrow.setVisibility(View.GONE);
            mRightArrow.setVisibility(View.VISIBLE);
            mMessageContainer.setGravity(Gravity.END);
        } else {
            color = mGray300;
            mLeftArrow.setVisibility(View.VISIBLE);
            mRightArrow.setVisibility(View.GONE);
            mMessageContainer.setGravity(Gravity.START);
        }

        ((GradientDrawable) mMessage.getBackground()).setColor(color);
        ((RotateDrawable) mLeftArrow.getBackground()).getDrawable()
                .setColorFilter(color, PorterDuff.Mode.SRC);
        ((RotateDrawable) mRightArrow.getBackground()).getDrawable()
                .setColorFilter(color, PorterDuff.Mode.SRC);
    }
*/


}
