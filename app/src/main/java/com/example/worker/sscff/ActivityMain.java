package com.example.worker.sscff;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.androidquery.util.XmlDom;
import org.xml.sax.SAXException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ActivityMain extends Activity implements SwipeRefreshLayout.OnRefreshListener{

    private AQuery aq;
    private Activity activity;
    private RecyclerView gridView;
    private StaggeredGridLayoutManager mLayoutManager;
    private AdapterMain adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<ClassItem> items = new ArrayList<ClassItem>();

    private final String[] FEEDS = new String[]{"http://siliconrus.com/feed/","http://habrahabr.ru/rss/","http://megamozg.ru/rss/","http://geektimes.ru/rss/"};
    private DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        aq = new AQuery(activity);
        AQUtility.setDebug(true);

        swipeRefreshLayout = new SwipeRefreshLayout(activity);
        swipeRefreshLayout.setOnRefreshListener(this);


        gridView = new RecyclerView(activity);
        gridView.setHasFixedSize(true);
        mLayoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
        gridView.setLayoutManager(mLayoutManager);
        gridView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout.addView(gridView);
        getWindow().setContentView(swipeRefreshLayout);

        adapter = new AdapterMain(activity,items);
        gridView.setAdapter(adapter);

        getFeeds();
    }

    public void getFeeds() {
        items.clear();
        for(String feed:FEEDS){
            request(feed);
        }
    }

    public void request(String url) {
        aq.ajax(url, XmlDom.class, this, "onRequest");
        swipeRefreshLayout.setRefreshing(true);
    }

    public void onRequest(String url,XmlDom xml, AjaxStatus status) {
        if (status.getCode()==200) {
            String logo = "";
            try {
                logo = xml.tags("url").get(0).text();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            List<XmlDom> xmlItems = xml.tags("item");

            for(XmlDom xmlItem: xmlItems){
                ClassItem item = new ClassItem();

                // Получаем описание
                String description = xmlItem.tag("description").text();
                item.setDescription(description);

                //Получаем лого
                item.setLogo(logo);

                // Получаем автора
                try {
                    item.setAuthor(xmlItem.tag("author").text());
                }
                catch (Exception e) {
                }

                //Получаем заголовки
                item.setTitle(xmlItem.tag("title").text());

                // Получаем ссылку
                item.setLink(xmlItem.tag("link").text());

                //Получаем дату публикации
                String pubDate = xmlItem.tag("pubDate").text();
                Date date = new Date();
                try {
                    date = formatter.parse(pubDate);
                }
                catch (Exception e) {
                    AQUtility.debug("errorParsingDate",e.toString());
                }
                item.setDate(date);

                //Получаем изображение
                String src1 = "";
                String src2 = "";

                try {
                    //description = description.replace(".png\">",".png\"/>").replace(".jpg\">",".jpg\"/>");

                    src1 = new XmlDom("<xml>"+description+"</xml>").tag("img").attr("src");
                    if (src1.startsWith("//") ) {
                        src1 = "http:"+src1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    description = description.replace(".png\">",".png\"/>").replace(".jpg\">",".jpg\"/>");

                    src2 = new XmlDom("<xml>"+description+"</xml>").tag("img").attr("src");
                    if (src2.startsWith("//") ) {
                        src2 = "http:"+src2;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (src1 != "") {
                    item.setImg(src1);
                }
                item.setImg(src2);

                //
                items.add(item);
            }
            Collections.sort(items, new Comparator<ClassItem>() {
                public int compare(ClassItem o1, ClassItem o2) {
                    if (o1.getDate() == null || o2.getDate() == null)
                        return 0;
                    return o2.getDate().compareTo(o1.getDate());
                }
            });

        }
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    protected void onDestroy(){

        super.onDestroy();

        if(isTaskRoot()){

            //clean the file cache with advance option
            long triggerSize = 6000000; //starts cleaning when cache size is larger than 3M
            long targetSize = 5000000;      //remove the least recently used files until cache size is less than 2M
            AQUtility.cleanCacheAsync(this, triggerSize, targetSize);
        }

    }

    @Override
    public void onRefresh() {
        getFeeds();
    }
}
