package common.download.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.pojos.HtmlInfo;
import common.pojos.ReportData;
import common.download.DataThreadControl;
import common.download.GenericMetaCommonDownload;
import common.rmi.packet.SearchKey;
import common.system.Systemconfig;
import common.utils.StringUtil;
import common.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下载元数据
 * 
 * @author grs
 */
public class ReportMetaCommonDownload extends GenericMetaCommonDownload<ReportData> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportMetaCommonDownload.class);

	public ReportMetaCommonDownload(SearchKey key) {
		super(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process() {

		List<ReportData> alllist = new ArrayList<ReportData>();
		List<ReportData> list = new ArrayList<ReportData>();
		String url = getRealUrl(siteinfo, key.getSITE_ID() != null ? key.getKEYWORD() : gloaburl);//
		int page = getRealPage(siteinfo);
		String keyword = key.getKEYWORD();
		map.put(keyword, 1);
		String nexturl = url;
		DataThreadControl dtc = new DataThreadControl(siteFlag, keyword);
		HtmlInfo html = htmlInfo("META");

		int totalCount = 0;
		while (nexturl != null && !nexturl.equals("")) {
			list.clear();

			html.setOrignUrl(nexturl);
			try {
				http.getContent(html);
				if (html.getContent().contains("<title>error-")) {
					html.setOrignUrl(nexturl.replace("fulltext1y/cninfo", "hkmblatest"));
					http.getContent(html);
				}
				// html.setContent(common.utils.StringUtil.getContent("filedown/META/baidu_news_search/6f962c1b7d205db4faf80453362b648e.htm"));
				nexturl = xpath.templateListPage(list, html, map.get(keyword), keyword, nexturl);

				if (list.size() == 0) {
					LOGGER.info(url + "元数据页面解析为空！！");
					TimeUtil.rest(siteinfo.getDownInterval());
					break;
				}
				LOGGER.info(url + "元数据页面解析完成。");

				totalCount += list.size();
				Systemconfig.urlFilter.filterDuplication(list);
				if (list.size() == 0) {
					if (alllist.size() == 0)
						TimeUtil.rest(siteinfo.getDownInterval());
					// break;
				}
				alllist.addAll(list);

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

		try {
			Systemconfig.dbService.saveDatas(alllist);

			alllist.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}

		dtc.process(alllist, siteinfo.getDownInterval(),null, key);
	}

	/**
	 * @param curr
	 * @param last
	 * @return false: 不是最后一页; true: 是最后一页
	 */
	protected boolean ifStop(String curr, String last) {
		if (last != null && curr != null) {
			String currPage = StringUtil.regMatcher(curr, "\"page\"", ",");
			String lastPage = StringUtil.regMatcher(last, "\"page\"", ",");
			if (currPage != null && lastPage != null) {
				if (currPage.equals(lastPage)) {
					LOGGER.info("当前页和上一页内容相同，已是最后一页，退出.");
					return true;
				}
			}
		}

		return false;
	}

}
