package cloud.veezee.android.models


class PlayableItem(url: String) {

    var _id: MutableMap<String, String>? = null

    var id: String? = ""
        get() = mongodbObjectIdTransformer()

    var fileName: String = url;
    var imageUrl: String? = null;
    var title: String? = null;
    var artist: Artist? = null
    var colors: Color? = null;
    var album: Album? = null;

    private fun mongodbObjectIdTransformer(): String? {
        return this._id!!["\$oid"]!!;
    }
}