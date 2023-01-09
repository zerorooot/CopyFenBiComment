package github.zerorooot.copyfenbi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File


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
                        val classLoader = (param.args[0] as Context).classLoader
                        copy(classLoader)
                        val config =
                            Environment.getExternalStorageDirectory().toString() + "/Android/dz"
                        if (File(config).exists()) {
                            filtration(classLoader)
                        }

                    }
                })
        }

    }

    private fun copy(classLoader: ClassLoader) {
        val episodeCommentClass =
            XposedHelpers.findClass("com.fenbi.android.ke.data.EpisodeComment", classLoader)
        XposedHelpers.findAndHookMethod("dc4",
            classLoader,
            "j",
            View::class.java,
            episodeCommentClass,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val context = (param.args[0] as View).context
                    val nickName = XposedHelpers.callMethod(param.args[1], "getNickName") as String
                    val comment = XposedHelpers.callMethod(param.args[1], "getComment") as String

                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("copy fen bi", comment)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(context, "用户 $nickName 的评论已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    param.result = false
                }
            })
    }

    private fun filtration(classLoader: ClassLoader) {
        val episodeCommentClass =
            XposedHelpers.findClass("com.fenbi.android.ke.data.EpisodeComment", classLoader)

        XposedHelpers.findAndHookMethod(
            "dc4",
            classLoader,
            "i",
            episodeCommentClass,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val replaceWord = arrayOf(
                        "老师讲的真好",
                        "清晰明了",
                        "很棒棒",
                        "满分！",
                        "\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D",
                        "打破零回复",
                        "懂了",
                        "拓展的很好",
                        "老师讲的挺好 咬字清晰 语速适当 爱了 爱了",
                        "下次还错-.-",
                        "吃了没文化的亏",
                        "基础不牢，地动山摇",
                        "不拖泥带水 很好"
                    )
                    val comment = XposedHelpers.callMethod(param.args[0], "getComment") as String
                    val replace= replaceWord.random()
                    if (comment.contains("上岸") || comment.contains("点赞")) {
                        XposedBridge.log("CopyFenbi $comment -> $replace")
                        XposedHelpers.setObjectField(param.args[0], "comment", replace)
                    }
                }
            })
    }
}