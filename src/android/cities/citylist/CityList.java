package com.nc.rac.cities.citylist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.nc.rac.R;
import com.nc.rac.cities.adapter.HotCityGridAdapter;
import com.nc.rac.cities.helper.ContactsHelper;
import com.nc.rac.utils.AMapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 鍩庡競閫夋嫨
 *
 * @author gugalor
 */
public class CityList extends Activity implements AMapLocationListener {
    private BaseAdapter adapter;
    private ListView mCityLit;
    private TextView overlay, citysearch;
    private ImageButton backbutton;
    private MyLetterListView letterListView;
    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;
    private Handler handler;
    private OverlayThread overlayThread;
    private SQLiteDatabase database;
    private ArrayList<CityModel> mCityNames;
    private View city_locating_state;
    private View city_locate_failed;
    private TextView city_locate_state;
    private ProgressBar city_locating_progress;
    private ImageView city_locate_success_img;


    View hotcityall;

    String[] hotcity = new String[]{"北京市", "上海市", "深圳市", "广州市"};
    WindowManager windowManager;


    //定位
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    boolean locCacheable = true; //开启缓存
    boolean locNeedAddr = true; //需要地址信息
    boolean locGPSFirst = false; //GPS优先
    long intervalTime = 60*1000; //持续定位间隔时间（毫秒）

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater localLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View city_layout = localLayoutInflater.inflate(R.layout.public_cityhot, null);
        setContentView(city_layout);
        citysearch = (TextView) city_layout.findViewById(R.id.city_search_edittext);
        backbutton = (ImageButton) city_layout.findViewById(R.id.title_left_btn);
        mCityLit = (ListView) city_layout.findViewById(R.id.public_allcity_list);
        letterListView = (MyLetterListView) city_layout.findViewById(R.id.cityLetterListView);

        View cityhot_header_blank = localLayoutInflater.inflate(R.layout.public_cityhot_header_padding_blank, mCityLit, false);
        mCityLit.addHeaderView(cityhot_header_blank, null, false);
        cityhot_header_blank = localLayoutInflater.inflate(R.layout.city_locate_layout, mCityLit, false);
        city_locating_state = cityhot_header_blank.findViewById(R.id.city_locating_state);
        city_locate_state = ((TextView) cityhot_header_blank.findViewById(R.id.city_locate_state));
        city_locating_progress = ((ProgressBar) cityhot_header_blank.findViewById(R.id.city_locating_progress));
        city_locate_success_img = ((ImageView) cityhot_header_blank.findViewById(R.id.city_locate_success_img));
        city_locate_failed = cityhot_header_blank.findViewById(R.id.city_locate_failed);
        mCityLit.addHeaderView(cityhot_header_blank);

        View hotheadview = localLayoutInflater.inflate(R.layout.public_cityhot_header_padding, mCityLit, false);
        mCityLit.addHeaderView(hotheadview, null, false);
        hotcityall = localLayoutInflater.inflate(R.layout.public_cityhot_allcity, mCityLit, false);
        final GridView localGridView = (GridView) hotcityall.findViewById(R.id.public_hotcity_list);

        mCityLit.addHeaderView(hotcityall);
        HotCityGridAdapter adapter = new HotCityGridAdapter(this, Arrays.asList(hotcity));
        localGridView.setAdapter(adapter);
        localGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cityModel = parent.getAdapter()
                        .getItem(position).toString();
                Setting.Save2SharedPreferences(CityList.this, "city",
                        cityModel);
                Intent intent = new Intent();
                intent.putExtra("city", cityModel);
                intent.putExtra("cityResult", "true");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        city_locating_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityModel = city_locate_state.getText().toString();
                if (cityModel.equals("定位失败")){
                    cityModel="";
                }
                Setting.Save2SharedPreferences(CityList.this, "city",
                        cityModel);
                Intent intent = new Intent();
                intent.putExtra("city", cityModel);
                intent.putExtra("cityResult", "true");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        loadLocation();


