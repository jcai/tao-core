/*
 * Copyright 2011 The Yishishun IT Department.
 * site: http://www.taobao.pk
 */
package tao.core

import config.TaobaoAppConfig
import org.apache.tapestry5.internal.InternalConstants
import org.apache.tapestry5.services.{LibraryMapping, ComponentClassResolver}
import org.apache.tapestry5.ioc.annotations.{Contribute,Symbol}
import java.io.{File, FileInputStream}
import org.apache.tapestry5.ioc.{ServiceBinder, Configuration, MappedConfiguration}
import services.{MongoTemplate, TaobaoApiClient, UserService, YamlLoader}

/**
 * tao core module
 * @author jcai
 * @version 0.1
 */
object TaoCoreModule {
    def bind(binder:ServiceBinder){
        binder.bind(classOf[UserService])
        binder.bind(classOf[TaobaoApiClient])
        binder.bind(classOf[MongoTemplate])
    }
    //contribute factory defaults
    def contributeFactoryDefaults(configuration: MappedConfiguration[String, String]) {
        configuration.add(TaoCoreSymbols.CONFIG_DIR_CFG, "support/config")
        configuration.add(TaoCoreSymbols.LOG_DIR, ".")
    }
    def buildTaobaoAppConfig(@Symbol(TaoCoreSymbols.CONFIG_DIR_CFG) configDir:String):TaobaoAppConfig=
        YamlLoader.loadConfig[TaobaoAppConfig](new FileInputStream(new File(configDir+"/config.yml")))

    @Contribute(classOf[ComponentClassResolver])
    def setupCoreAndAppLibraries(configuration: Configuration[LibraryMapping]): Unit = {
        configuration.add(new LibraryMapping("tao", "tao.core"))
    }
}