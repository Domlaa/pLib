package com.smart.ppx.okhttp;


import com.smart.ppx.bean.Result;

public interface IResponseListener {
    /**
     * 仅代表http请求成功，
     * 服务器是否完成操作需要具体解析
     * @param data 返回数据（包含返回的状态码）
     */
    void onSuccessful(Result data);

    /**
     * http请求失败
     * @param errorCode 请求码
     * @param errorMsg 错误信息
     */
    void onFailure(int errorCode, String errorMsg);

    /**
     * 网络请求结束
     */
    void finished();


}