        DBManager dbManager = new DBManager(this);
        dbManager.openDateBase();
        dbManager.closeDatabase();
        database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/"
                + DBManager.DB_NAME, null);
        mCityNames = getCityNames();
        database.close();
        letterListView
                .setOnTouchingLetterChangedListener(new LetterListViewListener());
        alphaIndexer = new HashMap<String, Integer>();
        handler = new Handler();
        overlayThread = new OverlayThread();
        initOverlay();
        setAdapter(mCityNames);
        mCityLit.setOnItemClickListener(new CityListOnItemClick());
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        citysearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean startLoad = ContactsHelper.getInstance().startLoadContacts();
                Intent intent = new Intent(CityList.this, searchactivity.class);
                startActivityForResult(intent, 2);
                return false;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                String city = data.getStringExtra("city");
                Intent intent = new Intent();
                intent.putExtra("city", city);
                intent.putExtra("cityResult", "true");
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                break;
        }
    }

    /**
     * 鑾峰彇浣嶇疆
     */
    public void loadLocation() {
        city_locate_failed.setVisibility(View.GONE);
        city_locate_state.setVisibility(View.VISIBLE);
        city_locating_progress.setVisibility(View.GONE);
//        city_locating_progress.setVisibility(View.VISIBLE);
        city_locate_success_img.setVisibility(View.GONE);

        // 定位
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(this);
        // 持续定位
        locationOption.setOnceLocation(true);
        initOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
        mHandler.sendEmptyMessage(AMapUtils.MSG_LOCATION_START);

//        city_locate_state.setText(getString(R.string.locating));
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                city_locate_failed.setVisibility(View.VISIBLE);
//                city_locate_state.setVisibility(View.GONE);
//                city_locating_progress.setVisibility(View.GONE);
//                city_locate_success_img.setVisibility(View.VISIBLE);
//                city_locate_state.setText("北京");
//            }
//        },2000);
//        if (locationClient == null) {
//            locationClient = new LocationClient(CityList.this);
//            locationClient.registerLocationListener(new LocationListenner());
//            LocationClientOption option = new LocationClientOption();
//            option.setAddrType("all");
//            option.setOpenGps(true);
//            option.setCoorType("bd09ll");
//            option.setScanSpan(2000);
//            locationClient.setLocOption(option);
//        }
//
//        locationClient.start();
//        locationClient.requestLocation();
    }

