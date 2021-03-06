package common.download.news;

import java.util.ArrayList;
import java.util.List;

import common.bean.HtmlInfo;
import common.bean.NewsData;
import common.download.DataThreadControl;
import common.download.GenericMetaCommonDownload;
import common.http.SimpleHttpProcess;
import common.rmi.packet.SearchKey;
import common.system.Systemconfig;
import common.util.TimeUtil;

/**
 * 下载元数据
 *
 * @author grs
 */
public class NewsMetaCommonDownload extends GenericMetaCommonDownload<NewsData> {

    public NewsMetaCommonDownload(SearchKey key) {
        super(key);
    }

    @Override
    public void process() {

        TimeUtil.rest(3);
        /* 状态 */

        List<NewsData> alllist = new ArrayList<NewsData>();
        List<NewsData> list = new ArrayList<NewsData>();
        String url = getRealUrl(siteinfo, key.getId() > 0 ? key.getKey() : gloaburl);
        int page = getRealPage(siteinfo);
        String keyword = key.getKey();
        map.put(keyword, 1);
        String nexturl = url;
        DataThreadControl dtc = new DataThreadControl(siteFlag, keyword);
        HtmlInfo html = htmlInfo("META");
        int totalCount = 0;
        while (nexturl != null && !nexturl.equals("")) {
            list.clear();

            html.setOrignUrl(nexturl);
            try {

                if (nexturl.contains("163.com"))
                    html.setAcceptEncoding("Accept-Encoding: gzip, deflate");
                http.getContent(html);
                // html.setContent(common.util.StringUtil.getContent("filedown/META/baidu_news_search/6f962c1b7d205db4faf80453362b648e.htm"));
                nexturl = xpath.templateListPage(list, html, map.get(keyword), keyword, nexturl, key.getRole() + "");

                if (list.size() == 0) {
                    Systemconfig.sysLog.log("关键词：[" + key.getKey() + "] " + url + "元数据页面解析为空！！");
                    TimeUtil.rest(siteinfo.getDownInterval());
                    break;
                }
                Systemconfig.sysLog.log("关键词：[" + key.getKey() + "] " + url + "元数据页面解析完成。");
                totalCount += list.size();
                Systemconfig.dbService.getNorepeatData(list, "");
                if (list.size() == 0) {
                    Systemconfig.sysLog.log("关键词：[" + key.getKey() + "] " + url + " 无新数据。");

                    TimeUtil.rest(siteinfo.getDownInterval());
                    break;
                }
                alllist.addAll(list);

                dtc.process(list, siteinfo.getDownInterval() - 5, null, key);

                map.put(keyword, map.get(keyword) + 1);
                if (map.get(keyword) > page)
                    break;
                url = nexturl;
                if (nexturl != null)
                    TimeUtil.rest(siteinfo.getDownInterval());

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        // //ID大于0表示有相同新闻需要采集
        // if(key.getId() > 0) {
        // for(int i = 0;i< alllist.size();i++) {
        // list.get(i).setId(key.getId());
        // new NewsSameDataCommonDownload(siteFlag,list.get(i), null).run();
        // }
        // } else {
        // dtc.process(alllist, siteinfo.getDownInterval()-5);
        // }

        if (alllist.size() == 0) {
            return;
        }


        Systemconfig.sysLog.log("关键词：[" + key.getKey() + "] 列表页检索完成，不重复数据：" + alllist.size() + "条。");
    }

    @Override
    protected void specialHtmlInfo(HtmlInfo html) {

        html.setUa(SimpleHttpProcess.getRandomUserAgent());
    }

}
