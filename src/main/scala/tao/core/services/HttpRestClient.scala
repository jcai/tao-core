/*
 * Copyright 2011 The Yishishun IT Soft Department.
 * site: http://www.taobao.pk
 */
package tao.core.services

import org.springframework.util.FileCopyUtils
import org.slf4j.LoggerFactory
import org.apache.commons.httpclient.methods.{GetMethod, PostMethod}
import java.io.InputStream
import collection.mutable.ArrayBuffer
import org.apache.commons.httpclient.{NameValuePair, HttpStatus, HttpClient}

/**
 *
 * http reset client,used as api client
 */
object HttpRestClient {
	private val logger= LoggerFactory.getLogger(getClass)
    def get(url:String,params:Map[String,String]=null,headers:Map[String,String]=null):String={
        var method:GetMethod  = null;
        try{
            val client = new HttpClient();
            client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
            client.getParams.setConnectionManagerTimeout(1000*60*1);
            client.getParams.setSoTimeout(1000*60*2);

            method = new GetMethod(url);
            if(params !=null){
                val ps=params.foldRight(ArrayBuffer[NameValuePair]()){(x,arr) =>
                    arr+=new NameValuePair(x._1,x._2)
                }
                method.setQueryString(ps.toArray)
            }
            if (headers != null){
                headers.foreach(x=>{
                    method.setRequestHeader(x._1,x._2)
                })
            }
            val response = client.executeMethod(method);
            if (response == HttpStatus.SC_OK) {
                val stream = method.getResponseBodyAsStream
                val ba = FileCopyUtils.copyToByteArray(stream);
                return new String(ba,"UTF-8")
            } else {
                throw new RuntimeException("server返回错误状态：" + method.getStatusLine());
            }
        }finally{
            if(method != null)
                method.releaseConnection();
        }
    }
    def getInputStream[T](url:String)(fun:InputStream=>T):T={
        var method:GetMethod  = null;
        try{
            val client = new HttpClient();
            client.getParams.setConnectionManagerTimeout(1000*60*1);
            client.getParams.setSoTimeout(1000*60*2);
            client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
            method = new GetMethod(url);
            val response = client.executeMethod(method);
            if (response == HttpStatus.SC_OK) {
                return fun(method.getResponseBodyAsStream)
            } else {
                throw new RuntimeException("server返回错误状态：" + method.getStatusLine());
            }
        }catch{
            case e=>
                logger.error("fail to process:"+url,e)
                throw e;
        }finally{
            if(method != null)
                method.releaseConnection();
        }
    }
	/**
     * post url
     * @param url http url
     * @param parameters http parameters
     * @return jsonobject
     */
    def post(url:String,parameters:Map[String,String]=Map[String,String]()):String={
        var method:PostMethod  = null;
        try{
            val client = new HttpClient();
            client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
            client.getParams.setConnectionManagerTimeout(1000*60*1);
            client.getParams.setSoTimeout(1000*60*2);
            method = new PostMethod(url);
            parameters.foreach(x=> method.setParameter(x._1,x._2))
            val response = client.executeMethod(method);
            if (response == HttpStatus.SC_OK) {
                val stream = method.getResponseBodyAsStream
                val ba = FileCopyUtils.copyToByteArray(stream);
                return new String(ba,"UTF-8")
            } else {
                throw new RuntimeException("server返回错误状态：" + method.getStatusLine());
            }
        }finally{
            if(method != null)
                method.releaseConnection();
        }
    }
}