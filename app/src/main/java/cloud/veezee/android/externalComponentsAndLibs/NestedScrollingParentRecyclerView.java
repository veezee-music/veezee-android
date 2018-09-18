package cloud.veezee.android.externalComponentsAndLibs;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class NestedScrollingParentRecyclerView extends RecyclerView {
    private boolean mChildIsScrolling = false;
    private int mTouchSlop;
    private float mOriginalX;
    private float mOriginalY;

    public NestedScrollingParentRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public NestedScrollingParentRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NestedScrollingParentRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll
            mChildIsScrolling = false;
            return false; // Let child handle touch event
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mChildIsScrolling = false;
                setOriginalMotionEvent(ev);
            }
            case MotionEvent.ACTION_MOVE: {
                if (mChildIsScrolling) {
                    // Child is scrolling so let child handle touch event
                    return false;
                }

                // If the user has dragged her finger horizontally more than
                // the touch slop, then child view is scrolling

                final int xDiff = calculateDistanceX(ev);
                final int yDiff = calculateDistanceY(ev);

                // Touch slop should be calculated using ViewConfiguration
                // constants.
                if (xDiff > mTouchSlop && xDiff > yDiff) {
                    mChildIsScrolling = true;
                    return false;
                }
            }
        }

        // In general, we don't want to intercept touch events. They should be
        // handled by the child view.  Be safe and leave it up to the original definition
        return super.onInterceptTouchEvent(ev);
    }

    public void setOriginalMotionEvent(MotionEvent ev) {
        mOriginalX = ev.getX();
        mOriginalY = ev.getY();
    }

    public int calculateDistanceX(MotionEvent ev) {
        return (int) Math.abs(mOriginalX - ev.getX());
    }

    public int calculateDistanceY(MotionEvent ev) {
        return (int) Math.abs(mOriginalY - ev.getY());
    }
}
