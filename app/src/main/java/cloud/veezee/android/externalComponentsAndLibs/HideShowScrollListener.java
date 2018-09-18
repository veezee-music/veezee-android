package cloud.veezee.android.externalComponentsAndLibs;

import android.support.v7.widget.RecyclerView;

public abstract class HideShowScrollListener extends RecyclerView.OnScrollListener {

    private static final int HIDE_THRESHOLD_HIDE = 0;
    private static final int HIDE_THRESHOLD_SHOW = 0;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (scrolledDistance > HIDE_THRESHOLD_HIDE && controlsVisible) {
            hide();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD_SHOW && !controlsVisible) {
            show();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }

        int scrollOffset = recyclerView.computeVerticalScrollOffset();
    }

    public abstract void hide();
    public abstract void show();
}