package snails.common.util.url;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.internal.util.WebUtils;

/**
 * @ClassName: ShortUrlGenerator
 * @Description: 短地址生成
 * @author chenlinlin
 * @date 2015年3月9日 下午9:51:10
 */
public class ShortUrlGenerator {

	private static Logger log = LoggerFactory.getLogger(ShortUrlGenerator.class);

	public static String shortUrl(String url) throws IOException {
		String middle = "", result = "";
		Map<String, String> params = new HashMap<String, String>();
		params.put("url", url);
		middle = WebUtils.doPost("http://dwz.cn/create.php", params, 10000, 10000);
		JSONObject object = JSONObject.fromObject(middle);
		result = object.get("tinyurl").toString();
		return result;
	}

	public static void main(String args[]) throws IOException {
		System.out.println(ShortUrlGenerator.shortUrl("www.taobao.com"));
	}
}
