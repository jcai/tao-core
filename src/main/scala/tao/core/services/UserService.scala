/*
 * Copyright 2011 The Yishishun IT Soft Department.
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


/**
 *
 * @author jcai
 * @version 0.1
 */
class UserService(config:TaobaoAppConfig,mongoTemplate:MongoTemplate,client:TaobaoApiClient) {
    private val logger = LoggerFactory getLogger getClass
    mongoTemplate.executeInColl(TaoCoreConstants.COLL_USER){coll=>
        coll.ensureIndex(MongoDBObject(TaoCoreConstants.FIELD_NICK->1),"nick",true)
    }
    private def saveOrUpdateUser(nick:String,dbObj:DBObject){
        mongoTemplate.saveOrUpdate(
            TaoCoreConstants.COLL_USER,
            MongoDBObject(TaoCoreConstants.FIELD_NICK-> nick),
            dbObj)
    }
    /**
     * 初始化用户
     */
    def initUser(session:String,nick:String){
        saveOrUpdateUser(nick, MongoDBObject(
            TaoCoreConstants.FIELD_NICK->nick,
            TaoCoreConstants.FIELD_SESSION->session))
        Actor.actor{
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
            //获取订购关系
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
                logger.debug(vasResponse.getArticleUserSubscribes.mkString(","));
            }
            logger.debug("user version:{}",version)
            saveOrUpdateUser(nick,
                MongoDBObject(TaoCoreConstants.FIELD_SHOP_ID->shopId,
                    TaoCoreConstants.FIELD_VERSION->version))
        }
    }
}