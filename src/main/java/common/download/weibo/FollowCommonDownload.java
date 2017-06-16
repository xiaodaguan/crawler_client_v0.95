package common.download.weibo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.pojos.HtmlInfo;
import common.pojos.UserData;
import common.download.GenericMetaCommonDownload;
import common.extractor.xpath.XpathExtractor;
import common.extractor.xpath.weibo.monitor.WeiboMonitorXpathExtractor;
import common.rmi.packet.SearchKey;
import common.pojos.CollectDataType;
import common.system.Systemconfig;
import common.system.UserAttribute;
import common.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下载数据
 * @author grs
 */
public class FollowCommonDownload extends GenericMetaCommonDownload<UserData> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FollowCommonDownload.class);

	private UserAttribute user;
	private int id;
	public FollowCommonDownload(SearchKey key, int id, UserAttribute user) {
		super(key);
		this.id = id;
		this.user = user;
	}
	@Override
	public void process() {
		List<UserData> alllist = new ArrayList<UserData>();
		List<UserData> list = new ArrayList<UserData>();
		String url = getRealUrl(siteinfo, gloaburl);
		String nexturl = url;
		HtmlInfo html = htmlInfo(CollectDataType.FOLLOW.name());
		int count = 1;
		try {
			while(nexturl != null && !nexturl.equals("")) {
				list.clear();
				
				html.setOrignUrl(nexturl);
				
				try {
					http.getContent(html, user);
//					html.setContent(common.utils.StringUtil.getContent("filedown/FOLLOW/sina/e8940d3a1f60ab11b7ca75404e0b30c3.htm"));
					
					nexturl =((WeiboMonitorXpathExtractor)((XpathExtractor)xpath)).templateRelation(list, html, count, id+"", nexturl, siteFlag);
					
					if(list.size()==0) {
						LOGGER.info(url + "元数据页面解析为空！！");
						break;
					}
					LOGGER.info(url + "元数据页面解析完成。");
					
					Systemconfig.urlFilter.filterDuplication(list);
					
					alllist.addAll(list);
					
					url = nexturl;
					count++;
					nexturl = null;
						TimeUtil.rest(siteinfo.getDownInterval());
					
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
			Systemconfig.dbService.saveDatas(alllist);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			alllist.clear();
			list.clear();
		}
	}
	
}
