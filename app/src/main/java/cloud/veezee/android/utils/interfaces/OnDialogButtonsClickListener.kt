package cloud.veezee.android.utils.interfaces

import android.app.AlertDialog

interface OnDialogButtonsClickListener {
    fun addClickListener(dialog: AlertDialog);
    fun cancelCLickListener(dialog: AlertDialog);
}