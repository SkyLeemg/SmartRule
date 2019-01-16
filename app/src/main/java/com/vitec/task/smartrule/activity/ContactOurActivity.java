package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.CompanyMessage;
import com.vitec.task.smartrule.bean.event.CompanayMsgEvent;
import com.vitec.task.smartrule.bean.event.CostomMsgEvent;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.AboutUserIntentService;
import com.vitec.task.smartrule.utils.HeightUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人中心--联系我们的界面
 */
public class ContactOurActivity extends BaseActivity implements View.OnClickListener{

//    private TextView tvNetAddress;//官网地址
//    private TextView tvNetMallAddress;//商城地址
//    private TextView tvPhone;//联系电话
//    private TextView tvEmail;//联系邮箱
    private EditText etSuggestion;//
    private TextView tvCount;//字数统计
    private Button btnSubmit;
    private ListView lvContent;
    private MKLoader mkLoader;

    private List<CompanyMessage> companyMessageList;
    private CompanayContentAdatper contentAdatper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_our);
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    /**
     * 还差一个后台接口，获取信息显示出来
     */
    private void initData() {
        companyMessageList = new ArrayList<>();
        companyMessageList = OperateDbUtil.queryCompanyMsgFromSqlite(getApplicationContext(), "");
        contentAdatper = new CompanayContentAdatper();
        lvContent.setAdapter(contentAdatper);
        Intent intent = new Intent(this, AboutUserIntentService.class);
        intent.putExtra(AboutUserIntentService.TYPE_FLAG, AboutUserIntentService.FLAG_GET_COMPANY_MESSAGE);
        startService(intent);
        HeightUtils.setListViewHeighBaseOnChildren(lvContent);

    }

    private void initView() {
        initWidget();
        setTvTitle("联系我们");
        etSuggestion = findViewById(R.id.et_suggestion);
        tvCount = findViewById(R.id.tv_count);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);
        btnSubmit.setClickable(false);
        etSuggestion.addTextChangedListener(suggestionTextWathcer);
        lvContent = findViewById(R.id.lv_com);
        mkLoader = findViewById(R.id.mkloader);

    }


    /**
     * 请求公司资料回调
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getCompanyMsgCallBack(CompanayMsgEvent event) {
        if (event.isSuccess()) {
            companyMessageList.clear();
            companyMessageList = (List<CompanyMessage>) event.getObject();
            contentAdatper.notifyDataSetChanged();
            HeightUtils.setListViewHeighBaseOnChildren(lvContent);
        }
    }


    /**
     * 提交意见回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void postAdivceCallBack(CostomMsgEvent event) {
        if (event.isSuccess()) {
            Toast.makeText(getApplicationContext(),"提交成功~感谢您的宝贵建议！",Toast.LENGTH_SHORT).show();
            etSuggestion.setText("");
        } else {
            Toast.makeText(getApplicationContext(),event.getMsg(),Toast.LENGTH_SHORT).show();
        }
        mkLoader.setVisibility(View.GONE);
    }

    private TextWatcher suggestionTextWathcer = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String count = etSuggestion.length() + "/" + 500;
            tvCount.setText(count);
            if (etSuggestion.getText().toString().length() > 5) {
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.selector_login_btn_click);
            } else {
                btnSubmit.setClickable(false);
                btnSubmit.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case  R.id.btn_submit:
                if (etSuggestion.getText().toString().trim().length() < 2) {
                    Toast.makeText(getApplicationContext(),"字数太少啦~",Toast.LENGTH_SHORT).show();
                    return;
                }
                mkLoader.setVisibility(View.VISIBLE);
                Bundle bundle = new Bundle();
                bundle.putString(NetConstant.post_submit_advice_content, etSuggestion.getText().toString());
                Intent intent = new Intent(getApplicationContext(), AboutUserIntentService.class);
                intent.putExtra(AboutUserIntentService.TYPE_FLAG, AboutUserIntentService.FLAG_POST_ADVICE);
                intent.putExtra(AboutUserIntentService.VALUE_BUNDLE, bundle);
                startService(intent);
            break;
        }
    }


    class CompanayContentAdatper extends BaseAdapter {

        @Override
        public int getCount() {
            return companyMessageList.size();
        }

        @Override
        public Object getItem(int i) {
            return companyMessageList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(ContactOurActivity.this).inflate(R.layout.item_list_view_company_msg, null);
                holder = new ViewHolder();
                holder.tvContent = view.findViewById(R.id.tv_content);
                holder.tvTitle = view.findViewById(R.id.tv_title);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.tvTitle.setText(companyMessageList.get(i).getName()+"：");
            holder.tvContent.setText(companyMessageList.get(i).getContent()+"");

            return view;
        }
    }

    class ViewHolder {
        TextView tvTitle;
        TextView tvContent;
    }
}
