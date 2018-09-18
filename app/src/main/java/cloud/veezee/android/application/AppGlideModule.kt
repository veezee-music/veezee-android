package cloud.veezee.android.application

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import cloud.veezee.android.R

@GlideModule
class AppGlideModule : AppGlideModule() {

    private val memoryCacheSize: Long = 1024 * 1024 * 20;
    private val diskCacheSize: Long = 1024 * 1024 * 200;

    override fun applyOptions(context: Context, builder: GlideBuilder) {

        val option = RequestOptions();
        option.placeholder(R.drawable.placeholder)
                .centerCrop();

        builder.setMemoryCache(LruResourceCache(memoryCacheSize))
                .setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSize))
                .setDefaultRequestOptions(option);

        super.applyOptions(context, builder);
    }
}