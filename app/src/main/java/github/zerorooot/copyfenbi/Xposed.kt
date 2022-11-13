package github.zerorooot.copyfenbi

import android.R.attr.classLoader
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam


class Xposed : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName.equals("com.fenbi.android.servant")) {
            XposedHelpers.findAndHookMethod("com.netease.nis.wrapper.MyApplication",
                lpparam.classLoader,
                "a",
                Context::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        fenbi((param.args[0] as Context).classLoader)
                    }
                })
        }

    }

    private fun fenbi(classLoader: ClassLoader) {
        val episodeCommentClass =
            XposedHelpers.findClass("com.fenbi.android.ke.data.EpisodeComment", classLoader)
        val ky = XposedHelpers.findClass("ky0\$a", classLoader)
        XposedHelpers.findAndHookMethod("dc4",
            classLoader,
            "r",
            episodeCommentClass,
            View::class.java,
            Int::class.javaPrimitiveType,
            ky,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val context = (param.args[1] as View).context
                    val nickName = XposedHelpers.callMethod(param.args[0], "getNickName") as String
                    val comment = XposedHelpers.callMethod(param.args[0], "getComment") as String

                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("copy fen bi", comment)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(context, "用户 $nickName 的评论已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    param.result = false
                }
            })
    }

}