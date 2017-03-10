package com.bracketcove.postrainer.reminderlist;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.bracketcove.postrainer.R;
import com.bracketcove.postrainer.reminderdetail.ReminderDetailFragment;
import com.bracketcove.postrainer.util.ActivityUtils;

public class ReminderListActivity extends AppCompatActivity {
    private static final String FRAG_REMINDER_LIST = "FRAG_REMINDER_LIST";

    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);
        //Toolbar
       // toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

        ReminderListFragment fragment =  (ReminderListFragment) manager.findFragmentByTag(FRAG_REMINDER_LIST);

        if (fragment == null){
            fragment = ReminderListFragment.newInstance();
        }

        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                fragment,
                R.id.cont_reminder_list_fragment,
                FRAG_REMINDER_LIST
        );
    }

}
