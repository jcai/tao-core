/*
 * Copyright 2011 The Yishishun Investment Management Co.,Ltd.
 * site: http://www.taobao.pk
 */
package tao.core

import org.apache.tapestry5.internal.InternalConstants
import org.apache.tapestry5.services.{LibraryMapping, ComponentClassResolver}
import org.apache.tapestry5.ioc.annotations.{Contribute,Symbol}
import org.apache.tapestry5.ioc.{Configuration, MappedConfiguration}

/**
 * tao core module
 * @author jcai
 * @version 0.1
 */
object TaoCoreModule {
    //contribute factory defaults
    def contributeFactoryDefaults(configuration: MappedConfiguration[String, String]) {
        configuration.add(TaoCoreSymbols.CONFIG_DIR_CFG, "support/config")
        configuration.add(TaoCoreSymbols.LOG_DIR, ".")
    }

    @Contribute(classOf[ComponentClassResolver])
    def setupCoreAndAppLibraries(configuration: Configuration[LibraryMapping]): Unit = {
        configuration.add(new LibraryMapping("tao", "tao.core"))
    }
}