/**
 * Created by Lucio on 2020-11-02.

 */
package andme.core.exception

import andme.core.dialogHandlerAM
import andme.core.exceptionHandlerAM
import andme.core.isDebuggable
import andme.core.support.ui.showAlertDialog
import andme.lang.IgnoreException
import andme.lang.orDefaultIfNullOrEmpty
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.fragment.app.Fragment

/**
 * 异常处理器,用于处理程序中相关的各种类型的异常
 */
interface ExceptionHandler {

    /**
     * 处理未捕获的异常
     */
    fun handleUncaughtException(e: Throwable)

    /**
     * 处理被捕获的常规异常：即用户通过tryCatch函数捕获的异常
     */
    fun handleCatchException(e: Throwable)

    /**
     * 处理UI异常：即用户通过tryUi函数捕获的异常
     */
    fun handleUIException(context: Context, e: Throwable)

    /**
     * 获取异常友好的描述信息
     */
    fun getFriendlyMessage(e: Throwable): String?

}

/**
 * 扩展友好消息字段，用于将异常转换成对用户比较容易理解的信息。
 */
inline val Throwable.friendlyMessage: String? get() = exceptionHandlerAM.getFriendlyMessage(this)

/**
 * 对话框形式  显示异常信息
 * @param e
 */
fun Context.showExceptionMsgAM(e: Throwable) {
    if (e is SilentException || e is IgnoreException) return
    showExceptionMsgAM(e.friendlyMessage.orDefaultIfNullOrEmpty(e.toString()))
}

/**
 * 显示异常信息
 */
fun Context.showExceptionMsgAM(msg: String) {
    dialogHandlerAM.showAlertDialog(this, msg, "确定")
}

/**
 * 捕获ui异常
 */
inline fun Context.tryUi(action: () -> Unit): Throwable? {
    return try {
        action()
        null
    } catch (e: Throwable) {
        exceptionHandlerAM.handleUIException(this, e)
        e
    }
}

/**
 * 捕获ui异常
 */
inline fun Context.tryOnCreate(action: () -> Unit): Throwable? {
    return try {
        action()
        null
    } catch (e: Throwable) {
        exceptionHandlerAM.handleCatchException(e)
        showAlertDialog(
            "界面初始化失败:${e.friendlyMessage}",
            Pair("退出", DialogInterface.OnClickListener { dialog, which ->
                (this as? Activity)?.finish()
            }),
            false
        )
        e
    }
}

inline fun View.tryUi(action: () -> Unit): Throwable? {
    return context.tryUi(action)
}

inline fun View.tryUiWithEnable(action: () -> Unit): Throwable? {
    return try {
        isEnabled = false
        action()
        null
    } catch (e: Throwable) {
        exceptionHandlerAM.handleUIException(context, e)
        e
    } finally {
        isEnabled = true
    }
}

inline fun Fragment.tryUi(action: () -> Unit): Throwable? {
    return context?.tryUi(action)
}

/**
 * 异常处理
 * @param printStack 异常时，是否调用printStackTrace方法打印日常
 */
inline fun <T> T.tryCatch(printStack: Boolean = isDebuggable, action: T.() -> Unit): Throwable? {
    try {
        action()
        return null
    } catch (e: Throwable) {
        if (printStack) {
            e.printStackTrace()
        }
        exceptionHandlerAM.handleCatchException(e)
        return e
    }
}

inline fun <T> T.tryIgnore(action: T.() -> Unit): Throwable? {
    try {
        action(this)
        return null
    } catch (e: Throwable) {
        return e
    }
}

inline fun <T> Throwable?.onCatch(block: (Throwable) -> T): T? {
    if (this == null)
        return null
    return block(this)
}