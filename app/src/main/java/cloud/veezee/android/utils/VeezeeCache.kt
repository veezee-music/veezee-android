package cloud.veezee.android.utils

import android.content.Context
import cloud.veezee.android.Constants
import com.danikula.videocache.HttpProxyCacheServer
import com.danikula.videocache.file.FileNameGenerator
import cloud.veezee.android.application.App
import java.io.File

class VeezeeCache(val context: Context) {

    private var mProxy: HttpProxyCacheServer? = null;
    private val HOME_PAGE_CONTENT_CACHE_NAME = "iVeezeeContentCache"

    var proxy: HttpProxyCacheServer? = null
        get() = myProxy();

    private fun myProxy(): HttpProxyCacheServer {

        if (mProxy == null)
            mProxy = videoCacheProxyBuilder();

        return mProxy!!;
    }

    private fun videoCacheProxyBuilder(): HttpProxyCacheServer = HttpProxyCacheServer.Builder(context)
            .cacheDirectory(File(Constants.DIRECTORY, "cache/audio"))
            .fileNameGenerator(CacheFileNameGenerator())
            .build();

    fun isContentCacheExist(): Boolean = SharedPreferencesHelper(context).exist(HOME_PAGE_CONTENT_CACHE_NAME);

    fun cacheHomePageContent(contentJson: String): Boolean = SharedPreferencesHelper(context).save(HOME_PAGE_CONTENT_CACHE_NAME, contentJson);

    fun retrieveHomePageContentJson(): String = SharedPreferencesHelper(context).get(HOME_PAGE_CONTENT_CACHE_NAME)!!;

    class CacheFileNameGenerator : FileNameGenerator {
        override fun generate(url: String?): String {
            val id = url?.split("/");
            return id?.get(id.lastIndex)?.replace(".mp3", "")!!;
        }
    }
}