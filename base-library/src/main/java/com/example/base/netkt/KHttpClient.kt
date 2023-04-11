package com.example.base.netkt

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class KHttpClient(private val address: String) : KIClient {
    //请求参数
    private var params: MutableList<String>? = null
    private var connection: HttpURLConnection? = null

    override fun get(): String? {
        val url = address + "?" + getParams()
        connection = build(GET, url)
        println("-------->>>> get, url --->>> $url")
        val result = doGet()
        println("result : $result")
        return result
    }

    override fun post(): String? {
        connection = build(POST, address)
        println("-------->>>> post, url --->>> $address")
        val result = doPost()
        println("result : $result")
        return result
    }

    private fun doPost(): String? {
        val params = getParams()
        val map = connection!!.requestProperties
        for (key in map.keys) {
            println(key + "->" + map[key])
        }
        try {
            if (params != null) {
                println("params!=null-> $params")
                connection!!.doOutput = true
                val out = DataOutputStream(connection!!.outputStream)
                out.write(params.toByteArray(Charset.forName("UTF-8")))
                out.flush()
                out.close()
            }
            connection!!.connect()
            return if (connection!!.responseCode == HttpURLConnection.HTTP_OK) {
                parsRtn(connection!!.inputStream)
            } else {
                throw Exception(connection!!.responseCode.toString() + " " + connection!!.responseMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun doGet(): String? {
        try {
            connection!!.connect()
            return if (connection!!.responseCode == HttpURLConnection.HTTP_OK) {
                parsRtn(connection!!.inputStream)
            } else {
                throw Exception(connection!!.responseCode.toString() + " " + connection!!.responseMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun addHeader(key: String, value: String) {
        connection!!.addRequestProperty(key, value)
    }

    override fun setHeader(key: String, value: String) {
        connection!!.setRequestProperty(key, value)
    }

    override fun addParam(key: String, value: String) {
        if (params == null) {
            params = ArrayList()
        }
        if (key.isNotEmpty()) {
            params!!.add("$key=$value")
        }
    }

    override fun release() {
        connection?.disconnect()
    }


    private fun build(method: String, address: String): HttpURLConnection? {
        val conn: HttpURLConnection
        try {
            val url = URL(address)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = CONN_TIMEOUT
            conn.readTimeout = READ_TIMEOUT
            conn.requestMethod = method
            return conn
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getParams(): String? {
        if (params == null) {
            return null
        }
        val buffer = StringBuilder()
        var first = true
        for (kv in params!!) {
            if (first) {
                first = false
            } else {
                buffer.append("&")
            }
            buffer.append(kv)
        }
        return buffer.toString()
    }

    @Throws(IOException::class)
    private fun parsRtn(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream, CHARSET))
        val buffer = StringBuilder()
        var line: String?
        var first = true
        while (reader.readLine().also { line = it } != null) {
            if (first) {
                first = false
            } else {
                buffer.append("\n")
            }
            buffer.append(line)
        }
        return buffer.toString()
    }

    companion object {
        private const val CHARSET = "GB2312"

        private const val CONN_TIMEOUT = 60_000
        private const val READ_TIMEOUT = 60_000

        private const val GET = "GET"
        private const val POST = "POST"
    }
}