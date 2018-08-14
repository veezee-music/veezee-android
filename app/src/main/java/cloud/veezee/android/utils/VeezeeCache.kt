package cloud.veezee.android.utils

import android.content.Context
import com.danikula.videocache.HttpProxyCacheServer
import com.danikula.videocache.file.FileNameGenerator
import cloud.veezee.android.application.App
import java.io.File

class VeezeeCache(val context: Context) {

    private var mProxy: HttpProxyCacheServer? = null;
    private val HOME_PAGE_CONTENT_CACHE_NAME = "iVezzeContentCache"

    var proxy: HttpProxyCacheServer? = null
        get() = myProxy();

    private fun myProxy(): HttpProxyCacheServer {

        if (mProxy == null)
            mProxy = videoCacheProxyBuilder();

        return mProxy!!;
    }

    private fun videoCacheProxyBuilder(): HttpProxyCacheServer = HttpProxyCacheServer.Builder(context)
            .cacheDirectory(File(App.setting?.directory, "cache/audio"))
            .fileNameGenerator(CacheFileNameGenerator())
            .build();

    fun isContentCacheExist(): Boolean = SharedPreferencesHelper.exist(context, HOME_PAGE_CONTENT_CACHE_NAME);

    fun cacheHomePageContent(contentJson: String): Boolean = SharedPreferencesHelper.save(context, HOME_PAGE_CONTENT_CACHE_NAME, contentJson);

    fun retrieveHomePageContentJson(): String = SharedPreferencesHelper.get(context, HOME_PAGE_CONTENT_CACHE_NAME)!!;

    class CacheFileNameGenerator : FileNameGenerator {
        override fun generate(url: String?): String {
            val id = url?.split("/");
            return id?.get(id.lastIndex)?.replace(".mp3", "")!!;
        }
    }
}