package github.zerorooot.copyfenbi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File


class Xposed : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (!lpparam.packageName.equals("com.fenbi.android.servant")) {
            return
        }
        val episodeCommentClass =
            XposedHelpers.findClass("com.fenbi.android.ke.data.EpisodeComment", lpparam.classLoader)


        val hook = object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
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
        }

        when (getPackageVersion(lpparam)) {
            //6.16.39.32
            6163932 -> {
                XposedHelpers.findAndHookMethod(
                    "rv3", lpparam.classLoader, "f",
                    View::class.java,
                    episodeCommentClass, hook
                )
            }
            //6.16.37
            6163700 -> {
                XposedHelpers.findAndHookMethod(
                    "it3", lpparam.classLoader, "f",
                    View::class.java,
                    episodeCommentClass, hook
                )
            }
            else -> {
                XposedBridge.log("CopyFenBiComment：不支持此版本")
            }
        }

    }

    private fun getPackageVersion(lpparam: LoadPackageParam): Int {
        val apkPath = File(lpparam.appInfo.sourceDir)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pkgParserClass =
                XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader)
            val packageLite =
                XposedHelpers.callStaticMethod(pkgParserClass, "parsePackageLite", apkPath, 0)
            XposedHelpers.getIntField(packageLite, "versionCode")
        } else {
            val parserCls =
                XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader)
            val pkg = XposedHelpers.callMethod(parserCls.newInstance(), "parsePackage", apkPath, 0)
            XposedHelpers.getIntField(pkg, "mVersionCode")
        }
    }
}