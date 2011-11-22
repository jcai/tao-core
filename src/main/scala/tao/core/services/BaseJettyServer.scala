/*
 * Copyright 2011 The Yishishun IT Soft Department.
 * site: http://www.taobao.pk
 */
package tao.core.services
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.thread.QueuedThreadPool
import org.mortbay.jetty.{ Server, Handler }
import org.apache.tapestry5.TapestryFilter
import org.mortbay.jetty.handler.ContextHandler
import org.mortbay.jetty.servlet.{ DefaultServlet, ServletHolder, ServletHandler, FilterHolder }
import java.util.{ Properties, HashMap }
import org.apache.log4j.PropertyConfigurator
import org.apache.tapestry5.internal.InternalConstants
import org.apache.tapestry5.ioc.Registry
import tao.core.TaoCoreSymbols

/**
 * common server class
 */
class BaseJettyServer {
    protected var registry: Registry = _
    protected def configLogger(name: String,prefix:String) {
        //init system properties
        if (System.getProperty(TaoCoreSymbols.LOG_DIR) == null) {
            System.setProperty(TaoCoreSymbols.LOG_DIR, ".")
        }
        //debug mode or enable log
        if (System.getProperty("enable-log") != "true") {
            val properties = new Properties();
            properties.put("log4j.rootCategory", "error,R");
            properties.put("log4j.appender.R", "org.apache.log4j.RollingFileAppender");
            properties.put("log4j.appender.R.layout", "org.apache.log4j.PatternLayout");
            properties.put("log4j.appender.R.layout.ConversionPattern", "[TaoKuaiDi] %d{MM-dd HH:mm:ss} %m%n");
            properties.put("log4j.appender.R.File", "${"+TaoCoreSymbols.LOG_DIR+"}/taobao." + name + ".log");
            properties.put("log4j.appender.R.MaxFileSize", "10000KB");
            properties.put("log4j.appender.R.MaxBackupIndex", "10");
            properties.put("log4j.category.org.apache.tapestry5", "error");
            properties.put("log4j.category.taokuaidi", "info");

            PropertyConfigurator.configure(properties);
        }
    }

    protected def createServer(port: Int, appPackage: String): (Server, ContextHandler) = {
        val contextHandler = new ContextHandler("/");
        val initParams = new HashMap[String, String]();
        initParams.put(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, appPackage);
        contextHandler.setInitParams(initParams);

        val handler = new ServletHandler

        //default servlet holder
        val servletHolder = new ServletHolder(classOf[DefaultServlet]);
        servletHolder.setName("default");
        handler.addServletWithMapping(servletHolder, "/");

        //filter holder

        val filterHolder = new FilterHolder(classOf[TapestryFilter]);
        filterHolder.setName("monad");
        handler.addFilterWithMapping(filterHolder, "/*", Handler.ALL);

        contextHandler.addHandler(handler);

        val server = new Server();
        //thread pool
        val tp = new QueuedThreadPool();
        tp.setMinThreads(10)
        tp.setMaxThreads(2000)
        tp.setLowThreads(20)
        tp.setSpawnOrShrinkAt(2)
        server.setThreadPool(tp)

        //connector
        val connector = new SelectChannelConnector
        connector.setPort(port)
        connector.setMaxIdleTime(30000)
        connector.setAcceptors(5)
        connector.setStatsOn(false)
        connector.setLowResourcesConnections(5000)
        //connector.setLowResourcesMaxIdleTime(5000)
        server.addConnector(connector)
        server.setHandler(contextHandler)
        registry = contextHandler.getServletContext.getAttribute(TapestryFilter.REGISTRY_CONTEXT_NAME).asInstanceOf[Registry]

        (server, contextHandler)
    }
    protected def getService[T](clazz: Class[T]): T = registry.getService(clazz)
}