package cloud.veezee.android.models

class HomePageItem {

    companion object {
        const val ALBUM = "Album";
        const val TRACK = "Track"
        const val HEADER = "Header"
        const val GENRE = "Genre"
    }

    var  type: String? = null;
    var title: kotlin.String? = null;

    var headerList: ArrayList<Header>? = null;
    var albumList: ArrayList<Album>? = null;
    var trackList: ArrayList<Track>? = null;
    var genreList: ArrayList<Genre>? = null;
}