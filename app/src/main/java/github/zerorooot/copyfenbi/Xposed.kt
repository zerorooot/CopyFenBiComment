package github.zerorooot.copyfenbi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File


class Xposed : IXposedHookLoadPackage {
    private val replaceWord: Array<String> by lazy {
        arrayOf(
            "老师讲的真好",
            "清晰明了",
            "很棒棒",
            "满分！",
            "\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D",
            "懂了",
            "拓展的很好",
            "老师讲的挺好 咬字清晰 语速适当 爱了 爱了",
            "下次还错-.-",
            "吃了没文化的亏",
            "基础不牢，地动山摇",
            "不拖泥带水 很好",
            "赞！",
            "讲的很清晰明了，可以加鸡腿了",
            "满分",
            "负责的老师",
            "老喜欢这个老师了！讲的真好",
            "讲解得非常全面",
            "讲的很好，学到了一些不知道的知识",
            "老师讲的很好！！",
            "我对了,我蒙的",
            "每次最痛苦的就是二选一，最最痛苦的就是每次二选一都选错ᇂ_ᇂ",
            "真好，拓展了很多知识",
            "眼瞎的集合",
            "不错！全讲到了！棒棒棒！",
            "好"
        )
    }

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

                        if (check("dz")) {
                            filtration(classLoader)
                        }

                        if (check("sp")) {
                            hideVideo(classLoader)
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
                    val comment = XposedHelpers.callMethod(param.args[0], "getComment") as String
                    if (comment.contains("上岸") || comment.contains("点赞")) {
                        val replace = replaceWord.random()
                        XposedBridge.log("CopyFenbi $comment -> $replace")
                        XposedHelpers.setObjectField(param.args[0], "comment", replace)
                    }
                }
            })
    }

    private fun hideVideo(classLoader: ClassLoader) {
        val episode =
            XposedHelpers.findClass("com.fenbi.android.business.ke.data.Episode", classLoader)
        val userMemberState = XposedHelpers.findClass(
            "com.fenbi.android.business.vip.data.UserMemberState",
            classLoader
        )
        XposedHelpers.findAndHookMethod("com.fenbi.android.question.common.render.MemberVideoRender",
            classLoader,
            "Q",
            String::class.java,
            episode,
            userMemberState,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val linearLayout = (XposedHelpers.getObjectField(
                        param.thisObject,
                        "i"
                    ) as LinearLayout)

                    //R$id.member_video_wrapper
                    linearLayout.findViewById<View>(2131365094).visibility = View.GONE

                }
            })
    }

    private fun check(path: String): Boolean {
        val config =
            Environment.getExternalStorageDirectory().toString() + "/Android/" + path
        return File(config).exists()
    }
}