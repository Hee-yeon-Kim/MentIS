package com.imslab.mentis;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ReportAdapter extends FragmentStatePagerAdapter {

    private int mPageCount;

    public ReportAdapter(FragmentManager fm, int pageCount) {

        super(fm);

        this.mPageCount = pageCount;

    }



    @Override
    public Fragment getItem(int position) {


        switch (position) {

            case 0:

//                SelfReport selfReport = new SelfReport();
//
//                return selfReport;
//
//
//            case 1:

                ingReport selfReport2 = new ingReport();

                return selfReport2;

            case 1:

                postReport selfReport3 = new postReport();

                return selfReport3;
          default:
                    return null;
        }


    }



    @Override
    public int getCount() {

        return mPageCount;

    }

}