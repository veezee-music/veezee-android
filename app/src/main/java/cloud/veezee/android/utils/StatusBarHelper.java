package cloud.veezee.android.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class StatusBarHelper {
//    public static void setLightStatusBar(View view){
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            int flags = view.getSystemUiVisibility();
//            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//            view.setSystemUiVisibility(flags);
//        }
//    }
//
//    public static void clearLightStatusBar(Context context,View view, int color) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            int flags = view.getSystemUiVisibility();
//            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//            view.setSystemUiVisibility(flags);
//
//            Window window = ((Activity)context).getWindow();
//            window.setStatusBarColor(color);
//        }
//    }

    public static Integer getHeight(Window window) {
        Rect rect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);

        return rect.top;
    }

    public static void showStatusBar(Window window) {
        View view = window.getDecorView();

        //window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        int flags = view.getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(flags);
    }

    public static void hideStatusBar(Window window) {
        View view = window.getDecorView();

        ///window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        int flags = view.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(flags);
    }

    public static void toggleStatusBar(Window window) {
        View view = window.getDecorView();

        int uiOptions = view.getSystemUiVisibility();
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);
    }
}
