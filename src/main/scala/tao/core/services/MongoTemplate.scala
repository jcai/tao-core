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

    /**
     * delete record using query object
     */
    def delete(coll:String,queryObj:MongoDBObject){
        db(coll) -= queryObj
    }

    /**
     * execute command with a collection
     */
    def executeInColl[T](coll:String)(fun:MongoCollection=>T)=fun(db(coll))

    /**
     * execute command with a db object
     */
    def executeInDB[T](fun:MongoDB=>T)=fun(db)

    /**
     * find records with collection
     */
    def find[A <% DBObject](coll:String,ref: A) = db(coll).find(ref)

    /**
     * Returns a single object from this collection matching the query.
     * @param o the query object
     * @return (Option[T]) Some() of the object found, or <code>None</code> if no such object exists
     */
    def findOne[A <% DBObject](coll:String,ref: A) = db(coll).findOne(ref)
}