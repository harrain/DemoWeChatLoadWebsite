package com.example.demowechat;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.StringLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.zip.Inflater;


/**
 * 加载列表的fragment.
 */
public class ConverFragment extends Fragment {

    Context mContext;

    ListView lv;

    List<WebsiteBean> list ;
    Stack<Pic> pics;
    RecyclerView rl;
    GalaryInfoAdapter adapter;

    public ConverFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conver,null,false);
        mContext = getContext();

        pics = new Stack<>();

        rl = (RecyclerView) view.findViewById(R.id.rl);
//        GridLayoutManager layoutManager = new GridLayoutManager(mContext,3);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rl.setLayoutManager(layoutManager);
        adapter = new GalaryInfoAdapter(mContext,pics);
        rl.setAdapter(adapter);
        rl.addItemDecoration(new MyDecoration(mContext, MyDecoration.VERTICAL_LIST));

//        lv = (ListView) view.findViewById(R.id.lv);
//        list= new ArrayList<>();
//
//        loadData();
//
////        for (int i = 0; i < 30; i++) {
////            list.add(++i);
////        }
//
//        lv.setAdapter(new LvAdapter());
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(mContext, WebsiteShowActivity.class);
//                intent.putExtra("title",list.get(position).getWebsiteTitle());
//                intent.putExtra("text",list.get(position).getWebsiteText());
//                startActivity(intent);
//            }
//        });
        return view;
    }

    public void addUri(Uri uri,String time){

        Pic pic = new Pic(uri,time);
        pics.push(pic);

        adapter.notifyDataSetChanged();
    }

    public int getImageCount(){
        return pics.size();
    }

    private void loadData() {
        WebsiteBean baiduWb = new WebsiteBean("http://img0.bdstatic.com/static/searchresult/img/logo-2X_b99594a.png","https://www.baidu.com/","百度");//baidu
        WebsiteBean bitCoin = new WebsiteBean("https://bitcoin.org/favicon.png","https://bitcoin.org/en/","bitCoin");//bitcoin
        WebsiteBean bing = new WebsiteBean("http://cn.bing.com/sa/simg/bing_p_rr_teal_min.ico","http://cn.bing.com/","必应");//bing
        WebsiteBean github = new WebsiteBean("https://github.com/fluidicon.png","https://github.com/","github");
        WebsiteBean juejin = new WebsiteBean("https://gold-cdn.xitu.io/v3/static/img/logo.a7995ad.svg","https://juejin.im/","掘金");
        WebsiteBean jianshu = new WebsiteBean("http://cdn2.jianshu.io/assets/web/logo-58fd04f6f0de908401aa561cda6a0688.png","http://www.jianshu.com/","简书");

        WebsiteBean csdn = new WebsiteBean("http://c.csdnimg.cn/public/favicon.ico","http://www.csdn.net/?ref=toolbar","csdn");//csdn
        WebsiteBean cto = new WebsiteBean("http://static2.51cto.com/51cto/cms/homepage/2015/images/logo.gif","http://www.51cto.com/","51cto");//51cto
        WebsiteBean imooc = new WebsiteBean("http://cn.bing.com/sa/simg/bing_p_rr_teal_min.ico","http://www.imooc.com/","慕课");//慕课
        WebsiteBean jike = new WebsiteBean("http://e.jikexueyuan.com/headerandfooter/images/logo.png","http://www.jikexueyuan.com/","极客学院");
        WebsiteBean zhihu = new WebsiteBean("","https://zhuanlan.zhihu.com/","知乎专栏");
        WebsiteBean as = new WebsiteBean("","http://www.android-studio.org/","Android Studio 中文社区");
        WebsiteBean bus = new WebsiteBean("http://www.apkbus.com/static/image/common/logo.png","http://www.apkbus.com/","安卓巴士");
        WebsiteBean osChina = new WebsiteBean("http://www.oschina.net/build/oschina/components/imgs/oschina.svg?t=1484580392000","http://www.oschina.net/","开源中国");
        WebsiteBean annet = new WebsiteBean("","http://www.hiapk.com/","安卓网");
        WebsiteBean develop = new WebsiteBean("https://developer.android.google.cn/_static/74dcb9f23a/images/android/favicon.png","https://developer.android.google.cn/develop/index.html","安卓开发者");

        list.add(baiduWb);
        list.add(bitCoin);
        list.add(bing);
        list.add(github);
        list.add(juejin);
        list.add(jianshu);

        list.add(csdn);
        list.add(cto);
        list.add(imooc);
        list.add(jike);
        list.add(zhihu);
        list.add(as);
        list.add(bus);
        list.add(osChina);
        list.add(annet);
        list.add(develop);

    }

//    class LvAdapter extends BaseAdapter{
//
//        @Override
//        public int getCount() {
//            return list.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return list.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHoler viewHoler;
//            if (convertView == null) {
//                View view = View.inflate(parent.getContext(), R.layout.list_item, null);
//                 viewHoler = new ViewHoler();
//                viewHoler.imageView = (ImageView) view.findViewById(R.id.iv);
//                viewHoler.textView = (TextView) view.findViewById(R.id.tv);
//                view.setTag(viewHoler);
//                convertView= view;
//            }else {
//                viewHoler = (ViewHoler) convertView.getTag();
//            }
//
//            try {
//                Glide.with(mContext).load(list.get(position).getPictureId()).into(viewHoler.imageView);
//                viewHoler.textView.setText(list.get(position).getWebsiteTitle());
//                viewHoler.content.setText(list.get(position).getWebsiteText());
////                Log.e("list",list.get(position).toString());
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//
//            return convertView;
//        }
//
//        class ViewHoler {
//            ImageView imageView;
//            TextView textView;
//            TextView content;
//        }
//    }


    class Pic{
        Uri uri;
        String data;

        public Pic(Uri uri, String data) {
            this.uri = uri;
            this.data = data;
        }

        public Uri getUri() {
            return uri;
        }

        public String getData() {
            return data;
        }
    }

    class WebsiteBean{
        String pictureId;
        String websiteText;
        String websiteTitle;
        String websiteContent;

        public WebsiteBean(String id,String text,String title){
            pictureId = id;
            websiteText = text;
            websiteTitle = title;
        }

        public String getPictureId() {
            return pictureId;
        }

        public void setPictureId(String pictureId) {
            this.pictureId = pictureId;
        }

        public String getWebsiteText() {
            return websiteText;
        }

        public void setWebsiteText(String websiteText) {
            this.websiteText = websiteText;
        }

        public String getWebsiteTitle() {
            return websiteTitle;
        }

        public void setWebsiteTitle(String websiteTitle) {
            this.websiteTitle = websiteTitle;
        }

        public String getWebsiteContent() {
            return websiteContent;
        }

        public void setWebsiteContent(String websiteContent) {
            this.websiteContent = websiteContent;
        }
    }

}
