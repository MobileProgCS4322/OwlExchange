package cs4322si.myapplication;

import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class OwlmessageAdapter extends RecyclerView.Adapter<OwlmessageAdapter.OwlmessageHolder> {

    private List<Owlmessage> owlmessageList;

    public class OwlmessageHolder extends RecyclerView.ViewHolder {

        private TextView mNameField;
        private TextView mTextField;
        private TextView mTimeStamp;
        private FrameLayout mLeftArrow;
        private FrameLayout mRightArrow;
        private RelativeLayout mMessageContainer;
        private LinearLayout mMessage;
        private int mGreen300;
        private int mGray300;

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

        public void bind(Owlmessage msg) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            mNameField.setText(msg.senderUsername + "->" + msg.receiverUsername);
            mTextField.setText(msg.message);

            Date timestamp = new Date(msg.timestamp);
            SimpleDateFormat spf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            mTimeStamp.setText(spf.format(timestamp));

            setIsSender(user != null && msg.senderUserid.equals(user.getUid()));
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

    public OwlmessageAdapter(List<Owlmessage> owlmessageList) {
        this.owlmessageList = owlmessageList;
    }

    @Override
    public OwlmessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message, parent, false);
        return new OwlmessageHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OwlmessageHolder holder, int position) {
        Owlmessage msg = owlmessageList.get(position);
        holder.bind(msg);
    }

    @Override
    public int getItemCount() {

        if (owlmessageList == null) {
            return 0;
        }
        else {
            return owlmessageList.size();
        }
    }

    public void updateList(List<Owlmessage> owlmessageList) {
        this.owlmessageList = owlmessageList;
        notifyDataSetChanged();
    }

}
