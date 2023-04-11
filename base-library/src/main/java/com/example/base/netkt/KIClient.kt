package com.example.base.netkt

interface KIClient {
    fun get(): String?
    fun post(): String?

    fun addHeader(key: String, value: String)
    fun setHeader(key: String, value: String)
    fun addParam(key: String, value: String)
    fun release()
}