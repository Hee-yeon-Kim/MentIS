package com.imslab.mentis;

import android.content.Context;
import android.os.Bundle;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.support.design.widget.TabLayout;

public class GraphActivity extends AppCompatActivity {

    private boolean isFragmentB = true ;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    public static Context context_GraphView; // context 변수 선언

    // ... 코드 계속
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        context_GraphView = this; // onCreate에서 this 할당
        Button backbutton = (Button) findViewById(R.id.backbutton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTabLayout = (TabLayout) findViewById(R.id.layout_tab);
        mTabLayout.addTab(mTabLayout.newTab().setText("ECG PATCH"));
        mTabLayout.addTab(mTabLayout.newTab().setText("E4 BAND"));

        mViewPager = (ViewPager) findViewById(R.id.pager_content);
        GraphPagerAdapter mContentsPagerAdapter = new GraphPagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());

        mViewPager.setAdapter(mContentsPagerAdapter);

        mViewPager.addOnPageChangeListener(  new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override

            public void onTabSelected(TabLayout.Tab tab) {

                mViewPager.setCurrentItem(tab.getPosition());


            }

            @Override

            public void onTabUnselected(TabLayout.Tab tab) {



            }



            @Override

            public void onTabReselected(TabLayout.Tab tab) {



            }

        });

    }

    @Override
    protected  void onStop()
    {

        super.onStop();
        ((MainActivity)MainActivity.context_main).toggleGraphView(3,null);

    }


}
