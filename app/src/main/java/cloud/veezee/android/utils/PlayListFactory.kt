package cloud.veezee.android.utils

import android.content.Context
import cloud.veezee.android.Constants
import com.google.gson.Gson
import cloud.veezee.android.utils.interfaces.OfflinePlayListResponseListener
import cloud.veezee.android.models.Album
import cloud.veezee.android.models.HomePageItem
import cloud.veezee.android.models.PlayableItem
import cloud.veezee.android.models.Track
import java.util.*
import kotlin.collections.ArrayList

class PlayListFactory(val context: Context) {

    private var proxy = VeezeeCache(context).proxy;

    fun album(album: Album?): ArrayList<PlayableItem> {

        val playList: ArrayList<PlayableItem> = ArrayList();
        var item: PlayableItem?;

        val albumCopyJson = Gson().toJson(album);
        val albumCopy = Gson().fromJson<Album>(albumCopyJson, Album::class.java);
        albumCopy.tracks = null;

        for (i in 0 until (album?.tracks?.size ?: 0)) {

            val track: Track? = album?.tracks?.get(i);

            item = Couchbase.getInstance(context)?.getById(track?.id!!);
            if(item == null) {

                val proxy: String = if(Constants.OFFLINE_ACCESS) proxy!!.getProxyUrl(track?.fileName)!! else track!!.fileName!!;

                item = PlayableItem(proxy);
                item._id = track?._id
                item.title = track?.title;
                item.album = albumCopy;

                if (album?.artist != null) {
                    // is album
                    item.imageUrl = album.image;
                    item.artist = track?.album?.artist ?: album.artist;
                    item.colors = album.colors;

                } else {
                    // is play home
                    item.imageUrl = track?.image ?: album?.image;
                    item.artist = track?.album?.artist;
                    item.colors = track?.colors ?: album?.colors;
                }
            }
            playList.add(item);
        }
        return playList;
    }

    fun track(tracks: ArrayList<Track>?): ArrayList<PlayableItem> {

        val playList: ArrayList<PlayableItem> = ArrayList();
        var item: PlayableItem?;

        for (i in 0 until tracks?.size!!) {

            val track = tracks[i];

            item = Couchbase.getInstance(context)?.getById(track.id!!);
            if(item == null) {
                val proxy: String = if(Constants.OFFLINE_ACCESS) proxy!!.getProxyUrl(track.fileName)!! else track.fileName!!;

                item = PlayableItem(proxy);
                item._id = track._id
                item.title = track.title;
                item.imageUrl = track.image;
                item.album = track.album
                item.artist = track.album?.artist;
                item.colors = track.colors;
            }

            playList.add(item);
        }

        return playList;
    }

    fun shuffle(playList: ArrayList<PlayableItem>): ArrayList<PlayableItem> {
        playList.shuffle();

        return playList;
    }

    fun offlinePlayList(items: ArrayList<PlayableItem>, listener: OfflinePlayListResponseListener) {

        val albums = ArrayList<Album>();

        for (playableItem: PlayableItem in items) {
            if (playableItem.album == null) {
                continue;
            }

            var skip = false;

            for (album in albums) {
                if (album.id == playableItem.album?.id) {
                    skip = true;
                    break;
                }
            }

            if (skip)
                continue;

            albums.add(playableItem.album!!);
        }

        val tracks = ArrayList<Track>();
        for (playableItem: PlayableItem in items) {
            val track: Track = Track();

            track._id = playableItem._id;
            track.title = playableItem.title;

            val albumCopyJson = Gson().toJson(playableItem.album);
            val albumCopy = Gson().fromJson<Album>(albumCopyJson, Album::class.java);

            track.album = albumCopy;
            track.fileName = playableItem.fileName
            track.image = playableItem.imageUrl;
            track.colors = playableItem.colors;

            val trackCopyJson = Gson().toJson(track);
            val trackCopy: Track = Gson().fromJson(trackCopyJson, Track::class.java);

            tracks.add(track);

            for (album in albums) {
                if (album.id == track.album?.id) {
                    if (album.tracks == null) {
                        album.tracks = ArrayList<Track>();
                    }
                    album.tracks!!.add(trackCopy);
                }
            }
        }

        val finalAlbums = ArrayList<Album>();
        val finalTracks = ArrayList<Track>();

        for (album in albums) {
            if (album.tracks?.size!! > 1) {
                finalAlbums.add(album);
            } else {
                finalTracks.add(album.tracks?.first()!!)
            }
        }

        val homePageItems: ArrayList<HomePageItem> = ArrayList();
        val title: ArrayList<String> = ArrayList();
        val type: ArrayList<String> = ArrayList();

        if(finalTracks.size != 0) {
            title.add("Track");
            type.add(HomePageItem.TRACK)
        }

        if(finalAlbums.size != 0) {
            title.add("Album");
            type.add(HomePageItem.ALBUM);
        }

        Collections.reverse(finalAlbums);
        Collections.reverse(finalTracks);

        for (i in 0 until title.size) {

            val homePageItem = HomePageItem();
            homePageItem.type = type[i];
            homePageItem.title = title[i];
            if(type[i] == HomePageItem.TRACK)
                homePageItem.trackList = finalTracks;
            else
                homePageItem.albumList = finalAlbums;

            homePageItems.add(homePageItem);
        }

        listener.response(Gson().toJson(homePageItems));
    }
}