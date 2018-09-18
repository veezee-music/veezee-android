package cloud.veezee.android.adapters.interfaces

interface OnListItemClickListener {
    fun onClick(id: String, position: Int, extra: Int? = null);
}