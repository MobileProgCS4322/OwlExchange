package cs4322si.myapplication;

import android.content.Context;
import android.graphics.Matrix;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import java.io.Console;

//haven't used this class yet - can't figure out what's going on with the ImageView
//I need access to an actual android phone to do some serious image debugging.
//
//UPDATE: I figured out what was going on with the Images - they were being SAVED with gray space -
//so this class is probably not necessary
public class ImageViewFullView extends AppCompatImageView {

    private final String TAG = "ImageViewFullView: ";

    public ImageViewFullView(Context context) {
        super(context);
        setup();
    }

    public ImageViewFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ImageViewFullView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected boolean setFrame(int frameLeft, int frameTop, int frameRight, int frameBottom) {

        //float frameWidth = frameRight - frameLeft;
        float frameHeight = frameBottom - frameTop;
        //Log.i(TAG, frameLeft + " , " + frameTop + " , " + frameRight + " , " + frameBottom);

        if (getDrawable() != null) {

            Matrix matrix = getImageMatrix();
            float scaleFactor, scaleFactorWidth, scaleFactorHeight;

            //scaleFactorWidth = (float) frameWidth / (float) getDrawable().getIntrinsicWidth();
            scaleFactorHeight = (float) frameHeight / (float) getDrawable().getIntrinsicHeight();

            //if (scaleFactorHeight > scaleFactorWidth) {
                scaleFactor = scaleFactorHeight;
            //} else {
            //    scaleFactor = scaleFactorWidth;
            //}

            matrix.setScale(scaleFactor, scaleFactor, 0, 0);
            setImageMatrix(matrix);
        }

        return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
    }


}