//    /**
//     * 鐩戝惉鍑芥暟锛屽張鏂颁綅缃殑鏃跺�欙紝鏍煎紡鍖栨垚瀛楃涓诧紝杈撳嚭鍒板睆骞曚腑
//     */
//    private class LocationListenner implements BDLocationListener {
//        public void onReceiveLocation(BDLocation location) {
//            city_locating_progress.setVisibility(View.GONE);
//
//            if (location != null) {
//
//                if (location.getCity() != null
//                        && !location.getCity().equals("")) {
//                    locationClient.stop();
//                    city_locate_failed.setVisibility(View.GONE);
//                    city_locate_state.setVisibility(View.VISIBLE);
//                    city_locating_progress.setVisibility(View.GONE);
//                    city_locate_success_img.setVisibility(View.VISIBLE);
//                    city_locate_state.setText(location.getCity());
//
//                } else {
//                    city_locating_state.setVisibility(View.GONE);
//                    city_locate_failed.setVisibility(View.VISIBLE);
//                }
//            } else {
//                // 瀹氫綅澶辫触
//                city_locating_state.setVisibility(View.GONE);
//                city_locate_failed.setVisibility(View.VISIBLE);
//            }
//
//        }
//    }

    /**
     * @return
     */
    private ArrayList<CityModel> getCityNames() {
        ArrayList<CityModel> names = new ArrayList<CityModel>();
        Cursor cursor = database.rawQuery(
                "SELECT * FROM T_City ORDER BY NameSort", null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            CityModel cityModel = new CityModel();
            cityModel.setCityName(cursor.getString(cursor
                    .getColumnIndex("CityName")));
            cityModel.setNameSort(cursor.getString(cursor
                    .getColumnIndex("NameSort")));
            names.add(cityModel);
        }
        cursor.close();
        return names;
    }

    /**
     * 斜 1/4
     *
     * @author
     */
    class CityListOnItemClick implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                long arg3) {
            CityModel cityModel = (CityModel) mCityLit.getAdapter()
                    .getItem(pos);
            if (cityModel != null) {
                Setting.Save2SharedPreferences(CityList.this, "city",
                        cityModel.getCityName());
                Intent intent = new Intent();
                intent.putExtra("city", cityModel.getCityName());
                intent.putExtra("cityResult", "true");
//                intent.putExtra("city", cityModel);
                setResult(RESULT_OK, intent);
                finish();
            }
        }

    }

    /**
     * ListView
     *
     * @param list
     */
    private void setAdapter(List<CityModel> list) {
        if (list != null) {
            adapter = new ListAdapter(this, list);
            mCityLit.setAdapter(adapter);
        }

    }

    /**
     * ListViewAdapter
     *
     * @author gugalor
     */
    private class ListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<CityModel> list;

        public ListAdapter(Context context, List<CityModel> list) {

            this.inflater = LayoutInflater.from(context);
            this.list = list;
            alphaIndexer = new HashMap<String, Integer>();
            sections = new String[list.size()];

            for (int i = 0; i < list.size(); i++) {
                //
                // getAlpha(list.get(i));
                String currentStr = list.get(i).getNameSort();
                //
                String previewStr = (i - 1) >= 0 ? list.get(i - 1)
                        .getNameSort() : " ";
                if (!previewStr.equals(currentStr)) {
                    String name = list.get(i).getNameSort();
                    alphaIndexer.put(name, i);
                    sections[i] = name;
                }
            }

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.public_cityhot_item,
                        null);
                holder = new ViewHolder();
                holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
                holder.name = (TextView) convertView
                        .findViewById(R.id.public_cityhot_item_textview);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(list.get(position).getCityName());
            String currentStr = list.get(position).getNameSort();
            String previewStr = (position - 1) >= 0 ? list.get(position - 1)
                    .getNameSort() : " ";
            if (!previewStr.equals(currentStr)) {
                holder.alpha.setVisibility(View.VISIBLE);
                holder.alpha.setText(currentStr);
            } else {
                holder.alpha.setVisibility(View.GONE);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView alpha;
            TextView name;
        }

    }

    // 鈥�
    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        overlay = (TextView) inflater.inflate(R.layout.overlay, null);
        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlay, lp);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        windowManager.removeView(overlay);

        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    private class LetterListViewListener implements
            MyLetterListView.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            if (alphaIndexer.get(s) != null) {
                int position = alphaIndexer.get(s);
                mCityLit.setSelection(position);
                overlay.setText(sections[position]);
                overlay.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                // 校overlay
                handler.postDelayed(overlayThread, 1500);
            }
        }

    }

    // overlay
    private class OverlayThread implements Runnable {

        @Override
        public void run() {
            overlay.setVisibility(View.GONE);
        }

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (null != aMapLocation) {
            Message msg = mHandler.obtainMessage();
            msg.obj = aMapLocation;
            msg.what = AMapUtils.MSG_LOCATION_FINISH;
            mHandler.sendMessage(msg);
        }
    }

    Handler mHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                //开始定位
                case AMapUtils.MSG_LOCATION_START:
                    city_locate_state.setText("正在定位...");
                    break;
                // 定位完成
                case AMapUtils.MSG_LOCATION_FINISH:
                    AMapLocation loc = (AMapLocation) msg.obj;
                    String result = AMapUtils.getLocationCity(loc);
                    city_locate_state.setText(result);
                    break;
//                //停止定位
//                case AMapUtils.MSG_LOCATION_STOP:
//                    travel_from2.setText("定位停止");
//                    break;
                default:
                    break;
            }
        };
    };

    // 根据控件的选择，重新设置定位参数
    private void initOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(locNeedAddr);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(locGPSFirst);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(locCacheable);
        // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
        locationOption.setInterval(intervalTime);
    }

}
