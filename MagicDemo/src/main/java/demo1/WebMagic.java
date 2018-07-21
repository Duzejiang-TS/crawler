package demo1;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

    public class WebMagic implements PageProcessor {

        public static void main(String[] args) {
            Spider.create(new WebMagic())
                    // 从"http://sousuo.gov.cn/column/30611/0.htm"开始抓
                    .addUrl("http://sousuo.gov.cn/column/30611/0.htm")
                    // 抓取页面的存储路径
                    .addPipeline(new FilePipeline("/data/pachong/govnews"))
                    // 开启5个线程抓取 ，底层处理了线程同步
                    .thread(5)
                    // 启动爬虫
                    .run();
        }

        // 第一步：DownLoader加载器下载网页时，抓取网站的相关配置，包括编码、抓取间隔、重试次数等
        private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

        //第二步： process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
        @Override
        public void process(Page page) {
            //每个html网页的url地址
            System.out.println(page.getUrl());
            //获取网页的title，该title位于haead标签中，text()方法表示获得文本内容
            String title = page.getHtml().xpath("//head/title/text()").toString();
            System.out.println(title);
            if (title == null) {
                //setSkip这个方法是对resultItems的内容进行忽略，默认设置为false，就是在本层逻辑中，爬取到的信息不进入管道进行保存。
                page.setSkip(true);
            }
            page.putField("allhtml", page.getHtml().toString());
            // 从页面发现后续的url地址来抓取  采用正则表达式去匹配网页名字，分为两种：1、新闻列表项；2、新闻详细页
            // 1、采集该网站新闻列表页 ，
            page.addTargetRequests(page.getHtml().links().regex("(http://sousuo.gov.cn/column/30611/\\d+.htm)").all());
            // 2、采集该网站每条新闻详细页
            page.addTargetRequests(
                    page.getHtml().links().regex("(http://www.gov.cn/xinwen/2017-\\d+/\\d+/content_\\d+.htm)").all());
        }

        //此方法在Downloader时底层会调用该方法获得site，包括爬虫时的一些设置信息。
        @Override
        public Site getSite() {
            return site;
        }
    }
