package snails.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtil {

    private final static Logger log = LoggerFactory.getLogger(WebUtil.class);

    public static String getContent(String url, String charset) throws Exception {
        InputStream in = null;
        String s = "";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Connection", "close");
            in = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, charset));
            StringBuffer sb = new StringBuffer();
            char[] charBuf = new char[1024 * 4];
            int len = br.read(charBuf);
            while (len != -1) {
                sb.append(charBuf, 0, len);
                len = br.read(charBuf);
            }
            br.close();
            s = sb.toString();
        } catch (BindException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        return s;
    }

    public static void main(String[] args) throws Exception {
        // System.out.println(getContent("http://tradecardseller.wangwang.taobao.com/tradecard/nameCard.htm?uid=cntaobaosimpleliangy",
        // "gbk"));
        System.out
                .println(getContent(
                        "http://117.135.134.240/msg/HttpPkgSM?account=zhuluu&pswd=123456Aa&product=1233577153&msg=15167893925|亲保邮哦&msg=15167893925|亲保邮哦2",
                        "gbk"));
    }

    public static String doPost(String Url, NameValuePair[] data) throws HttpException, IOException {
        // NameValuePair[] data = {
        // new NameValuePair("account", "te_kqw"),
        // new NameValuePair("method", "SendSms"),
        // new NameValuePair("password", "a123456"),
        // new NameValuePair("mobile", sms.getMobile()),
        // new NameValuePair("pid", "4"),
        // new NameValuePair("content", sms.getMessage())};
        // String result = WebUtil.doPost(Url, data);

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(Url);
        client.getParams().setContentCharset("UTF-8");
        client.getParams().setConnectionManagerTimeout(10000);
        method.setRequestHeader("ContentType", "application/x-www-form-urlencoded;charset=UTF-8");
        client.executeMethod(method);
        method.setRequestBody(data);
        return method.getResponseBodyAsString();
    }

    public static String post(String url, List<NameValuePair> params) {

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

        PostMethod method = new PostMethod(url);
        method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

        if (!CollectionUtils.isEmpty(params)) {
            for (NameValuePair param : params) {
                method.addParameter(param);
            }
        }

        try {
            int status = client.executeMethod(method);
            if (status != HttpStatus.SC_OK) {
                log.error("post eror, status code:" + status);
            } else {
                return method.getResponseBodyAsString();
            }
        } catch (HttpException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
        return StringUtils.EMPTY;
    }

    public static String get(String url) {

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

        GetMethod method = new GetMethod(url);
        method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

        try {
            int status = client.executeMethod(method);
            if (status != HttpStatus.SC_OK) {
                log.error("Get subway rate error, status code:" + status);
            } else {
                return method.getResponseBodyAsString();
            }
        } catch (HttpException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

}
