package hxzcmall.com.amfc.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author: captain
 * Time:  2017/11/9 0009
 * Describe:
 */
public class ToastUtils {

    public static Toast toast;

    private ToastUtils() {
         /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 短时间显示Toas
     *
     * @param context
     * @param content
     */
    public static void showShort(Context context, CharSequence content) {
        /*这里做Toast的非空判断是防止多次点击，Toast的多次显示*/
        if (toast == null) {
            toast= Toast.makeText(context, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param content
     */
    public static void showShort(Context context, int content) {
        if (toast == null) {
            toast= Toast.makeText(context, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param content
     */
    public static void showLong(Context context, CharSequence content) {
        if (toast == null) {
            toast= Toast.makeText(context, content, Toast.LENGTH_LONG);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    /**
     * 上时间显示Toast
     *
     * @param context
     * @param content
     */
    public static void showLong(Context context, int content) {
        if (toast == null) {
            toast= Toast.makeText(context, content, Toast.LENGTH_LONG);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    /**
     * 自定义时长显示Toast
     *
     * @param context
     * @param content
     * @param duration
     */
    public static void showDuration(Context context, CharSequence content, int duration) {
        if (toast == null) {
            toast= Toast.makeText(context, content, duration);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    /**
     * 自定义时长显示Toast
     *
     * @param context
     * @param content
     * @param duration
     */
    public static void showDuration(Context context, int content, int duration) {
        if (toast == null) {
            toast= Toast.makeText(context, content, duration);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}
