package cs4322si.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class OwlitemHolder extends RecyclerView.ViewHolder {

    private final TextView mTitle;
    private final TextView mCategory;
    private final TextView mPoster;
    public final ImageView mPicture;

    //Interface to send callbacks...
    public interface ClickListener{
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    private OwlitemHolder.ClickListener mClickListener;

    public void setOnClickListener(OwlitemHolder.ClickListener clickListener){
        mClickListener = clickListener;
    }

    //StorageReference s orageRef = storage.getReference();

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

        //listener set on ENTIRE ROW, you may set on individual components within a row.
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickListener.onItemLongClick(v, getAdapterPosition());
                return true;
            }
        });


    }

    public void bind(Owlitem item, FirebaseStorage storage, Context context) {
        mTitle.setText(item.title);
        mCategory.setText(item.category);
        mPoster.setText(item.username);

        StorageReference gsReference = storage.getReferenceFromUrl(item.imageLoc);
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(gsReference)
                .into(mPicture);

/*        final long ONE_MEGABYTE = 1024 * 1024;
        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });     */
        
        
        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //setIsSender(currentUser != null && chat.getUid().equals(currentUser.getUid()));
    }

}
