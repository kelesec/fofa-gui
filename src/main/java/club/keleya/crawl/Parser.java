package club.keleya.crawl;

import club.keleya.data.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;

public class Parser {
    /**
     * 提取单个元素中的URL标签
     * @param element 元素
     * @return URL
     */
    private static String getUrl(Element element) {
        Elements urlSpan = element.select("span.hsxa-host > a");

        if (!urlSpan.isEmpty() && urlSpan.hasAttr("href")) {
            return urlSpan.attr("href");
        }

        String text = element.select("span.hsxa-host").text();
        if (!text.isEmpty()) {
            return text;
        }

        return urlSpan.text();
    }

    /**
     * 从元素中提取出Host
     * @param element 元素
     * @return Host
     */
    private static String getHost(Element element) {
        Element hostEl = element.select("p > a.hsxa-jump-a").first();
        if (hostEl == null) {
            return "";
        } else {
            return hostEl.text();
        }
    }

    /**
     * 获取Port
     * @param element 元素
     * @return port
     */
    private static int getPort(Element element) {
        Element portEl = element.select("a.hsxa-port").first();
        if (portEl != null) {
            try {
                return Integer.parseInt(portEl.text());
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * 获取站点标题
     * @param element 元素
     * @return title
     */
    private static String getTitle(Element element) {
        Element titleEl = element.select("[class=hsxa-meta-data-list-main-left hsxa-fl] > p").first();

        if (titleEl != null) {
            return titleEl.text();
        }

        return "";
    }

    /**
     * 获取Server
     * @param element 元素
     * @return server
     */
    private static String getServer(Element element) {
        Element serverEl = element.select("a.hsxa-list-span-item").last();

        if (serverEl != null) {
            return serverEl.text();
        }

        return "";
    }

    /**
     * 解析获取Data集合
     * @param html 页面HTML
     * @param index Data对象的id字段
     * @return 包含Data对象的集合
     */
    public static HashSet<Data> getDatas(String html, int index) {
        Document doc = Jsoup.parse(html);
        Element body = doc.body();

        HashSet<Data> dataSet = new HashSet<>();

        // 获取当前页面的数据
        String url;
        String host;
        int port;
        String title;
        String server;

        for (Element element : body.select("div.el-checkbox-group > div")) {
            url = getUrl(element);         // 获取URL
            host = getHost(element);       // 获取Host
            port = getPort(element);       // 获取port
            title = getTitle(element);     // 获取标题
            server = getServer(element);   // 获取Server
//            System.out.printf("%s, %s, %d, %s, %s\r\n", url, host, port, title, server);
            dataSet.add(new Data(index, url, host, port, title, server, false));
            index++;
        }

        return dataSet;
    }
}
