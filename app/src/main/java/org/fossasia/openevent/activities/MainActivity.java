package org.fossasia.openevent.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.fossasia.openevent.OpenEventApp;
import org.fossasia.openevent.R;
import org.fossasia.openevent.dbutils.DataDownload;
import org.fossasia.openevent.dbutils.DbSingleton;
import org.fossasia.openevent.events.CantDownloadEvent;
import org.fossasia.openevent.events.CounterEvent;
import org.fossasia.openevent.events.EventDownloadEvent;
import org.fossasia.openevent.events.MicrolocationDownloadEvent;
import org.fossasia.openevent.events.NoInternetEvent;
import org.fossasia.openevent.events.RefreshUiEvent;
import org.fossasia.openevent.events.SessionDownloadEvent;
import org.fossasia.openevent.events.SpeakerDownloadEvent;
import org.fossasia.openevent.events.SponsorDownloadEvent;
import org.fossasia.openevent.events.TracksDownloadEvent;
import org.fossasia.openevent.fragments.BookmarksFragment;
import org.fossasia.openevent.fragments.LocationsFragment;
import org.fossasia.openevent.fragments.SpeakerFragment;
import org.fossasia.openevent.fragments.SponsorsFragment;
import org.fossasia.openevent.fragments.TracksFragment;


public class MainActivity extends AppCompatActivity {

    private static final String COUNTER_TAG = "Donecounter";
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView navigationView;
    private ProgressBar downloadProgress;
    private FrameLayout mainFrame;
    private int counter;
    private int eventsDone;
    private final String TRACKS_FRAGMENT_TAG = "TFTAG";
    private final String BOOKMARKS_FRAGMENT_TAG = "BFTAG";
    private final String SPEAKER_FRAGMENT_TAG = "SKFTAG";
    private final String SPONSORS_FRAGMENT_TAG = "SPFTAG";
    private final String LOCATION_FRAGMENT_TAG = "LFTAG";
    private final String MAP_FRAGMENT_TAG = "MFTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        counter = 0;
        setContentView(R.layout.activity_main);
        eventsDone = 0;
        setUpToolbar();
        setUpNavDrawer();
        mainFrame = (FrameLayout) findViewById(R.id.layout_main);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        downloadProgress = (ProgressBar) findViewById(R.id.progress);
        downloadProgress.setVisibility(View.VISIBLE);
        downloadProgress.setIndeterminate(true);
        DataDownload download = new DataDownload();
        download.downloadVersions();
        this.findViewById(android.R.id.content).setBackgroundColor(Color.LTGRAY);

        if(savedInstanceState==null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new TracksFragment()).commit();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        OpenEventApp.getEventBus().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenEventApp.getEventBus().register(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        setupDrawerContent(navigationView, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tracks, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Will close the drawer if the home button is pressed
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            BookmarksFragment bf = (BookmarksFragment)getSupportFragmentManager().findFragmentByTag(BOOKMARKS_FRAGMENT_TAG);
            if ( null != bf ) {
                setTitle(getResources().getString(R.string.menu_bookmarks));
            }
            TracksFragment tf = (TracksFragment)getSupportFragmentManager().findFragmentByTag(TRACKS_FRAGMENT_TAG);
            if ( null != tf ) {
                setTitle(getResources().getString(R.string.menu_tracks));
            }
            SpeakerFragment skf = (SpeakerFragment)getSupportFragmentManager().findFragmentByTag(SPEAKER_FRAGMENT_TAG);
            if ( null != skf ) {
                setTitle(getResources().getString(R.string.menu_speakers));
            }
            SponsorsFragment spf = (SponsorsFragment)getSupportFragmentManager().findFragmentByTag(SPONSORS_FRAGMENT_TAG);
            if ( null != spf ) {
                setTitle(getResources().getString(R.string.menu_sponsor));
            }
            LocationsFragment lf = (LocationsFragment)getSupportFragmentManager().findFragmentByTag(LOCATION_FRAGMENT_TAG);
            if ( null != lf ) {
                setTitle(getResources().getString(R.string.menu_locations));
            }
            android.support.v4.app.Fragment ft = getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
            if ( null != ft ) {
                setTitle(getResources().getString(R.string.menu_map));
            }
            setSupportActionBar(mToolbar);
        }
    }

