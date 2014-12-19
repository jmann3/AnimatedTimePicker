package com.isobar.jmann.acceleratedpickupdropoff;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private ListView mListView;
    private AvailableTimesAdapter mArrayAdapter;
    private String[] mAavailableTimes = {"6:30 am", "7:00 am", "7:30 am", "8:00 am", "8:30 am", "9:00 am", "9:30 am", "10:00 am", "10:30 am", "11:00 am", "11:30 am", "12:00 pm", "12:30 pm",
            "1:00 pm", "1:30 pm", "2:00 pm"};
    private float[] gaussianAmplitudes = {1.0f, 1.25f, 1.5f, 2.0f, 1.5f, 1.25f, 1.0f};
    private static final long ANIMATION_DURATION = 100;
    private static int mCenterTopHeight = -1;
    private static int mCenterBottomHeight = -1;
    private static int mRowHeightPixels;
    private static int mStatusBarHeight;
    private static int mListViewTopHeight;

    private int mLastFirstVisibleItem = 0;
    private boolean mIsScrollingUp;
    private boolean mIsAligningRow = false;
    private long mPreviousEventTime;  // stop animations when scrolling too fast
    private int mCenterRowPosition = -1;
    private int mRowOffsetAtStart = -1;
    private int mCenterAlignCounter = 0;

    TextView mCenterRowTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // determine height of list row in pixels
        mRowHeightPixels =  (int)getResources().getDimension(R.dimen.time_row_height);

        // determine center row top and bottom y position after layout
        final RelativeLayout centerRow = (RelativeLayout)findViewById(R.id.center_row);
        centerRow.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                // determine statusbar height
                mStatusBarHeight = UIUtils.topOffset(MainActivity.this, findViewById(R.id.master_view));

                int[] positionTop = new int[2];
                centerRow.getLocationOnScreen(positionTop);

                mCenterTopHeight = positionTop[1];
                mCenterBottomHeight = mCenterTopHeight + mRowHeightPixels;

                int distanceCenterToList = (mCenterTopHeight - mListViewTopHeight);

                mRowOffsetAtStart = (distanceCenterToList % mRowHeightPixels);

                // create header/footer views
                RelativeLayout topHeader = (RelativeLayout)getLayoutInflater().inflate(R.layout.time_list_header, null);
                RelativeLayout bottomHeader = (RelativeLayout)getLayoutInflater().inflate(R.layout.time_list_footer, null);

                // set view heights
                ListView.LayoutParams layoutParams = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mCenterTopHeight - mStatusBarHeight);
                topHeader.setLayoutParams(layoutParams);
                bottomHeader.setLayoutParams(layoutParams);

                // add header/footer to list
                mListView.addHeaderView(topHeader);
                mListView.addFooterView(bottomHeader);


                centerRow.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // get pointer to center row's time
        mCenterRowTime = (TextView)findViewById(R.id.big_time);

        mListView = (ListView) findViewById(R.id.listView);
        mArrayAdapter = new AvailableTimesAdapter(this, R.layout.time_list_row, mAavailableTimes);
        mListView.setAdapter(mArrayAdapter);

        mListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] positionTop = new int[2];
                mListView.getLocationOnScreen(positionTop);

                mListViewTopHeight = positionTop[1];

                mListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                // TODO: settup list of views which have been animated
                //adapter.setAnimate(scrollState = SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL);

                // determine scroll direction
                final int currentFirstVisibleItem = view.getFirstVisiblePosition();

                if (currentFirstVisibleItem >= mLastFirstVisibleItem)
                    mIsScrollingUp = true;
                else
                    mIsScrollingUp = false;

                mLastFirstVisibleItem = currentFirstVisibleItem;

                if (scrollState == SCROLL_STATE_IDLE) {

                    // TODO: align row position so centered

                    if (mCenterAlignCounter == 1) {
                        mCenterAlignCounter = 0;
                        return;

                    } else {

                        // check if all of first row is visible
                        View firstVisibleRow = mListView.getChildAt(0);
                        View centerRow = mListView.getChildAt(mCenterRowPosition);

                        // calculate height in window of center row in list
                        // get coordinates of origin of row
                        int[] location = new int[2];
                        centerRow.getLocationOnScreen(location);

                        int centerDiff = (mCenterTopHeight - location[1]);

                        Log.d("CenterPosition", "mCenterRowPosition is " + mCenterRowPosition + ", first visible position is " + mListView.getFirstVisiblePosition());

                        mListView.post(new Runnable() {
                            @Override
                            public void run() {
                                mListView.smoothScrollToPositionFromTop(mCenterRowPosition + mListView.getFirstVisiblePosition(), mCenterTopHeight - mStatusBarHeight, 100);
                            }
                        });

                        mCenterAlignCounter++;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                long currTime = System.currentTimeMillis();
                long timeToScrollOneElement = currTime - mPreviousEventTime;
                double speed = ((double) 1 / timeToScrollOneElement) * 1000;

                mPreviousEventTime = currTime;

                if (speed <=80) {

                    //for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount - 1; i++) {
                    for (int i = 0; i < totalItemCount - 1; i++) {

                        // get each visible row
                        View rowView = mListView.getChildAt(i);

                        if (rowView != null && mCenterTopHeight != -1 && mCenterBottomHeight != -1) {

                            // get coordinates of origin of row
                            int[] location = new int[2];
                            rowView.getLocationOnScreen(location);

                            // determine if row overlaps center
                            int rowCenterY = (int) (location[1] + (mRowHeightPixels / 2));

                            //  look for center row
                            if (rowCenterY < mCenterBottomHeight && rowCenterY > mCenterTopHeight) {

                                mCenterRowPosition = i;

                                // expand rowView icon and restore prior icons
                                mArrayAdapter.expandAndRestore(rowView, speed);

                                // set time for center row
                                mCenterRowTime.setText(mAavailableTimes[((ViewHolder) rowView.getTag()).arrayPosition]);

                                break;
                            }
                        }
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * array adapter for list
     */
    private class AvailableTimesAdapter extends ArrayAdapter<String> {

        View mPreviouslyExpandedView;

        public AvailableTimesAdapter(Context context, int resource) {
            super(context, resource);
        }

        public AvailableTimesAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public String getItem(int position) {
            return mAavailableTimes[position];
        }

        @Override
        public int getCount() {
            return mAavailableTimes.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {

                // inflate the layout
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.time_list_row, parent, false);

                // settup a new ViewHolder
                viewHolder = new ViewHolder();

                viewHolder.mCircleImage = (ImageView)convertView.findViewById(R.id.circle_image);
                viewHolder.mTime = (TextView)convertView.findViewById(R.id.pickup_time);

                // store the holder with the view
                convertView.setTag(viewHolder);

            } else {
                // viewHolder is already available
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mTime.setText((String) getItem(position));

            viewHolder.mCircleImage.setScaleX(1.0f);
            viewHolder.mCircleImage.setScaleY(1.0f);
            GradientDrawable bgShape = (GradientDrawable) viewHolder.mCircleImage.getBackground();

            bgShape.setColor(getResources().getColor(R.color.medium_gray));

            // tag the view with a position that can be used to retrieve time for the center row
            viewHolder.arrayPosition = position;

            return convertView;
        }

        public synchronized void expandAndRestore(View view, double speed) {

            if (mPreviouslyExpandedView != view) {

                // expand currentView
                ImageView circleView = (ImageView) view.findViewById(R.id.circle_image);

                GradientDrawable bgShape = (GradientDrawable) circleView.getBackground();

                bgShape.setColor(getResources().getColor(R.color.enterprise_green));

                if (speed < 30) {
                    circleView.animate()
                            .withLayer()
                            .scaleX(2.0f)
                            .scaleY(2.0f)
                            .setDuration((long) ANIMATION_DURATION)
                            .setListener(new InnerAnimatorListener(circleView))
                            .start();
                } else {
                    circleView.setScaleX(2.0f);
                    circleView.setScaleY(2.0f);
                }




                // restore prior view icon
                if (mPreviouslyExpandedView!= null) {

                    circleView = (ImageView) mPreviouslyExpandedView.findViewById(R.id.circle_image);

                    bgShape = (GradientDrawable) circleView.getBackground();

                    // set center row circle view color to green and return size to original
                    bgShape.setColor(getResources().getColor(R.color.medium_gray));

                    circleView.setScaleX(1.0f);
                    circleView.setScaleY(1.0f);
                }
            }

            // save current view as prior
            mPreviouslyExpandedView = view;
        }
    }

    private class ViewHolder {
        ImageView mCircleImage;
        TextView mTime;
        ImageView mSelectLabel;
        int arrayPosition;
    }

    private class InnerAnimatorListener implements Animator.AnimatorListener {

        View mView;

        InnerAnimatorListener(View v) {
            mView = v;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            ViewCompat.setHasTransientState(mView, true);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            ViewCompat.setHasTransientState(mView, false);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mView.setScaleX(1.0f);
            mView.setScaleY(1.0f);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
