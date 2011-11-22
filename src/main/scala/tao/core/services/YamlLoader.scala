/*
 * Copyright 2011 The Yishishun IT Department.
 * site: http://www.taobao.pk
 */
package tao.core.services

import org.yaml.snakeyaml.constructor.Constructor
import java.io.{InputStream, InputStreamReader}
import org.yaml.snakeyaml.{Yaml, TypeDescription}
import org.springframework.core.io.DefaultResourceLoader
import org.apache.commons.io.IOUtils

/**
 * implements YamlLoader
 * @author jcai
 * @version 0.1
 */
object YamlLoader{
    private val resourceLoader = new DefaultResourceLoader
    def loadConfigFromResource[T <: Object](location:String)(implicit m: Manifest[T]):T = {
        var is:InputStream = null
        try{
            is=resourceLoader.getResource(location).getInputStream
            return loadConfig(is)
        }finally{
            IOUtils.closeQuietly(is)
        }
    }
    /**
     * load config file
     */
    def loadConfig[T <: Object](is:InputStream)(implicit m: Manifest[T]):T = {
        //obtain type parameter
        val clazz = m.erasure.asInstanceOf[Class[T]]
        val constructor = new Constructor(clazz);
        val carDescription = new TypeDescription(clazz);
        constructor.addTypeDescription(carDescription);
        val yaml = new Yaml(constructor);
        val reader = new InputStreamReader(is, "UTF-8");
        yaml.load(reader).asInstanceOf[T]
    }
    def loadConfig[T <: Object](str:String)(implicit m: Manifest[T]):T = {
        //obtain type parameter
        val clazz = m.erasure.asInstanceOf[Class[T]]
        val constructor = new Constructor(clazz);
        val carDescription = new TypeDescription(clazz);
        constructor.addTypeDescription(carDescription);
        val yaml = new Yaml(constructor);
        yaml.load(str).asInstanceOf[T]
    }
    def toYml[T <: Object](obj:T):String = {
        //obtain type parameter
        val clazz = obj.getClass.asInstanceOf[Class[T]]
        val constructor = new Constructor(clazz);
        val carDescription = new TypeDescription(clazz);
        constructor.addTypeDescription(carDescription);
        val yaml = new Yaml(constructor);
        yaml.dump(obj)
    }
}