    private void setUpNavDrawer() {
        if (mToolbar != null) {
            final android.support.v7.app.ActionBar ab = getSupportActionBar();
            assert ab != null;
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
            ActionBarDrawerToggle mActionBarDrawerToggle = new ActionBarDrawerToggle(this,
                    mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

            mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
            mActionBarDrawerToggle.syncState();
        }
    }

    private void syncComplete() {
        downloadProgress.setVisibility(View.GONE);
        Bus bus = OpenEventApp.getEventBus();
        bus.post(new RefreshUiEvent());
        ImageView header_drawer = (ImageView) findViewById(R.id.headerDrawer);
        DbSingleton dbSingleton = DbSingleton.getInstance();
        if (!(dbSingleton.getEventDetails().getLogo().isEmpty())) {
            Picasso.with(getApplicationContext()).load(dbSingleton.getEventDetails().getLogo()).into(header_drawer);
        }

        Snackbar.make(mainFrame, getString(R.string.download_complete), Snackbar.LENGTH_SHORT).show();
    }

    private void downloadFailed() {
        downloadProgress.setVisibility(View.GONE);
        Snackbar.make(mainFrame, getString(R.string.download_failed), Snackbar.LENGTH_LONG).show();

    }

    private void setupDrawerContent(NavigationView navigationView, final Menu menu) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();
                        menu.clear();
                        switch (id) {
                            case R.id.nav_tracks:
                                fragmentManager.beginTransaction()
                                        .replace(R.id.content_frame, new TracksFragment(),TRACKS_FRAGMENT_TAG).commit();
                                getSupportActionBar().setTitle(R.string.menu_tracks);
                                break;
                            case R.id.nav_bookmarks:
                                fragmentManager.beginTransaction()
                                        .replace(R.id.content_frame, new BookmarksFragment(),BOOKMARKS_FRAGMENT_TAG).commit();
                                getSupportActionBar().setTitle(R.string.menu_bookmarks);
                                break;
                            case R.id.nav_speakers:
                                fragmentManager.beginTransaction()
                                        .replace(R.id.content_frame, new SpeakerFragment(),SPEAKER_FRAGMENT_TAG).commit();
                                getSupportActionBar().setTitle(R.string.menu_speakers);
                                break;
                            case R.id.nav_sponsors:
                                fragmentManager.beginTransaction()
                                        .replace(R.id.content_frame, new SponsorsFragment(),SPONSORS_FRAGMENT_TAG).commit();
                                getSupportActionBar().setTitle(R.string.menu_sponsor);
                                break;
                            case R.id.nav_locations:
                                fragmentManager.beginTransaction()
                                        .replace(R.id.content_frame, new LocationsFragment(),LOCATION_FRAGMENT_TAG).commit();
                                getSupportActionBar().setTitle(R.string.menu_locations);
                                break;
                            case R.id.nav_map:
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                fragmentTransaction.replace(R.id.content_frame,
                                        ((OpenEventApp) getApplication())
                                                .getMapModuleFactory()
                                                .provideMapModule()
                                                .provideMapFragment(),MAP_FRAGMENT_TAG).commit();
                                getSupportActionBar().setTitle(R.string.menu_map);
                                break;
                            case R.id.nav_settings:
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                mDrawerLayout.closeDrawers();
                                startActivity(intent);

                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    //Subscribe Events
    @Subscribe
    public void onCounterReceiver(CounterEvent event) {
        counter = event.getRequestsCount();
        Log.d(COUNTER_TAG, counter + "");
        if (counter == 0) {
            syncComplete();
        }
    }

    @Subscribe
    public void onTracksDownloadDone(TracksDownloadEvent event) {
        if (event.isState()) {
            eventsDone++;
            Log.d(COUNTER_TAG, eventsDone + " " + counter);
            if (counter == eventsDone) {
                syncComplete();
            }
        } else {
            downloadFailed();
        }
    }

    @Subscribe
    public void onSponsorsDownloadDone(SponsorDownloadEvent event) {
        if (event.isState()) {
            eventsDone++;
            Log.d(COUNTER_TAG, eventsDone + " " + counter);
            if (counter == eventsDone) {
                syncComplete();
            }
        } else {

            downloadFailed();
        }
    }

    @Subscribe
    public void onSpeakersDownloadDone(SpeakerDownloadEvent event) {
        if (event.isState()) {
            eventsDone++;
            Log.d(COUNTER_TAG, eventsDone + " " + counter);
            if (counter == eventsDone) {
                syncComplete();
            }
        } else {

            downloadFailed();
        }
    }

    @Subscribe
    public void onSessionDownloadDone(SessionDownloadEvent event) {
        if (event.isState()) {
            eventsDone++;
            Log.d(COUNTER_TAG, eventsDone + " " + counter);
            if (counter == eventsDone) {
                syncComplete();
            }
        } else {

            downloadFailed();
        }
    }

    @Subscribe
    public void noInternet(NoInternetEvent event) {
        downloadFailed();
    }

    @Subscribe
    public void cantDownload(CantDownloadEvent event) {
        downloadProgress.setVisibility(View.GONE);
        Snackbar.make(mainFrame, getString(R.string.cantDownload), Snackbar.LENGTH_LONG).show();
    }

    @Subscribe
    public void onEventsDownloadDone(EventDownloadEvent event) {
        if (event.isState()) {
            eventsDone++;
            Log.d(COUNTER_TAG, eventsDone + " " + counter);
            if (counter == eventsDone) {
                syncComplete();
            }
        } else {

            downloadFailed();
        }
    }

    @Subscribe
    public void onMicrolocationsDownloadDone(MicrolocationDownloadEvent event) {
        if (event.isState()) {
            eventsDone++;
            Log.d(COUNTER_TAG, eventsDone + " " + counter);
            if (counter == eventsDone) {
                syncComplete();
            }
        } else {

            downloadFailed();
        }

    }

}