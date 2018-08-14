package cloud.veezee.android.externalComponentsAndLibs;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageViewDependOnHeight extends ImageView {

    public SquareImageViewDependOnHeight(Context context) {
        super(context);
    }

    public SquareImageViewDependOnHeight(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageViewDependOnHeight(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SquareImageViewDependOnHeight(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        setMeasuredDimension(height, height);
    }
}