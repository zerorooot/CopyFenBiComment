package github.zerorooot.copyfenbi

import android.R.attr.classLoader
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam


class Xposed : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (!lpparam.packageName.equals("com.fenbi.android.servant")) {
            return
        }
        val findClass =
            XposedHelpers.findClass("com.fenbi.android.ke.data.EpisodeComment", lpparam.classLoader)

        XposedHelpers.findAndHookMethod("it3", lpparam.classLoader, "f",
            View::class.java,
            findClass, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    super.beforeHookedMethod(param)
                    val context = (param.args[0] as View).context
                    val nickName = XposedHelpers.callMethod(param.args[1], "getNickName") as String
                    val comment = XposedHelpers.callMethod(param.args[1], "getComment") as String

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("copy fen bi", comment)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(context, "用户 $nickName 的评论已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    param.result = false


                }

//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    super.afterHookedMethod(param)
//                }
            })
    }

}