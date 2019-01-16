package com.vitec.task.smartrule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.UseIntructionAdapter;
import com.vitec.task.smartrule.bean.IntructionItem;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.db.BleDataDbHelper;

import java.util.ArrayList;
import java.util.List;

public class UseInstructionActivity extends BaseActivity {

    private ListView lvIntruction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_instruction);

        initView();
    }

    private void initView() {
        initWidget();
        setTvTitle("使用说明");
        lvIntruction = findViewById(R.id.lv_intruction);
        List<IntructionItem> itemList = new ArrayList<>();
        IntructionItem intructionItem = new IntructionItem();
        intructionItem.setIntruction("介绍啊啊啊啊啊啊a");
        intructionItem.setTitle("靠尺说明");
        itemList.add(intructionItem);

        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
        List<RulerOptions> optionsList = bleDataDbHelper.queryOptionsAllDataFromSqlite("");
        if (optionsList.size() > 0) {
            for (int i=0;i<optionsList.size();i++) {
                IntructionItem item = new IntructionItem();
                item.setTitle(optionsList.get(i).getOptionsName());
                item.setIntruction(optionsList.get(i).getMethods());
                itemList.add(item);
            }
        }
        bleDataDbHelper.close();
        UseIntructionAdapter adapter = new UseIntructionAdapter(UseInstructionActivity.this, itemList);
        lvIntruction.setAdapter(adapter);
     }
}
