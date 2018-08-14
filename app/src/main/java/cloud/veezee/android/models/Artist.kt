package cloud.veezee.android.models

class Artist {
    var _id: MutableMap<String, String>? = null

    var id: String? = ""
        get() = mongodbObjectIdTransformer();

    var name: String? = null;

    private fun mongodbObjectIdTransformer(): String? {
        return this._id!!["\$oid"]!!;
    }
}