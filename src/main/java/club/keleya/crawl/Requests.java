package club.keleya.crawl;

import club.keleya.data.Data;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Requests {
    private final HashSet<Data> datas;
    private static int threads;
    private static ExecutorService executorService;
    private final static String url = "https://fofa.info/result";
    private final static PoolingHttpClientConnectionManager cm;
    private final static CloseableHttpClient client;
    private final static BasicCookieStore cookieStore;
    private final static RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000L))
            .setResponseTimeout(Timeout.ofMilliseconds(5000L))
            .build();

    // 初始化
    static {
//        datas = new HashSet<>();
        threads = 5;
        cm = new PoolingHttpClientConnectionManager();

        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);
        cookieStore = new BasicCookieStore();
        client = HttpClients.custom().setDefaultCookieStore(cookieStore)
                .setConnectionManager(cm).build();
    }

    public Requests() {
        datas = new HashSet<>();
    }

    /**
     *
     * @param params 请求参数
     * @param cookie Cookie信息
     * @return 页面内容
     */
    private static CloseableHttpResponse doGet(ArrayList<NameValuePair> params, String cookie) {
        // 构造参数请求URI
        URI uri = null;
        try {
            uri = new URIBuilder(new URI(url)).addParameters(params).build();
//            System.out.println(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // 创建HttpGet对象
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(config);
        httpGet.setHeader("Cookie", cookie);

        try {
            CloseableHttpResponse response = client.execute(httpGet);
            if (response.getCode() == 200) {
                System.out.printf("\33[32;2m[+] %d: %s\33[0m\r\n", response.getCode(), uri);
            } else {
                System.out.printf("\33[31;2m[-] %d: %s\33[0m\r\n", response.getCode(), uri);
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 将Response解析为HTML
     * @param response 响应数据
     * @return HTML
     */
    private static String getHtml(CloseableHttpResponse response) {
        if (response != null && response.getCode() == 200) {
            try {
                return EntityUtils.toString(response.getEntity());
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    /**
     * 获取总共多少个页面
     * @param query 语法
     * @return 页面数
     */
    private static int getMaxPage(String query) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("qbase64", query));

        // 获取响应数据
        CloseableHttpResponse response = doGet(params, null);
        String html = getHtml(response);

        if (html == null) {
            return -1;
        }

        // Jsoup解析
        Document dom = Jsoup.parse(html);
        Element ele = dom.select("span.hsxa-highlight-color").first();

        if (ele != null) {
            int max = Integer.parseInt(ele.text().replace(",", ""));
            max = Math.min(max, 100000);    // 最多只能获取100000数据
            return max % 10 == 0 ? max / 10 : max / 10 + 1;
        }
        return -1;
    }

    /**
     * 多线程查询
     * @param query 查询语法
     * @param fofaToken Cookie中的 `fofa_token` 字段
     * @param fromPage 开始查询的页面
     * @param toPage 结束页面
     * @param _threads 线程数，最大30，默认5
     * @return 包含Data数据类型的集合(HashSet)
     */
    public HashSet<Data> query(String query, String fofaToken, int fromPage, int toPage, int _threads) {
        if (_threads > 0) {
            threads = Math.min(_threads, 30);
        }

        String _query = Base64.getEncoder().encodeToString(query.getBytes());
        String cookie = "fofa_token=" + fofaToken;

        if (toPage == -1) {
            try {
                int max = getMaxPage(_query);
                toPage = max == -1 ? 1 : max;
            } catch (Exception e) {
                toPage = 1;
            }
        }

//         记录页码，通过Boolean值判断是否成功请求此页数据，False表示未请求
        HashMap<Integer, Boolean> pageMap = new HashMap<>();
        for (int i = fromPage; i <= toPage; i++) {
            pageMap.put(i, Boolean.FALSE);
        }

        // 创建多线程
        executorService = Executors.newFixedThreadPool(threads);

        // 并发解析
        boolean flag = false;
        int count = pageMap.size();
        while (!flag) {

            // 计数
            CountDownLatch countDownLatch = new CountDownLatch(count);
            count = 0;

            for (Map.Entry<Integer, Boolean> entry: pageMap.entrySet()) {
                if (entry.getValue()) {
                    continue;
                }

                executorService.submit(()->{
                    // 配置请求参数
                    ArrayList<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("qbase64", _query));
                    params.add(new BasicNameValuePair("page", entry.getKey().toString()));

                    // 解析响应
                    CloseableHttpResponse response = doGet(params, cookie);
                    if (response != null && response.getCode() == 200) {
                        entry.setValue(true);

                        // 解析内容
                        String html = getHtml(response);
                        HashSet<Data> dataSet = Parser.getDatas(html, this.datas.size());
                        datas.addAll(dataSet);
                    }
                    countDownLatch.countDown();
                });
            }

            // 线程阻塞：等待子线程请求执行完毕
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 判断是否全部请求完毕
            flag = true;
            for (Boolean value : pageMap.values()) {
                if (!value) {
                    flag = false;
                    count++;
                }
            }
        }

        executorService.shutdown();
        return this.datas;
    }

    /**
     * 关闭线程池
     */
    public static void shutdownThreadPool() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
