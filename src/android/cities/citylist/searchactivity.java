package com.nc.rac.cities.citylist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.nc.rac.R;
import com.nc.rac.cities.helper.ContactsHelper;
import com.nc.rac.cities.model.Contacts;

import java.util.ArrayList;

/**
 * Created by gugalor
 */
public class searchactivity extends Activity {
    ListView searchresult;
    private EditText input;
    private ImageButton clear,left;
    private SQLiteDatabase database;
    int resId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchlayout);
        Intent whichView = getIntent();
        if(whichView!=null){
            resId = whichView.getIntExtra("vid", 0);
        }
        searchresult = (ListView) findViewById(R.id.searchresult);
        input = (EditText) findViewById(R.id.input);
        clear = (ImageButton) findViewById(R.id.clear);
        left=(ImageButton)findViewById(R.id.left_title_button);
        database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input.setText("");
            }
        });
        final CitysearchAdapter adapter = new CitysearchAdapter(ContactsHelper.mSearchContacts, this);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchresult.setAdapter(adapter);
        searchresult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final Contacts cityModel = (Contacts) ContactsHelper.mSearchContacts.get(position);
    			Setting.Save2SharedPreferences(searchactivity.this, "city",
    					cityModel.getName());
                Intent intent =new Intent();
                intent.putExtra("city",cityModel.getName());
                if (resId != 0) {
                    intent.putExtra("vid", resId);//
                }
                setResult(RESULT_OK,intent);
    			finish();

            }
        });

        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String curCharacter=s.toString().trim();

                if(TextUtils.isEmpty(curCharacter)){
                    clear.setVisibility(View.INVISIBLE);
                    ContactsHelper.getInstance().parseQwertyInputSearchContacts(null);
                }else{
                    clear.setVisibility(View.VISIBLE);
                    ContactsHelper.getInstance().parseQwertyInputSearchContacts(curCharacter);
                }
                if(ContactsHelper.mSearchContacts.size()==0){
                    searchresult.setAdapter(new CitysearchNonAdapter(searchactivity.this));
                }else {
                    searchresult.setAdapter(adapter);
                    adapter.refresh(ContactsHelper.mSearchContacts);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

        });


    }
    /**
     * 从数据库获取城市数据
     *
     * @return
     */
    private ArrayList<CityModel> getCityNames(String string)
    {
        ArrayList<CityModel> names = new ArrayList<CityModel>();
        Cursor cursor = database.rawQuery("SELECT CityName FROM T_City where CityName like '%"+string+"%' ORDER BY NameSort", null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            CityModel cityModel = new CityModel();
            cityModel.setCityName(cursor.getString(cursor.getColumnIndex("CityName")));
            names.add(cityModel);
        }
        return names;
    }


}
