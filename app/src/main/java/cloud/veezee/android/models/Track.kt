package cloud.veezee.android.models

class Track {

    var _id: MutableMap<String, String>? = null

    var id: String? = ""
        get() = mongodbObjectIdTransformer()

    var title: String? = null;
    var fileName: String? = null;
    var album: Album? = null;
    var image: String? = null;
    var colors: Color? = null;

    private fun mongodbObjectIdTransformer(): String? {
        return this._id!!["\$oid"]!!;
    }
}