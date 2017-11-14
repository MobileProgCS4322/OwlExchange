package cs4322si.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class OwlitemAdapter extends RecyclerView.Adapter<OwlitemAdapter.OwlitemHolder> {

    private List<Owlitem> owlitemList;

    public class OwlitemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView mTitle;
        private TextView mCategory;
        private TextView mPoster;
        private TextView mDatePosted;
        private ImageView mPicture;

        public OwlitemHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.mTitle);
            mCategory = itemView.findViewById(R.id.mCategory);
            mPoster = itemView.findViewById(R.id.mPoster);
            mDatePosted = itemView.findViewById(R.id.mDatePosted);
            mPicture = itemView.findViewById(R.id.mPicture);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View itemView) {
            //getAdapterPosition();
            //Toast.makeText(itemView.getContext(), "position = " + getLayoutPosition(), Toast.LENGTH_SHORT).show();

            Intent i = new Intent(itemView.getContext(), ItemDetailActivity.class);

            Owlitem currItem = owlitemList.get(getLayoutPosition());
            i.putExtra("currentItem", (Parcelable) currItem);
            //BitmapDrawable drawable = (BitmapDrawable) holder.mPicture.getDrawable();
            //Bitmap bitmap = drawable.getBitmap();

            //Drawable dr = mPicture.getDrawable();
            //Bitmap bmp =  ((GlideBitmapDrawable)dr.getCurrent()).getBitmap();
            //i.putExtra("mainPicture", bmp);

            itemView.getContext().startActivity(i);

        }

        @Override
        public boolean onLongClick(View itemView) {
            //Toast.makeText(itemView.getContext(), "LONG CLICK position = " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
            return true;
        }

   }

    public OwlitemAdapter(List<Owlitem> owlitemList) {
        this.owlitemList = owlitemList;
    }

    @Override
    public OwlitemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemlayout, parent, false);
        return new OwlitemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OwlitemHolder holder, int position) {
        Owlitem currItem = owlitemList.get(position);
        holder.mTitle.setText(currItem.title);
        holder.mCategory.setText(currItem.category);
        holder.mPoster.setText(currItem.username);

        Date datePosted = new Date(currItem.datePosted);
        SimpleDateFormat spf= new SimpleDateFormat("MMM dd yyyy");
        holder.mDatePosted.setText(spf.format(datePosted));

        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(currItem.imageLoc);
        Glide.with(holder.mPicture.getContext())
                .using(new FirebaseImageLoader())
                .load(gsReference)
                .into(holder.mPicture);

            }

    @Override
    public int getItemCount() {
        return owlitemList.size();
    }

}
