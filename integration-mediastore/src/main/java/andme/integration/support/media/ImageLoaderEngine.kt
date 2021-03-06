package andme.integration.support.media

import andme.core.imageLoaderAM
import android.content.Context
import android.widget.ImageView
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.listener.OnImageCompleteCallback
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView

/**
 * Created by Lucio on 2020-11-10.
 */
 object ImageLoaderEngine : ImageEngine {

    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        imageLoaderAM.load(imageView,url)
    }

    override fun loadImage(context: Context, url: String, imageView: ImageView, longImageView: SubsamplingScaleImageView?, callback: OnImageCompleteCallback?) {
    }

    override fun loadImage(context: Context, url: String, imageView: ImageView, longImageView: SubsamplingScaleImageView?) {
    }

    override fun loadAsGifImage(context: Context, url: String, imageView: ImageView) {
    }

    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        imageLoaderAM.load(imageView, url)
    }

    override fun loadFolderImage(context: Context, url: String, imageView: ImageView) {
        imageLoaderAM.load(imageView, url)
    }
}