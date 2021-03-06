package com.forbitbd.financrr.ui.finance;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.ViewPager;

import com.forbitbd.androidutils.models.Project;
import com.forbitbd.androidutils.utils.Constant;
import com.forbitbd.androidutils.utils.PrebaseActivity;
import com.forbitbd.androidutils.utils.ViewPagerAdapter;
import com.forbitbd.financrr.R;
import com.forbitbd.financrr.models.Account;
import com.forbitbd.financrr.models.TransactionResponse;
import com.forbitbd.financrr.ui.finance.account.AccountFragment;
import com.forbitbd.financrr.ui.finance.accountAdd.AddAccountFragment;
import com.forbitbd.financrr.ui.finance.report.FinanceReportActivity;
import com.forbitbd.financrr.ui.finance.transaction.TransactionFragment;
import com.forbitbd.financrr.ui.finance.transactionAdd.TransactionAddActivity;
import com.forbitbd.financrr.ui.finance.transactionUpdate.TransactionUpdateActivity;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class FinanceActivity extends PrebaseActivity implements FinanceContract.View, View.OnClickListener {

    private FinancePresenter mPresenter;

    private static final int TRANSACTION_ADD=8000;
    private static final int TRANSACTION_UPDATE_DELETE=10000;

    private Project project;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private SearchView searchView;

    private FloatingActionButton fabCreateAccount,fabAddTransaction,fabReport,fabDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

        this.project = (Project) getIntent().getSerializableExtra(Constant.PROJECT);

        mPresenter = new FinancePresenter(this);

        initView();
    }

    private void initView() {
        setupToolbar(R.id.toolbar);
        getSupportActionBar().setTitle(getString(R.string.finance));


        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeQueryText(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fabCreateAccount = findViewById(R.id.fab_add_account);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        fabReport = findViewById(R.id.fab_report);
        fabDownload = findViewById(R.id.fab_download);

        fabCreateAccount.setOnClickListener(this);
        fabAddTransaction.setOnClickListener(this);
        fabReport.setOnClickListener(this);
        fabDownload.setOnClickListener(this);
    }

    private void changeQueryText(int position) {
        switch (position){
            case 0:
                searchView.setQueryHint("Search Account by Name");
                break;

            case 1:
                searchView.setQueryHint("Search by Invoice or Purpose");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem searchMenuItem =  menu.findItem(R.id.search);

        Drawable drawable = searchMenuItem.getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this,android.R.color.white));
        searchMenuItem.setIcon(drawable);

        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("Search Account by Name");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mPresenter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mPresenter.filter(newText);
                return false;
            }
        });

        return true;
    }

    private void setupViewPager(ViewPager viewPager) {

        if(pagerAdapter==null){
            pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        }
        pagerAdapter.addFragment(new AccountFragment(), "Accounts");
        pagerAdapter.addFragment(new TransactionFragment(), "Transactions");

        viewPager.setAdapter(pagerAdapter);

    }


    @Override
    public Project getProject() {
        return this.project;
    }

    @Override
    public void filter(String query) {

        switch (viewPager.getCurrentItem()){
            case 0:

                AccountFragment af = (AccountFragment) pagerAdapter.getItem(viewPager.getCurrentItem());
                af.filter(query);
                break;

            case 1:

                TransactionFragment tf = (TransactionFragment) pagerAdapter.getItem(viewPager.getCurrentItem());
                tf.filter(query);
                break;
        }


    }

    @Override
    public void showAccountAddDialog() {
        AddAccountFragment addAccountFragment = new AddAccountFragment();
        addAccountFragment.setCancelable(false);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.PROJECT,getProject());
        addAccountFragment.setArguments(bundle);

        addAccountFragment.show(getSupportFragmentManager(),"JJJJ");
    }

    @Override
    public void addAccount(Account account) {
        AccountFragment af = (AccountFragment) pagerAdapter.getItem(0);
        af.addAccount(account);
    }

    @Override
    public void startAddTransactionActivity() {
        Intent intent = new Intent(getApplicationContext(), TransactionAddActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.PROJECT,project);
        intent.putExtras(bundle);
        startActivityForResult(intent,TRANSACTION_ADD);
    }

    @Override
    public void startFinanceReportActivity() {
        Intent intent = new Intent(getApplicationContext(), FinanceReportActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.PROJECT,project);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void removeRelatedTransactions(Account account) {
        TransactionFragment transactionFragment = (TransactionFragment) pagerAdapter.getItem(1);
        transactionFragment.removeRelatedTransactions(account);
    }

    @Override
    public void startUpdateOrDeleteActivity(TransactionResponse transaction) {
        Intent intent = new Intent(getApplicationContext(), TransactionUpdateActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.PROJECT,project);
        bundle.putSerializable(Constant.TRANSACTION,transaction);

        intent.putExtras(bundle);

        startActivityForResult(intent,TRANSACTION_UPDATE_DELETE);
    }

    @Override
    public void onClick(View view) {

        if(view==fabCreateAccount){
            mPresenter.showAccountAddDialog();
        }else if(view==fabAddTransaction){
            mPresenter.startAddTransactionActivity();
        }else if(view==fabReport){
            mPresenter.startFinanceReportActivity();
        }else if(view==fabDownload){

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode,  resultCode, data);

        if(requestCode==TRANSACTION_ADD && resultCode== Activity.RESULT_OK){
            TransactionResponse transaction = (TransactionResponse) data.getSerializableExtra(Constant.TRANSACTION);

            if(transaction!=null){
                TransactionFragment tf = (TransactionFragment) pagerAdapter.getItem(1);
                tf.addTransaction(transaction);
            }
        }

        if(requestCode==TRANSACTION_UPDATE_DELETE && resultCode== Activity.RESULT_OK){
            TransactionResponse transaction = (TransactionResponse) data.getSerializableExtra(Constant.TRANSACTION);

            String status = data.getStringExtra(Constant.STATUS);

            TransactionFragment tf = (TransactionFragment) pagerAdapter.getItem(1);

            if(status.equals(Constant.UPDATE)){
                tf.update(transaction);
            }else if(status.equals(Constant.DELETE)){
                tf.remove(transaction);
            }

        }
    }
}
