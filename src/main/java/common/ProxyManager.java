package common;

import common.downloader.DefaultDownloader;
import common.http.SimpleHttpProcess;
import common.system.AppContext;
import common.system.Systemconfig;
import okhttp3.OkHttpClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProxyManager {


    public ProxyManager() {

        Systemconfig.crawlerType = 1;
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        AppContext.initAppCtx(path);
    }

    public String getOneFromProvider() {
        return SimpleHttpProcess.getRandomProxy();
    }

    public void run() {

        Systemconfig.proxyPoolRedis.clearAll();
        while (true) {
            try {
                String oneProxy = getOneFromProvider();
                Systemconfig.proxyPoolRedis.addOne(oneProxy);

            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    Thread.sleep(1000 * 3);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        new ProxyManager().run();

    }
}