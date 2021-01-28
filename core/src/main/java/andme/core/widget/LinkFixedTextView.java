
package andme.core.widget;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * @包名 ucux.app.widgets
 * @文件名 LinkFixedTextView.java
 * @作者 luochao
 * @创建日期 2015-6-8
 * @版本 V 1.0
 * 修正ListView 的 Item中如果TextView包含超链接，只响应超链接点击事件而不响应ListView item的点击事件
 * 并提供接口可以供外部以内置浏览器的方式打开超链接(如果整个app点击超链接的操作都一致的话,可以考虑实现一个子类,并实现接口,在构造函数中设置接口)
 * @版本 V 2.0
 * @日期 2016-12-27
 * @描述: 修正长按超链接不响应控件长按事件
 * @######备注 如果要让控件能够支持listview的itemclick等事件,则{@link #setAttachToAdapterView}
 */

public class LinkFixedTextView extends AppCompatTextView {

    private OnLinkFixedTextViewListener listener;

    //解决问题-重写touchevent之后,长按超链接并不会响应控件的长按时间,因此在up和down的时候,校验是否主动触发longclick事件
    OnLongClickListener mLongClickListenerCache = null;
    CheckForLongPress mPendingCheckForLongPress;//长按延迟执行
    private boolean mHasPerformedLongPress = false;//是否已触发长按操作

    //
    OnClickListener mClickListenerCache = null;

    /**
     * 是否按住了控件
     * 解决:
     * 在listview中 长按文本的超链接,{@link #isPressed()}返回false,导致CheckForLongPress没有触发长按处理
     */
    private boolean mIsTouching = false;

    /**
     * 是否是adapterview的item
     */
    private boolean mIsAttachToAdapterView = false;

    public LinkFixedTextView(Context context) {
        super(context);
    }

    public LinkFixedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinkFixedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mClickListenerCache = l;
        super.setOnClickListener(l);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListenerCache = l;
        super.setOnLongClickListener(l);
    }

    /**
     * 在adapterview中使用这个控件,会与adapterview的itemclick等事件冲突.如果要让adapterview的事件生效,则需要调用此方法,
     * 否则可以在控件的click等相应事件中实现类似adapterview的事件效果
     */
    public void setAttachToAdapterView() {
        mIsAttachToAdapterView = true;
        this.setFocusable(false);
        this.setClickable(false);
        this.setLongClickable(false);
    }

    /**
     * 是否可以响应点击
     *
     * @return
     */
    private boolean isEnableClickPerform() {
        return mIsAttachToAdapterView ? mClickListenerCache != null : this.isClickable() && mClickListenerCache != null;
    }

    /**
     * 是否可以响应长按
     *
     * @return
     */
    private boolean isEnableLongClickPerform() {
        return mIsAttachToAdapterView ? mLongClickListenerCache != null : this.isLongClickable() && mLongClickListenerCache != null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
//            boolean performAction = isPerformAction();
//            if (!performAction) {
//                return super.onTouchEvent(event);
//            }
            int action = event.getAction();

            if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP){
                if(action == MotionEvent.ACTION_CANCEL){
                    setPressed(false);
                    //移除长按
                    removeLongPressCallback();
                    return true;
                }else{
                    return super.onTouchEvent(event);
                }
            }

            //获取当前文本
            CharSequence text = this.getText();
            Spannable spanText = Spannable.Factory.getInstance().newSpannable(text);

            //获取当前手指坐标
            int x = (int) event.getX();
            int y = (int) event.getY();

            //计算偏移量
            x -= this.getTotalPaddingLeft();
            y -= this.getTotalPaddingTop();

            x += this.getScrollX();
            y += this.getScrollY();

            Layout layout = this.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            //获取包含的link
            ClickableSpan[] links = spanText.getSpans(off, off, ClickableSpan.class);

            if (action == MotionEvent.ACTION_DOWN) {
                //如果控件要响应点击或长按或按下的位置包含超链接 则返回true,表示要处理事件
                boolean downLink = links != null && links.length > 0;
                if (isEnableClickPerform() || isEnableLongClickPerform() || downLink) {
                    checkPerformLongClick();
                    mIsTouching = true;
                    setPressed(true);
                    if (downLink) {
                        int start = spanText.getSpanStart(links[0]);
                        int end = spanText.getSpanEnd(links[0]);
                        Selection.setSelection(spanText, start, end);
                    }
                    return true;
                } else {
                    return false;
                }
            } else if (action == MotionEvent.ACTION_UP) {
                mIsTouching = false;
                setPressed(false);
                if (mHasPerformedLongPress) {//如果已经触发了长按操作,则不再处理
                    //预防触发系统的长按操作
                    cancelLongPress();
                } else {
                    //移除长按
                    removeLongPressCallback();

                    //如果点击区域不包含链接
                    if (links == null || links.length == 0) {
                        //没有触发长按操作,点击区域也不是超链接,则处理点击操作
                        if (isEnableClickPerform()) {
                            mClickListenerCache.onClick(this);
                        }
                    } else {
                        //触发链接点击回调
                        if (links[0] instanceof URLSpan) {
                            URLSpan span = (URLSpan) links[0];
                            this.listener.onLinkFixedURLSpanClick(this, span);
                        } else {
                            (links[0]).onClick(this);
                        }
                        Selection.removeSelection(spanText);
                    }
                }
                return true;
            }
        } catch (Exception ex) {
            return super.onTouchEvent(event);
        }

        return super.onTouchEvent(event);
    }

    protected void checkPerformLongClick() {
        mHasPerformedLongPress = false;
        if (this.isEnableLongClickPerform()) {// 如果当前支持长按
            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress();
            }
            //-20毫秒,让手动触发的长按事件先于系统触发
            postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout() - 20);
        }
    }

    /**
     * 移除长按回调
     */
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }


    private final class CheckForLongPress implements Runnable {
        @Override
        public void run() {
            if (mIsTouching) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }
    }

    public void setOnLinkFixedTextViewListener(
            OnLinkFixedTextViewListener listener) {
        this.listener = listener;
    }

    public interface OnLinkFixedTextViewListener {
        /**
         * 点击了文本中的超链接
         *
         * @param sender
         * @param clickedSpan 点击的URLSpan对象
         */
        void onLinkFixedURLSpanClick(LinkFixedTextView sender,
                                     URLSpan clickedSpan);
    }

}
