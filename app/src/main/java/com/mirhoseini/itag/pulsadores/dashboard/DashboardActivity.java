package com.mirhoseini.itag.pulsadores.dashboard;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mirhoseini.itag.R;
import com.mirhoseini.itag.pulsadores.*;
import com.mirhoseini.itag.pulsadores.database.Devices;
import com.mirhoseini.itag.pulsadores.database.Events;

/**
 * Created by sylvek on 28/12/2015.
 */
public class DashboardActivity extends CommonActivity implements DevicePreferencesFragment.OnDevicePreferencesListener, DashboardFragment.OnDashboardListener, ConfirmAlertDialogFragment.OnConfirmAlertDialogListener, EventsHistoryFragment.OnEventsHistoryListener {

    private static final int NUM_PAGES = 3;

    //    private static final int CONFIRM_REMOVE_EVENTS = 0;
    private static final int CONFIRM_REMOVE_KEYRING = 1;
    public static final String EVENTS_HISTORY_FRAGMENT = "eventsHistoryFragment";

    private BluetoothLEService service;

    private boolean activated;

    private String address;

    private String name;

    private ViewPager mPager;

    private TabLayout mTab;

    private FloatingActionButton mFab;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder instanceof BluetoothLEService.BackgroundBluetoothLEBinder) {
                service = ((BluetoothLEService.BackgroundBluetoothLEBinder) iBinder).service();
                service.connect(DashboardActivity.this.address);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(BluetoothLEService.TAG, "onServiceDisconnected()");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        mTab = (TabLayout) findViewById(R.id.tab);
        mPager = (ViewPager) findViewById(R.id.pager);
        final ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());

        mTab.setTabsFromPagerAdapter(pagerAdapter);
        mTab.getTabAt(0).setText(R.string.dashboard);
        mTab.getTabAt(1).setText(R.string.preferences);
        mTab.getTabAt(2).setText(R.string.events_history);
        mTab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mPager.setAdapter(pagerAdapter);
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTab));
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.hide();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mFab.setImageResource((activated) ? android.R.drawable.ic_lock_silent_mode_off : android.R.drawable.ic_lock_silent_mode);
                activated = !activated;
                onImmediateAlert(address, activated);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        address = getIntent().getStringExtra(Devices.ADDRESS);
        name = getIntent().getStringExtra(Devices.NAME);
        setTitle(name);
    }

    private void onImmediateAlert(final String address, final boolean activate) {
        service.immediateAlert(address, (activate) ? BluetoothLEService.HIGH_ALERT : BluetoothLEService.NO_ALERT);
    }

    @Override
    public void onDashboardStarted() {
        // bind service
        bindService(new Intent(this, BluetoothLEService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDashboardStopped() {
        if (service != null) {
            service.disconnect(this.address);
        }

        setRefreshing(false);

        unbindService(serviceConnection);
    }

    @Override
    public void onImmediateAlertAvailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFab.show();
            }
        });
    }

    @Override
    public void onRingStone(int source) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ring_tone));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(Preferences.getRingtone(this, address, Preferences.Source.values()[source].name())));
        startActivityForResult(intent, source);
    }

    @Override
    public void onOutOfRangerBip(Boolean enabled) {
        service.setLinkLossNotificationLevel(address, (enabled) ? BluetoothLEService.HIGH_ALERT : BluetoothLEService.NO_ALERT);
    }

    @Override
    public void doPositiveClick(int returnCode) {
        switch (returnCode) {
            case CONFIRM_REMOVE_KEYRING:
                if (Preferences.clearAll(this, address)) {
                    setRefreshing(false);
                    service.remove(address);
                    Devices.removeDevice(this, address);
                    Events.removeEvents(this, address);
                    NavUtils.navigateUpFromSameTask(this);
                }
                break;
        }
    }

    @Override
    public void doNegativeClick(int returnCode) {
        // nothing to do.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final Preferences.Source source;
            switch (requestCode) {
                default:
                case 0 /* single_click */:
                    source = Preferences.Source.single_click;
                    break;
                case 1 /* double_click */:
                    source = Preferences.Source.double_click;
                    break;
                case 2 /* out_of_range */:
                    source = Preferences.Source.out_of_range;
                    break;
                case 3 /* connected */:
                    source = Preferences.Source.connected;
                    break;
            }

            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                Preferences.setRingtone(this, address, source.name(), uri.toString());
            }
        }
    }

    @Override
    public void onExportEvents() {
        final String title = getString(R.string.app_name);
        final String export = Events.export(this, this.address);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, export);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, title));
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return DashboardFragment.instance(address);
                case 1:
                    return DevicePreferencesFragment.instance(address);
                case 2:
                    return EventsHistoryFragment.instance(address);
                default:
                    throw new RuntimeException("no fragment for position " + position);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            ConfirmAlertDialogFragment.instance(R.string.confirm_remove_keyring, CONFIRM_REMOVE_KEYRING).show(getFragmentManager(), "dialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard, menu);
        return true;
    }
}
