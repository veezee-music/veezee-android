package cloud.veezee.android.models

class Album {

    var _id: MutableMap<String, String>? = null

    var id: String? = ""
        get() = mongodbObjectIdTransformer()

    var title: String? = null;
    var artist: Artist? = null;
    var tracks: ArrayList<Track>? = null;
    var image: String? = null;
    var geners: ArrayList<Genre>? = null;
    var colors: Color? = null;

    private fun mongodbObjectIdTransformer(): String? {
        return this._id!!["\$oid"]!!;
    }

}