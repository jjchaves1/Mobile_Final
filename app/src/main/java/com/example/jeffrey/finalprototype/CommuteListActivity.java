package com.example.jeffrey.finalprototype;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import com.example.jeffrey.finalprototype.Content.Commute;
import com.example.jeffrey.finalprototype.Content.WeeklyInfo;

import java.util.LinkedList;
import java.util.List;

import database.CommuteBaseHelper;
import database.CommuteDbSchema;
import database.CommuteDbSchema.CommuteTable;

import static com.example.jeffrey.finalprototype.Content.addItem;

/**
 * An activity representing a list of Commutes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CommuteDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CommuteListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static final int NEW_COMMUTE_REQUEST = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commute_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        final Intent addCommuteIntent = new Intent(this, AddNewCommute.class);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(addCommuteIntent, NEW_COMMUTE_REQUEST);
            }
        });

        View recyclerView = findViewById(R.id.commute_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.commute_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == NEW_COMMUTE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("Inside of RESULT_OK for new commute request");
                String name = data.getStringExtra("id");
                int arrHour = data.getIntExtra("arr_hour", 12);
                int arrMin = data.getIntExtra("arr_min", 0);
                int prepMins = data.getIntExtra("prep_mins", 0);
                String destination = data.getStringExtra("destination");

                boolean sunday = data.getBooleanExtra("sunday", false);
                boolean monday = data.getBooleanExtra("monday", false);
                boolean tuesday = data.getBooleanExtra("tuesday", false);
                boolean wednesday = data.getBooleanExtra("wednesday", false);
                boolean thursday = data.getBooleanExtra("thursday", false);
                boolean friday = data.getBooleanExtra("friday", false);
                boolean saturday = data.getBooleanExtra("saturday", false);
                boolean repeat = data.getBooleanExtra("repeat", false);

                WeeklyInfo w = makeWeek(sunday, monday, tuesday, wednesday, thursday, friday,
                            saturday, repeat);

                Commute newCommute = new Commute(name, destination, arrHour, arrMin, prepMins, w);
                addItem(newCommute);

                View recyclerView = findViewById(R.id.commute_list);
                assert recyclerView != null;
                setupRecyclerView((RecyclerView) recyclerView);
            } else {
                System.out.println("Inside of RESULT_NOT_OK for new commute request");

            }
        }
    }

    public WeeklyInfo makeWeek(boolean su, boolean m, boolean tu, boolean w, boolean th,
                               boolean f, boolean sa, boolean r){
        boolean[] week = new boolean[7];
        week[0] = su;
        week[1] = m;
        week[2] = tu;
        week[3] = w;
        week[4] = th;
        week[5] = f;
        week[6] = sa;
        return new WeeklyInfo(week, r);
    }

    @Override
    protected void onPause(){
        super.onPause();

        Context mContext = getApplicationContext();
        SQLiteDatabase mDatabase = new CommuteBaseHelper(mContext).getWritableDatabase();

//        for (Commute commute : Content.ITEMS){
//            ContentValues values = getContentValues(commute);
//            mDatabase.insert(CommuteTable.NAME, null, values);
//        }
    }

    private static ContentValues getContentValues(Commute commute){
        ContentValues values = new ContentValues();
        values.put(CommuteTable.Cols.ID, commute.id);
        values.put(CommuteTable.Cols.ARR_HOUR, commute.arrivalTimeHour);
        values.put(CommuteTable.Cols.ARR_MIN, commute.arrivalTimeMin);
        values.put(CommuteTable.Cols.PREP_MINS, commute.preparationTime);
        values.put(CommuteTable.Cols.DESTINATION, commute.destination);
        return values;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(Content.ITEMS));
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Commute> mValues;

        public SimpleItemRecyclerViewAdapter(List<Commute> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.commute_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
//            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(holder.mItem.id);
            holder.mAlarmSwitch.setChecked(holder.mItem.alarmArmed);
            // Set the color of the Selected days of the alarm
            if (holder.mItem.weekInfo.repeat == true) {
                holder.mAlarmRepeat.setColorFilter(Color.parseColor("#ff4081"));
            } else {
                holder.mAlarmRepeat.setColorFilter(Color.GRAY);
            }
            for (int i = 0; i < holder.mItem.weekInfo.days.length; i ++) {
                if (holder.mItem.weekInfo.days[i]) {
                    holder.mDayList.get(i).setTextColor(Color.parseColor("#ff4081"));
                } else {
                    holder.mDayList.get(i).setTextColor(Color.GRAY);
                }
            }
            holder.mAlarmSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Setting the colors
                    if (holder.mItem.weekInfo.repeat == true) {
                        holder.mAlarmRepeat.setColorFilter(Color.parseColor("#ff4081"));
                    } else {
                        holder.mAlarmRepeat.setColorFilter(Color.GRAY);
                    }

                    for (int i = 0; i < holder.mItem.weekInfo.days.length; i ++) {
                        if (holder.mAlarmSwitch.isChecked()) {
                            if (holder.mItem.weekInfo.days[i]) {
                                holder.mDayList.get(i).setTextColor(Color.parseColor("#ff4081"));
                            } else {
                                holder.mDayList.get(i).setTextColor(Color.GRAY);
                            }
                        } else {
                            holder.mAlarmRepeat.setColorFilter(Color.LTGRAY);
                            holder.mDayList.get(i).setTextColor(Color.LTGRAY);
                        }
                    }
                    // Now we need to Set or cancel the alarm TODO
                    if (holder.mAlarmSwitch.isChecked()) {
                        // Set Alarm
                    } else {
                        // Cancel Alarm
                    }
                }
            });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(CommuteDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        CommuteDetailFragment fragment = new CommuteDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.commute_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, CommuteDetailActivity.class);
                        intent.putExtra(CommuteDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
//            final TextView mIdView;
            final TextView mContentView;
            final Switch mAlarmSwitch;
            final ImageView mAlarmRepeat;
            final TextView mSunday, mMonday, mTuesday, mWednesday, mThursday, mFriday, mSaturday;
            final LinkedList<TextView> mDayList = new LinkedList<>();
            Commute mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
//                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
                mAlarmSwitch = (Switch) view.findViewById(R.id.alarmSwitch);
                mAlarmRepeat = (ImageView) view.findViewById(R.id.alarmRepeat);
                mSunday = (TextView) view.findViewById(R.id.textViewSun);
                mMonday = (TextView) view.findViewById(R.id.textViewMon);
                mTuesday = (TextView) view.findViewById(R.id.textViewTues);
                mWednesday = (TextView) view.findViewById(R.id.textViewWed);
                mThursday = (TextView) view.findViewById(R.id.textViewThur);
                mFriday = (TextView) view.findViewById(R.id.textViewFri);
                mSaturday = (TextView) view.findViewById(R.id.textViewSat);
                mDayList.add(mSunday);
                mDayList.add(mMonday);
                mDayList.add(mTuesday);
                mDayList.add(mWednesday);
                mDayList.add(mThursday);
                mDayList.add(mFriday);
                mDayList.add(mSaturday);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
