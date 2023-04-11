package com.smart.ppx;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    private static final String SERVER = "http://rezz.top:8080/chat/ppx?shareUrl=";


    private static final String SHORT = "http://suo.nz/api.php";
    private static final String KEY = "5e669b9b44bb357519e2ff0c@e0ea2bb377708b3344dd4a57193de9e5";

    private static final String QUERY = "http://whois.pconline.com.cn/ipJson.jsp";

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void test() {
        /*String shareUrl = "https://h5.pipix.com/s/nAKbEs/";
        String a = shareUrl.substring(23,30);
        System.out.println(a);*/

        //String b = "http://rezz.top:8080/chat/play?postfix=nxUpJM/";

        /*String c = getCorrectUrl(SHORT,b,KEY);
        IClient client = new HttpClient(c);
        String result = client.get();
        System.out.println(result);*/

      //  System.out.println(request(SHORT, b, KEY));

    }



    public static String getCorrectUrl(String host, String url, String key) {
        StringBuilder builder = new StringBuilder();
        try {
            String formatUrl = URLEncoder.encode(url, "UTF-8");
            builder.append(host).append("?url=").append(formatUrl).append("&key=").append(key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }


}