package tao.core.services

import org.junit.Test
import com.taobao.api.internal.util.{TaobaoHashMap, RequestParametersHolder, TaobaoUtils}
import collection.JavaConversions._
import org.slf4j.LoggerFactory
import java.util.TreeMap
import com.taobao.api.Constants
import java.io.IOException
import java.security.{GeneralSecurityException, MessageDigest}

/**
 * taoba query language test
 * @author jcai
 * @version 0.1
 */
class TqlTest {
    private val logger = LoggerFactory getLogger getClass
    @Test
    def test_query{
        val ql="select price,title from items where nicks=xiser0620 and start_price=100 and order_by=price:desc"
        val url="http://gw.api.taobao.com/tql/2.0/json"
        val appKey="12405322"
        val appSecret="047d4fa94c704107f84a38daeec9681b"
        //val appKey="test"
        //val appSecret="test"
        val parameter = new RequestParametersHolder
        val appParams = new TaobaoHashMap
        val sortedParams = new TreeMap[String, String]();
        sortedParams.put("ql",ql)
        sortedParams.put("app_key",appKey)
        sortedParams.put("sign_method","md5")
        val str=sortedParams.foldLeft(appSecret){(r,s)=>{
            r+s._1+s._2
        }}+appSecret
        val sign = byte2hex(encryptMD5(str))
        sortedParams.put("sign",sign)

        logger.debug("sign:{}",sortedParams)
        println(HttpRestClient.get(url,sortedParams.toMap))
    }


    private def encryptMD5(data: String): Array[Byte] = {
        var bytes: Array[Byte] = null
        try {
            var md: MessageDigest = MessageDigest.getInstance("MD5")
            bytes = md.digest(data.getBytes(Constants.CHARSET_UTF8))
        }
        catch {
            case gse: GeneralSecurityException => {
                throw new IOException(gse)
            }
        }
        return bytes
    }
    private def byte2hex(bytes:Array[Byte]):String= {
		val sign = new StringBuilder();
        0.until(bytes.length).foreach(i=>{
            val hex = Integer.toHexString(bytes(i) & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        })
		return sign.toString();
	}
}