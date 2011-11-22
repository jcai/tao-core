/*
 * Copyright 2011 The Yishishun IT Department.
 * site: http://www.taobao.pk
 */
package tao.core.services

import tao.core.config.TaobaoAppConfig
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.conversions.scala.{RegisterJodaTimeConversionHelpers, DeregisterJodaTimeConversionHelpers}

/**
 * mongo template
 * @author jcai
 * @version 0.1
 */
class MongoTemplate(config:TaobaoAppConfig) {
    DeregisterJodaTimeConversionHelpers();
    RegisterJodaTimeConversionHelpers();

    val db= MongoConnection(config.mongoServer)(config.mongoDb)

    /**
     * 通过查询来更新
     */
    def saveOrUpdate(coll:String,queryObj:DBObject,dbObject:DBObject){
        db(coll).update(queryObj,MongoDBObject("$set"->dbObject),true,false)
    }
    def delete(coll:String,queryObj:MongoDBObject){
        db(coll) -= queryObj
    }
    def executeInColl(coll:String)(fun:MongoCollection=>Unit){
        fun(db(coll))
    }
    def find[A <% DBObject](coll:String,ref: A) = db(coll).find(ref)
    def findOne[A <% DBObject](coll:String,ref: A) = db(coll).findOne(ref)
}