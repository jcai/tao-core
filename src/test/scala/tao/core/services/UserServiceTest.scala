package tao.core.services

import tao.core.config.TaobaoAppConfig
import java.io.{File, FileInputStream}
import java.util.concurrent.CountDownLatch
import org.junit.{Assert, Test}
import org.easymock.EasyMock
import org.apache.tapestry5.services.Cookies

/**
 *
 * @author jcai
 * @version 0.1
 */
class UserServiceTest {
    @Test
    def test_initUser{
        val cookies=EasyMock.createMock(classOf[Cookies])
        cookies.writeCookieValue(EasyMock.isA(classOf[String]),EasyMock.isA(classOf[String]))

        EasyMock.replay(cookies)

        val config = YamlLoader.loadConfigFromResource[TaobaoAppConfig]("classpath:test_config.yml")
        val userService = new UserService(config,new MongoTemplate(config),new TaobaoApiClient(config),cookies)
        try{
            userService.initUser("session","acai",false)
        }catch{
            case e=>
                Assert.assertEquals(e.getMessage,"Invalid session:Session not exist")
        }

        EasyMock.verify(cookies)
    }
}