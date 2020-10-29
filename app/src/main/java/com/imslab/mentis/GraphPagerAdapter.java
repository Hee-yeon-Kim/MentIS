package com.imslab.mentis;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class GraphPagerAdapter extends FragmentStatePagerAdapter {

    private int mPageCount;

    public GraphPagerAdapter(FragmentManager fm, int pageCount) {

        super(fm);

        this.mPageCount = pageCount;

    }



    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:

                ecgView ECGFragment = new ecgView();
                ((MainActivity)MainActivity.context_main).toggleGraphView(1,ECGFragment);
               // ((MainActivity)MainActivity.context_main).myToast("1이다.");
                return ECGFragment;



            case 1:

                e4View e4Fragment = new e4View();
                ((MainActivity)MainActivity.context_main).toggleGraphView(2,e4Fragment);
              //  ((MainActivity)MainActivity.context_main).myToast("2이다.");

                return e4Fragment;



            default:

                return null;

        }

    }



    @Override
    public int getCount() {

        return mPageCount;

    }

}