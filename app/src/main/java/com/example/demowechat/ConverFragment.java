package com.example.demowechat;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.applibrary.AppConstant;
import com.example.applibrary.Link;
import com.example.applibrary.LogUtils;
import com.example.applibrary.widget.SwipeMenuRecyclerView;

import java.io.File;



/**
 * 加载列表的fragment.
 */
public class ConverFragment extends Fragment {

    Context mContext;

    ListView lv;

//    LinkedList<Pic> pics;
    Link<Pic> pics;
    SwipeMenuRecyclerView rl;
    GalaryInfoAdapter adapter;
    private static final String TAG = "ConverFragment";
    private boolean isOnCreate = true;

    public ConverFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conver,null,false);
        mContext = getActivity();
        pics = new Link<>();
        rl = (SwipeMenuRecyclerView) view.findViewById(R.id.rl);
//        GridLayoutManager layoutManager = new GridLayoutManager(mContext,3);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rl.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        rl.addItemDecoration(dividerItemDecoration);
//        rl.addItemDecoration(new MyDecoration(mContext, MyDecoration.VERTICAL_LIST));
        adapter = new GalaryInfoAdapter(mContext,pics);
        rl.setAdapter(adapter);

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

    @Override
    public void onStart() {
        super.onStart();
        if (isOnCreate){
            loadFromLocal();
            isOnCreate = false;
        }

        notifyDataSetChanged();
    }

    public void addUri(Uri uri, String time){

        File file = new File(uri.getPath());
        Log.e(TAG,"file size:"+file.length());
        Log.e(TAG,"file path:"+file.getAbsolutePath());
        Pic pic = new Pic(uri.getPath(),time,Formatter.formatFileSize(mContext,file.length()));
        pics.add(pic);

        adapter.notifyDataSetChanged();
    }

    public void addUri(Uri uri, String time,String size){

        Pic pic = new Pic(uri.getPath(),time,size);
        pics.add(pic);

        adapter.notifyDataSetChanged();
    }

    public int getImageCount(){
        return pics.size();
    }



    public void loadData(Uri uri, String time, String size) {
        Pic pic = new Pic(uri.getPath(),time,size);
        pics.add(pic);
    }

    public void loadData(Uri uri, String time, String size,String location) {
//        LogUtils.i("loadData",location);
        Pic pic = null;
        if (TextUtils.isEmpty(location)){
            pic = new Pic(uri.getPath(), time, size, "", "");
        }else {
            String[] split = location.split("-");
            pic = new Pic(uri.getPath(), time, size, split[0], split[1]);
        }
        pics.add(pic);
    }

    public void notifyDataSetChanged(){
        adapter.notifyDataSetChanged();
    }

    public void clearPics() {
        if (pics !=null && pics.size()>0) {
            pics.clean();
        }
    }

    public void addUri(Uri uri, String picTime, String longitude, String latitude) {
//        LogUtils.i("addUri",uri.toString());
        File file = new File(uri.toString());

        Pic pic = new Pic(uri.toString(),picTime,Formatter.formatFileSize(mContext,file.length()),longitude,latitude,false);
        pics.add(pic);
//        LogUtils.i("addUri",longitude+"-"+latitude);
//        Log.e(TAG,"weizhi----"+pics.indexOf(pic));

        adapter.notifyItemInserted(0);
        notifyDataSetChanged();
    }

    /**
     * 应用启动后从本地读取保存的照片，显示到列表上
     */
    private void loadFromLocal() {

        clearPics();
        try {
            File cacheDir = new File(AppConstant.KEY.IMG_DIR);
            Log.e(TAG, "file:" + cacheDir.getAbsolutePath());
            if (!cacheDir.exists()) {
                ((MainActivity)mContext).setToolbarTitle();
                return;
            }
            File[] files = cacheDir.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    Uri uri = Uri.fromFile(file);
                    loadData(uri, parseTime(file.getName()), caculateFileSize(file.length()), parseLocation(file.getName()));
//                Log.e(TAG, "pic:" + uri.getPath());
                }
            }
        }catch(Exception e){
            LogUtils.e(TAG,e.getMessage());
        }
        ((MainActivity)mContext).setToolbarTitle();//更新mainactivity的标题
        notifyDataSetChanged();
    }

    /**
     * 计算文件大小
     *
     * @param length
     * @return
     */
    private String caculateFileSize(long length) throws Exception{
        return Formatter.formatFileSize(mContext, length);
    }

    /**
     * 从文件名中解析出时间点
     */
    private String parseTime(String fileName) throws Exception{
//        Log.e(TAG, "parseTime:" + fileName.substring(0, 19));
        return fileName.substring(0, 19);

    }

    private String parseLocation(String name) throws Exception{
        if (name.indexOf(".jpeg") == 19) {
            return "";
        }
//        LogUtils.i("parseLocation", name.indexOf(".jpeg") + "");
//            LogUtils.i("parseLocation",name.substring(20,name.indexOf(".jpeg")));
        if (name.indexOf(".jpeg") > 20) {
            return name.substring(20, name.indexOf(".jpeg"));
        }
        return "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isOnCreate = true;
        pics = null;
        LogUtils.i(TAG,"onDestroyView");
    }

    class Pic{

        boolean isRead = true;

        String path;
        String data;
        String size;

        String longitude;
        String latitude;

        public Pic() {
        }



        public Pic(String path, String data, String size) {
            this.path = path;
            this.data = data;
            this.size = size;
            isRead = true;
        }

        public Pic(String path, String picTime, String size, String longitude, String latitude) {
            this.path = path;
            this.data = picTime;
            this.size = size;
            this.longitude = longitude;
            this.latitude = latitude;
            isRead = true;
        }

        public Pic(String path, String picTime, String size, String longitude, String latitude,boolean isRead) {
            this.path = path;
            this.data = picTime;
            this.size = size;
            this.longitude = longitude;
            this.latitude = latitude;
            this.isRead = isRead;
        }

        public String getPath() {
            return path;
        }

        public String getData() {
            return data;
        }

        public String getSize() {
            return size;
        }

        public String getLongitude() {
            return longitude;
        }

        public String getLatitude() {
            return latitude;
        }

        public boolean isRead() {
            return isRead;
        }

        public void setRead(boolean read) {
            isRead = read;
        }
    }


}
