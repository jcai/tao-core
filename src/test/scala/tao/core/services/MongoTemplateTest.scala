package tao.core.services

import tao.core.config.TaobaoAppConfig
import com.mongodb.casbah.commons.MongoDBObject
import org.junit.{Assert, Test}
import com.mongodb.casbah.Imports._

/**
 *
 * @author jcai
 * @version 0.1
 */

class MongoTemplateTest {
    @Test
    def test_saveOrUpdate{
        val config = new TaobaoAppConfig
        val mongoTemplate = new MongoTemplate(config)
        mongoTemplate.saveOrUpdate("test",MongoDBObject("nick"->"acai"),MongoDBObject("name"->"test1"))
        mongoTemplate.saveOrUpdate("test",MongoDBObject("nick"->"acai"),MongoDBObject("sex"->"test2"))
        val q = MongoDBObject("nick" ->"acai")
        Assert.assertEquals(1,mongoTemplate.find("test",q).count)
        mongoTemplate.delete("test",q)
        Assert.assertEquals(0,mongoTemplate.find("test",q).count)
    }
}