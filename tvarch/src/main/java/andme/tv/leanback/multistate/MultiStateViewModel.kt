package andme.tv.leanback.multistate

import andme.core.lifecycle.SingleEvent
import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

/**
 * Created by Lucio on 2021/3/3.
 */
open class MultiStateViewModel(application: Application) : AndroidViewModel(application) {

    val isLoadingEvent = MutableLiveData<Boolean>(false)

    val isErrorEvent = MutableLiveData<Boolean>(false)

    val errorMessageEvent = MutableLiveData<String>("")

    val errorButtonTextEvent = MutableLiveData<String>("")

    val errorBackgroundEvent = MutableLiveData<Drawable>()

    val errorImageEvent = MutableLiveData<Drawable>()

    val onErrorButtonClickEvent = SingleEvent()
}