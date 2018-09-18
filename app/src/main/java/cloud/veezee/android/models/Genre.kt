package cloud.veezee.android.models

class Genre {
    var _id: MutableMap<String, String>? = null

    var id: String? = ""
        get() = mongodbObjectIdTransformer()

    var title: String = "";
    var image: String = "";

    private fun mongodbObjectIdTransformer(): String? {
        return this._id!!["\$oid"]!!;
    }
}