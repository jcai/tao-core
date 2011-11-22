/*
 * Copyright 2011 The Yishishun IT Department.
 * site: http://www.taobao.pk
 */
package tao.core.pages.api

import org.apache.tapestry5.util.TextStreamResponse
import org.apache.tapestry5.annotations.ActivationRequestParameter
import java.security.MessageDigest
import com.taobao.api.internal.util.codec.Base64
import org.apache.tapestry5.ioc.annotations.Inject
import org.slf4j.Logger
import java.net.URLEncoder
import com.taobao.api.Constants
import sun.misc.BASE64Decoder
import org.springframework.util.StringUtils
import com.taobao.api.internal.util.{WebUtils, TaobaoUtils}
import org.apache.tapestry5.services.{Request, Cookies}
import tao.core.services.UserService
import tao.core.config.TaobaoAppConfig
import tao.core.TaoCoreConstants

/**
 * 供Taobao调用的API
 * @author jcai
 * @version 0.1
 */
class CallbackApi {
    @ActivationRequestParameter
    private var top_appkey:String = _
    @ActivationRequestParameter
    private var top_session:String = _
    @ActivationRequestParameter
    private var top_parameters:String = _
    @ActivationRequestParameter
    private var top_sign:String = _
    @ActivationRequestParameter
    private  var encode:String = _
    @Inject
    private var logger:Logger = _
    @Inject
    private var cookies:Cookies = _
    @Inject
    private var userService:UserService= _
    @Inject
    private var config:TaobaoAppConfig = _
    def onActivate:Object={
        try{
            if(!TaobaoUtils.verifyTopResponse(top_parameters,top_session,top_sign,config.appKey,config.appSecret)){
                return new TextStreamResponse("text/plain","错误请求!")
            }

            val parameters=decodeTopParams(top_parameters)
            val time=parameters.get("ts").toLong
            val currentTime = System.currentTimeMillis()
            if (currentTime - time > TaoCoreConstants.EXPIRED_INTERVAL){
                return new TextStreamResponse("text/plain","expired,pls login again")
            }
            logger.debug("sessionkey:{}",top_session)
            val nick= parameters.get("visitor_nick")
            if(nick == null){
                return new TextStreamResponse("text/plain","please login")
            }
            logger.info("user {} logging",nick)
            userService.initUser(top_session,nick)
            return "Start"
        }catch{
            case e=>
                return new TextStreamResponse("text/plain",e.getMessage)
        }
        /*
        val refreshToken=parameters.get("refresh_token")
        val refreshParameters = new TreeMap[String,String]()
        refreshParameters.put("appkey",TaokuaidiConstants.APP_KEY)
        refreshParameters.put("refresh_token",refreshToken)
        refreshParameters.put("sessionkey",top_session)

        val signStr = sign(refreshParameters.toMap)
        logger.debug("self sign:{}",signStr)
        refreshParameters.put("sign",signStr)
        val parametersEncoded=refreshParameters.toMap
        if(logger.isDebugEnabled){
            logger.debug("url:{}",parametersEncoded.foldLeft(""){(b,x)=>
                b+"&"+x._1+"="+x._2
            })
        }

        val r=HttpRestClient.get(TaokuaidiConstants.REFRESH_TOKEN_URL,parametersEncoded)
        */
        //new TextStreamResponse("text/plain","OK")
    }
    def sign(parameters:Map[String,String])={
        // 对参数+密钥做MD5运算
        val md = MessageDigest.getInstance("MD5");
        val parameterStr = parameters.foldLeft(""){(b,x)=>
            b+x._1+x._2
        }
        logger.debug("sign origin string:{}",parameterStr)
        val digest = md.digest((parameterStr + config.appSecret).getBytes());
        new String(Base64.encodeBase64(digest));
   }

    /**
     * 解释TOP回调参数为键值对。
     *
     * @param topParams 经过BASE64编码的字符串
     * @return 键值对
     * @throws IOException
     */
    def decodeTopParams(topParams: String): java.util.Map[String, String] = {
        if (!StringUtils.hasText(topParams)) {
            return null
        }
        val decoder = new BASE64Decoder();
        val buffer=decoder.decodeBuffer(topParams)

        val paramEncode=if(StringUtils.hasText(encode)) encode else Constants.CHARSET_GBK
        var originTopParams: String = new String(buffer, paramEncode)
        return WebUtils.splitUrlQuery(originTopParams)
    }
}