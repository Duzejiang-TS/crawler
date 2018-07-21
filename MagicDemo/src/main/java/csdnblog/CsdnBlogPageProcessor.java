package csdnblog;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

public class CsdnBlogPageProcessor implements PageProcessor {

    // 设置csdn用户名
    private static String username = "qq598535550";//"ts_forever";
    // 共抓取到的文章数量
    private static int size = 0;
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    // process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    @Override
    public void process(Page page) {

        // 列表页
        if (!page.getUrl().regex("http://blog\\.csdn\\.net/" + username + "/article/details/\\d+").match()) {
            // 添加所有文章页
            page.addTargetRequests(page.getHtml().xpath("//div[@class='article-list']").links()// 限定文章列表获取区域
                    .regex("/" + username + "/article/details/\\d+")
                    .replace("/" + username + "/", "http://blog.csdn.net/" + username + "/")// 巧用替换给把相对url转换成绝对url
                    .all());
            // 添加其他列表页
            page.addTargetRequests(page.getHtml().xpath("//div[@id='papelist']").links()// 限定其他列表页获取区域
                    .regex("/" + username + "/article/list/\\d+")
                    .replace("/" + username + "/", "http://blog.csdn.net/" + username + "/")// 巧用替换给把相对url转换成绝对url
                    .all());
            // 文章页//*[@id="mainBox"]/main/div[2]/div[1]
        } else {
            size++;// 文章数量加1
            // 用CsdnBlog类来存抓取到的数据，方便存入数据库
            CsdnBlog csdnBlog = new CsdnBlog();
            // 设置编号
            csdnBlog.setId(Integer.parseInt(
                    page.getUrl().regex("http://blog\\.csdn\\.net/" + username + "/article/details/\\d+").get()));
            // 设置标题
            csdnBlog.setTitle(
                    page.getHtml().xpath("//*[@id='mainBox']/main/div[2]/div[" + size + "]/h4/a/text()").get());
            // 设置日期
            csdnBlog.setDate(
                    page.getHtml().xpath("//*[@id='mainBox']/main/div[2]/div[" + size + "]/div/p[1]/span/text()").get());
            // 设置标签（可以有多个，用,来分割）
            csdnBlog.setTags("0");
            // 设置类别（可以有多个，用,来分割）
            csdnBlog.setCategory("1");
            // 设置阅读人数
            csdnBlog.setView(Integer.parseInt(page.getHtml().xpath("//*[@id='mainBox']/main/div[2]/div[" + size + "]/div/p[2]/span")
                    .regex("(\\d+)人阅读").get()));
            // 设置评论人数
            csdnBlog.setComments(Integer.parseInt(page.getHtml()
                    .xpath("//*[@id='mainBox']/main/div[2]/div[" + size + "]/div/p[3]/span").regex("\\((\\d+)\\)").get()));
            // 设置是否原创
            //*[@id="mainBox"]/main/div[2]/div[1]/h4/a/span
            //*[@id="mainBox"]/main/div[2]/div[2]/h4/a/span
            csdnBlog.setCopyright(page.getHtml().regex("bog-copyright").match() ? 1 : 0);
            // 把对象存入数据库
            new CsdnBlogDao().add(csdnBlog);
            // 把对象输出控制台
            System.out.println(csdnBlog);
        }

    }

    @Override
    public Site getSite() {
        return site;
    }

    // 把list转换为string，用,分割
    public static String listToString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            } else {
                flag = true;
            }
            result.append(string);
        }
        return result.toString();
    }

    public static void main(String[] args) {
        long startTime, endTime;
        System.out.println("【爬虫开始】请耐心等待一大波数据到你碗里来...");
        startTime = System.currentTimeMillis();
        // 从用户博客首页开始抓，开启5个线程，启动爬虫
        Spider.create(new CsdnBlogPageProcessor()).addUrl("http://blog.csdn.net/" + username)
                .thread(5)
                .run();
        endTime = System.currentTimeMillis();
        System.out.println("【爬虫结束】共抓取" + size + "篇文章，耗时约" + ((endTime - startTime) / 1000) + "秒，已保存到数据库，请查收！");
    }
}
