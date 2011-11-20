/*
 * Copyright 2011 The Yishishun Investment Management Co.,Ltd.
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
}
