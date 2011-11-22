package tao.core.services

import tao.core.config.TaobaoAppConfig
import java.io.{File, FileInputStream}
import java.util.concurrent.CountDownLatch
import org.junit.{Assert, Test}

/**
 *
 * @author jcai
 * @version 0.1
 */
class UserServiceTest {
    @Test
    def test_initUser{
        val config = YamlLoader.loadConfigFromResource[TaobaoAppConfig]("classpath:test_config.yml")
        val userService = new UserService(config,new MongoTemplate(config),new TaobaoApiClient(config))
        try{
            userService.initUser("session","acai",false)
        }catch{
            case e=>
                Assert.assertEquals(e.getMessage,"Invalid session:Session not exist")
        }
    }
}