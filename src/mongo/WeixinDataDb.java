package mongo;

import common.bean.WeixinData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Created by guanxiaoda on 16/4/15.
 */
public class WeixinDataDb extends Db<WeixinData> {
    Logger logger = LoggerFactory.getLogger(WeixinDataDb.class);
    private Connection conn = null;
    private String URL = "jdbc:oracle:thin:@172.18.79.3:1521/ORCL";
    private String USERNAME = "jinrong";
    private String PASSWORD = "jinrong";
    private String TABLE = "weixin_data";

    public WeixinDataDb() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();

            this.conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @Override
    public int saveData(WeixinData wd) {
        String sql = "insert into " + TABLE + "(" +
                "title, author, pubtime, url, search_keyword, " +
                "source, category_code, inserttime, md5, content, " +
                "brief, img_url, read_num, like_num" +
                ") values(" +
                "?,?,?,?,?," +
                "?,?,?,?,?," +
                "?,?,?,?" +
                ")";
//        String sql = "insert into " + TABLE + "(title) values(?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, wd.getTitle());
            ps.setString(2, wd.getAuthor());
            ps.setTimestamp(3, new Timestamp(wd.getPubdate().getTime()));
            ps.setString(4, wd.getUrl());
            ps.setString(5, wd.getSearchKey());

            ps.setString(6, wd.getSource());
            ps.setInt(7, wd.getCategoryCode());
            ps.setTimestamp(8, new Timestamp(wd.getInserttime().getTime()));
            ps.setString(9, wd.getMd5());
            ps.setString(10, wd.getContent());

            ps.setString(11, wd.getBrief());
            ps.setString(12, wd.getImgUrl());
            ps.setInt(13, wd.getReadNum());
            ps.setInt(14, wd.getPraiseNum());

            ps.execute();
            int count = ps.getUpdateCount();

            logger.info("weixindata:[{}] wrote into oracle: [{}]", wd.getTitle(), URL + "/" + TABLE);
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    public static void main(String[] args) {
        WeixinData wd = new WeixinData();
        wd.setTitle("test");
        WeixinDataDb wdb = new WeixinDataDb();
        wdb.saveData(wd);
    }
}
