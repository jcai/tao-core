/*
 * Copyright 2011 The Yishishun IT Soft Department.
 * site: http://www.taobao.pk
 */
package tao.core.services

import tao.core.config.TaobaoAppConfig
import com.taobao.api.{TaobaoRequest, TaobaoResponse, Constants, DefaultTaobaoClient}

/**
 * taobao api client
 * @author jcai
 * @version 0.1
 */

class TaobaoApiClient(config:TaobaoAppConfig){
     private val client = new DefaultTaobaoClient(
        config.apiUrl,
        config.appKey,
        config.appSecret,
        Constants.FORMAT_JSON,
        60000,60000)

    def execute[T <: TaobaoResponse](request:TaobaoRequest[T])=client.execute(request)
    def execute[T <: TaobaoResponse](request:TaobaoRequest[T],session:String)=client.execute(request,session)
}