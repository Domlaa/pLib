package com.smart.ppx.helper

import android.content.Context
import android.util.Log
import dalvik.system.PathClassLoader
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException

object UtilKt {

    private const val PACK_NAME = "com.xning.xposedtest"
    private const val PACK_NAME2 = "cn.lianqinba.strings"

    /**
     * 根据包名构建目标Context,并调用getPackageCodePath()来定位apk
     * @param context context参数
     * @param modulePackageName 当前模块包名
     * @return return apk file
     */
    fun findApkFile(context: Context): File? {
        Log.i("TAG", "--->>> findApkFile, packageName = ${context.packageName}")
        try {
            val moduleContext = context.createPackageContext(
                //"com.smart.ppx",
                PACK_NAME,
                Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
            )
            val apkPath = moduleContext.packageCodePath
            return File(apkPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun loadClass(context: Context) {

        val apkFile = findApkFile(context)
            ?: throw RuntimeException("寻找模块apk失败")

        //加载指定的hook逻辑处理类，并调用它的handleHook方法
        Log.d("--->>", "--->>> package = ${context.packageName}, path = ${apkFile.absolutePath}")
        val pathClassLoader = PathClassLoader(apkFile.absolutePath, ClassLoader.getSystemClassLoader())
        Log.d("--->>", "--->>> pathClassLoader = ${pathClassLoader}")

        val className = "MainActivity"
        val className2 = "PPXHooker"
        val className3 = "HookPPX"

        try {
            val cls = Class.forName("${PACK_NAME}.$className", true, pathClassLoader)
            val instance = cls.newInstance()
            Log.d(className, "--->>> instance = $instance")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val cls = Class.forName("${PACK_NAME}.$className2", true, pathClassLoader)
            val instance = cls.newInstance()
            Log.d(className2, "--->>> instance = $instance")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val cls = Class.forName("${PACK_NAME}.$className3", true, pathClassLoader)
            val instance = cls.newInstance()
            Log.d(className3, "--->>> instance = $instance")
        } catch (e: Exception) {
            e.printStackTrace()
        }



    }

    // 查找apk路径
    fun getApplicationApkPath(context: Context): String {
        try {
            val packageName = PACK_NAME2
            val pm = context.packageManager
            val apkPath = pm.getApplicationInfo(packageName, 0).publicSourceDir
            return apkPath ?: throw Error("Failed to get the APK path of $packageName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "null"
    }


}