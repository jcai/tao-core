/*
 * Copyright 2011 The Yishishun IT Department.
 * site: http://www.taobao.pk
 */
package tao.core.config

import reflect.BeanProperty

/**
 * taobao api configuration
 * @author jcai
 * @version 0.1
 */
class TaobaoAppConfig {
    @BeanProperty
    var appName:String = _
    @BeanProperty
    var appKey:String=_
    @BeanProperty
    var appSecret:String=_
    @BeanProperty
    var apiUrl:String = _
    @BeanProperty
    var containerUrl:String = _
    @BeanProperty
    var feeCode:String = _
    @BeanProperty
    var freeVersion:String = _
    @BeanProperty
    var mongoServer:String = "localhost:27017"
    @BeanProperty
    var mongoDb:String = "taobao"
}
