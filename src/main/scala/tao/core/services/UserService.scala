/*
 * Copyright 2011 The Yishishun IT Department.
 * site: http://www.taobao.pk
 */
package tao.core.services

import com.taobao.api.domain.User
import com.taobao.api.request.{VasSubscribeGetRequest, ShopGetRequest, UserGetRequest}
import tao.core.config.TaobaoAppConfig
import tao.core.TaoCoreConstants
import com.mongodb.casbah.commons.MongoDBObject
import actors.Actor
import org.slf4j.LoggerFactory
import com.mongodb.casbah.Imports._
import collection.JavaConversions._
import org.apache.tapestry5.services.Cookies
import org.springframework.util.StringUtils
import java.net.{URLDecoder, URLEncoder}


/**
 * User Service for taobao sellers and buyers
 * @author jcai
 * @version 0.1
 */
class UserService(config:TaobaoAppConfig,
                  mongoTemplate:MongoTemplate,
                  client:TaobaoApiClient,
                  cookies:Cookies) {
    //logger
    private val logger = LoggerFactory getLogger getClass
    //create unique index on NICK field
    mongoTemplate.executeInColl(TaoCoreConstants.COLL_USER){coll=>
        coll.ensureIndex(MongoDBObject(TaoCoreConstants.FIELD_NICK->1),"nick",true)
    }

    /**
     * if free user
     * @param nick user nick
     */
    def isFreeUser(nick:String):Boolean={
        val user = findUser(nick).getOrElse(null)
        if(user != null){
            val version = user.get(TaoCoreConstants.FIELD_VERSION)
            return version == null || String.valueOf(version) == config.freeVersion
        }else{
            return true;
        }
    }

    def getOnlineUser:DBObject={
        val nickCookie=cookies.readCookieValue(TaoCoreConstants.NICK_COOKIE_NAME_FORMAT.format(config.appKey))
        if (StringUtils.hasText(nickCookie)){
            val nick=URLDecoder.decode(nickCookie,"UTF-8")
            return findUser(nick).getOrElse(null)
        }
        return null
    }
    /**
     * find user by nick name
     * @param nick nick name
     * @return DBObject
     */
    def findUser(nick:String)=mongoTemplate.findOne(
        TaoCoreConstants.COLL_USER,MongoDBObject(TaoCoreConstants.FIELD_NICK->nick))

    /**
     * verify user
     * @param nick nick name
     */
    def verifyValidateUser:DBObject={
        val user=getOnlineUser
        if(user != null){
            val version = user.get(TaoCoreConstants.FIELD_VERSION)
            if(version == null || String.valueOf(version) == config.freeVersion){
                val countUse = user.get(TaoCoreConstants.FIELD_COUNT_USE)
                if(countUse != null){
                    if(countUse.asInstanceOf[Int]>10){
                        throw new RuntimeException("你试用已经超过十次，请订购正式无限制版本");
                    }
                }
                //inc count_use field
                mongoTemplate.executeInColl(TaoCoreConstants.COLL_USER){coll=>
                    coll.update(
                        MongoDBObject(TaoCoreConstants.FIELD_NICK->user.get(TaoCoreConstants.FIELD_NICK)),
                        $inc(TaoCoreConstants.FIELD_COUNT_USE->1)
                    )
                }
            }
            return user;
        }else{
            throw new RuntimeException("非法请求，未能找到用户");
        }
    }
    //save or update user by nick and dbObj
    private def saveOrUpdateUser(nick:String,dbObj:DBObject){
        mongoTemplate.saveOrUpdate(
            TaoCoreConstants.COLL_USER,
            MongoDBObject(TaoCoreConstants.FIELD_NICK-> nick),
            dbObj)
    }
    /**
     * init user by session_key and nick
     * @param session taobao session key
     * @param nick taobao nick name
     * @param actor if use Actor to execute
     */
    def initUser(session:String,nick:String,actor:Boolean=true){
        saveOrUpdateUser(nick, MongoDBObject(
            TaoCoreConstants.FIELD_NICK->nick,
            TaoCoreConstants.FIELD_SESSION->session))
        //write cookie
        cookies.writeCookieValue(TaoCoreConstants.NICK_COOKIE_NAME_FORMAT.format(config.appKey),URLEncoder.encode(nick,"UTF-8"))
        val body= ()=>{
            //get user information
            logger.debug("init user")
            val userRequest = new UserGetRequest
            userRequest.setFields("user_id,has_shop")
            userRequest.setNick(nick)
            val userResponse = client.execute(userRequest,session)
            var user:User = null
            if(userResponse.isSuccess){
                user = userResponse.getUser
                if(user == null){
                    throw new RuntimeException("未能找到用户");
                }
            }else{
                throw new RuntimeException(userResponse.getMsg);
            }
            //get shopId
            var shopId = 0L
            if(user.getHasShop){
                val request = new ShopGetRequest
                request.setFields("sid")
                request.setNick(nick)
                val response=client.execute(request,session)
                logger.debug("body:{}",response.getBody)
                if(response.isSuccess){
                    val shop=response.getShop
                    if(shop != null){
                        shopId = shop.getSid
                    }
                }else{
                    logger.error("fail to fetch,code:{},msg:{}",response.getErrorCode,response.getMsg)
                }
            }
            //get version
            var version= config.freeVersion
            val vasRequest = new VasSubscribeGetRequest
            vasRequest.setNick(nick)
            vasRequest.setArticleCode(config.feeCode)
            val vasResponse = client.execute(vasRequest)
            if(vasResponse.isSuccess){
                logger.debug(vasResponse.getBody)
                if(vasResponse.getArticleUserSubscribes != null){
                    vasResponse.getArticleUserSubscribes.foreach(aus=>{
                        version = aus.getItemCode
                    })
                }
            }
            logger.debug("user version:{}",version)
            saveOrUpdateUser(nick,
                MongoDBObject(TaoCoreConstants.FIELD_SHOP_ID->shopId,
                    TaoCoreConstants.FIELD_VERSION->version))
        }

        //execute
        if(actor){
            Actor.actor{body()}
        }else{
            body()
        }
    }
}