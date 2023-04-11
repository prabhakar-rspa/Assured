/*
 * Copyright (c) 2012-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.rspcaassured.ui;

import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.rspcaassured.R;
import com.rspcaassured.Utility.AppUtility;
import com.rspcaassured.Utility.BackgroundJobService;
import com.rspcaassured.Utility.InternetUtil;
import com.rspcaassured.Utility.LoadingDialog;
import com.rspcaassured.Utility.SharedPref;
import com.rspcaassured.loaders.ApplicationListLoader;
import com.rspcaassured.objects.ApplicationObject;
import com.rspcaassured.sync.Application.ApplicationSyncAdapter;
import com.rspcaassured.sync.Assessment.AssessmentSyncAdapter;
import com.rspcaassured.sync.AssessmentChecklistSection.AssessmentChecklistSectionSyncAdapter;
import com.rspcaassured.sync.AssessmentQuestion.AssessmentQuestionSyncAdapter;
import com.rspcaassured.ui.Assessment.AssessmentOverviewActivity;
import com.rspcaassured.ui.Checklist.ChecklistReport;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.mobilesync.model.Layout;
import com.salesforce.androidsdk.mobilesync.util.Constants;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.smartstore.ui.SmartStoreInspectorActivity;
import com.salesforce.androidsdk.ui.SalesforceListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main activity
 */
public class MainActivity extends SalesforceListActivity implements
		OnQueryTextListener, OnCloseListener, AdapterView.OnItemSelectedListener,
		LoaderManager.LoaderCallbacks<List<ApplicationObject>> {

	private static final String CHECKLIST_QUESTION_SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.AssessmentQuestion.AssessmentQuestionSyncAdapter";
	private static final String CHECKLIST_SECTION_SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.AssessmentChecklistSection.AssessmentChecklistSectionSyncAdapter";
	private static final String ASSESSMENT_SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.Assessment.AssessmentSyncAdapter";// assessment sync content authority


	public static final String OBJECT_ID_KEY = "object_id";
	private static final String SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.Application.ApplicationSyncAdapter";// application sync content authority
	private static final long SYNC_FREQUENCY_ONE_HOUR = 60 * 60;
	private static final int APPLICATION_LOADER_ID = 1; // for loading applications

	private static final String TAG = "MainActivity";
	private static final int JOB_ID = 1;
	private static final String APPLICATION_SYNC_DOWN_NAME = "syncDownApplications";

	private ApplicationListAdapter appListAdapter; //new change for application
	private NameFieldFilter nameFilter;
	public static LogoutDialogFragment logoutConfirmationDialog;
	public static NoInternetDialogFragment noInternetDialogFragment;
	private ApplicationListLoader applicationLoader; // for loading application
	private LoadCompleteReceiver loadCompleteReceiver;
	private AtomicBoolean isRegistered;

	private RestClient client;

	// Drawer
	private DrawerLayout drawerLayout;

	// Shared preferences
	private SharedPref sharedPref;

	// Loading dialog
	LoadingDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sharedPref = new SharedPref(this);
		// setting the theme style
		if(sharedPref.loadNightModeState()){
			setTheme(R.style.darkTheme);
		}else {
			setTheme(R.style.AppTheme);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Create loading dialog
		loadingDialog = new LoadingDialog(this); // loading dialog is started onCreateLoader method

		//Assign variable drawer layout
		drawerLayout = findViewById(R.id.drawer_layout);

		// Get application filter spinner
		Spinner appFilterSpinner = findViewById(R.id.applicationFilterSpinner);
		ArrayAdapter<CharSequence> applicationFilterArray = ArrayAdapter.createFromResource(this, R.array.application_filter, android.R.layout.simple_spinner_item);
		applicationFilterArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		appFilterSpinner.setAdapter(applicationFilterArray);
		appFilterSpinner.setOnItemSelectedListener(this);

		appListAdapter = new ApplicationListAdapter(this,R.layout.list_item); // for Application
		getListView().setAdapter(appListAdapter); // for application
		nameFilter = new NameFieldFilter(appListAdapter, null); // Filtering for Applications
		logoutConfirmationDialog = new LogoutDialogFragment();
		noInternetDialogFragment = new NoInternetDialogFragment();
		loadCompleteReceiver = new LoadCompleteReceiver();
		isRegistered = new AtomicBoolean(false);

		sharedPref.setDashboardSeen(true);
		verifyCameraPermission();
	}

	/**
	 * method to verify camera permissions
	 */
	private void verifyCameraPermission(){
		Log.d(TAG, "verifyCameraPermission: verifying user has camera permission");
		if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
			Log.d(TAG, "verifyCameraPermission: no camera permission exists, requesting for camera permission");
			ActivityCompat.requestPermissions(this,
					new String[]{
							Manifest.permission.CAMERA
					},
					100);
		}else{
			Log.d(TAG, "verifyCameraPermission: camera permission exists");
		}
	}

	/**
	 * onClick action for ClickNavMenu
	 */
	public void ClickNavMenu(View view){
		//Open drawer
		openDrawer(drawerLayout);
	}

	/**
	 * opens navigation Drawer
	 */
	public static void openDrawer(DrawerLayout drawerLayout) {
		//Open drawer layout
		drawerLayout.openDrawer(GravityCompat.START);
	}

	/**
	 * on click action for ClickCloseNavigation
	 */
	public void ClickCloseNavigation(View view){
		// close drawer
		closeDrawer(drawerLayout);
	}

	/**
	 * closes navigation Drawer
	 */
	public static void closeDrawer(DrawerLayout drawerLayout) {
		// Close drawer layout
		// Check condition
		if(drawerLayout.isDrawerOpen(GravityCompat.START)){
			// When Drawer is open
			// Close Drawer
			drawerLayout.closeDrawer(GravityCompat.START);
		}
	}

	/**
	 * Onclick action for ClickHome
	 */
	public void ClickHome(View view){
		// When in the main activity, close the drawer
		closeDrawer(drawerLayout);
	}

	/**
	 * redirects activity to profile
	 */
	public void ClickProfile(View view){
		// Redirect activity to profile
		redirectActivity(this, Profile.class);
	}

	/**
	 * Redirects activity to notifications
	 */
	public void ClickNotifications(View view) {
		// Redirect activity
		redirectActivity(this, Notifications.class);
	}

	/**
	 * Redirects activity to settings
	 */
	public void ClickSettings(View view){
		// Redirect activity
		redirectActivity(this, SettingsActivity.class);
	}

	/**
	 * Displays logout confirmation dialog
	 * @param view view element
	 */
	public void onClickLogout(View view){
		if(!InternetUtil.isInternetAvailable(MainActivity.this)){
			//closeDrawer(drawerLayout);
			noInternetDialogFragment.show(getFragmentManager(), "No Internet");
			return;
		}
		logoutConfirmationDialog.show(getFragmentManager(), "LogoutDialog");
	}

	/**
	 * Testing Assessment Overview Layout
	 * @param view view element
	 */
	public void onClickCollapsableCard(View view){
		// Redirect Activity to Assessment Overview
		redirectActivity(this, CollapsableCard.class);
	}

	/**
	 * Testing Checklist Layout
	 * @param view view element
	 */
	public void onClickChecklistReportTest(View view){
		// Redirect Activity to Checklist
		redirectActivity(this, ChecklistReport.class);
	}

	/**
	 * Inspects smart store database
	 * @param view view element
	 */
	public void onClickInspectDB(View view){
		// Inspects smartStore database
		launchSmartStoreInspectorActivity();
	}

	/**
	 * Switch Accounts
	 * @param view view element
	 */
	public void switchAccounts(View view){
		launchAccountSwitcherActivity();
	}

	/**
	 * Syncs the records up and down
	 * @param view view element
	 */
	public void onClickRefresh(View view){
		Toast.makeText(this, "Synchronizing...", Toast.LENGTH_SHORT).show();
		requestSync(false /* sync up + sync down */);

	}

	/**
	 * redirects to new activity
	 */
	public static void redirectActivity(Activity activity, Class aClass) {
		// Initialise intent
		Intent intent = new Intent(activity,aClass);
		// Set flag
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// Start activity
		activity.startActivity(intent);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String text = parent.getItemAtPosition(position).toString();

		switch (text){
			case "Item 1":
				Toast.makeText(this,"Application Filter Item 1 Clicked", Toast.LENGTH_SHORT).show();
				break;
			case "Item 2":
				Toast.makeText(this,"Application Filter Item 2 Clicked", Toast.LENGTH_SHORT).show();
				break;
			case "Item 3":
				Toast.makeText(this,"Application Filter Item 3 Clicked", Toast.LENGTH_SHORT).show();
				break;
			case "Item 4":
				Toast.makeText(this,"Application Filter Item 4 Clicked", Toast.LENGTH_SHORT).show();
				break;
			case "Item 5":
				Toast.makeText(this,"Application Filter Item 5 Clicked", Toast.LENGTH_SHORT).show();
				break;

		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}


	@Override
	public void onLogoutComplete() {
		super.onLogoutComplete();
		// If refresh token is revoked - ClientManager does a logout that doesn't finish top activity activity or show login
		if (!isChild()) {
			recreate();
		}
	}


	@Override
	public void onResume(RestClient client) {
		// Keeping reference to rest client
		this.client = client;

		// Loader initialization and receiver registration
		getLoaderManager().initLoader(APPLICATION_LOADER_ID, null, this).forceLoad();

		// Registering broadcast reciever
		if (!isRegistered.get()) {
			registerReceiver(loadCompleteReceiver,
					new IntentFilter(ApplicationListLoader.LOAD_COMPLETE_INTENT_ACTION));
		}
		isRegistered.set(true);

		// Load user information
		loadUserInformation(drawerLayout);


		// Setup periodic sync
		setupPeriodicSync();

		// Sync now
		requestSync(true /* sync down only */);

		// Periodic clean ghost job starting
		setupPeriodicCleanGhosts();
	}
	/**
	 * Load user information
	 */
	public static void loadUserInformation(DrawerLayout drawerLayout){
		UserAccount user = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();
		Log.d("onCreate", "user.getProfilePhoto(): "+user.getProfilePhoto());
		TextView drawerUserFullName = drawerLayout.findViewById(R.id.drawerUserFullName);
		ImageView drawerProfileImage = drawerLayout.findViewById(R.id.drawerProfileImage);
		ImageView headerToolbarProfileIcon = drawerLayout.findViewById(R.id.headerToolbarProfileIcon);
		if(drawerUserFullName != null){
			String userFullName = user.getFirstName() + " " + user.getLastName();
			drawerUserFullName.setText(userFullName);
		}
		if(drawerProfileImage != null){
			if(user.getProfilePhoto() != null){
				drawerProfileImage.setImageBitmap(user.getProfilePhoto());
			}

		}
		if(headerToolbarProfileIcon != null){
			if(user.getProfilePhoto() != null){
				headerToolbarProfileIcon.setImageBitmap(user.getProfilePhoto());
			}
		}

	}

	/**
	 * Schedule job to periodically clean ghost records from smart store
	 */
	public void setupPeriodicCleanGhosts(){
		Boolean backgroundServicesState = sharedPref.loadBackgroundServicesState();
		Boolean jobAlreadyRunning =  isJobServiceOn(this);
		Log.d(TAG, "Job Already Running => " + jobAlreadyRunning);
		if(backgroundServicesState && !jobAlreadyRunning){
			ComponentName componentName = new ComponentName(this, BackgroundJobService.class);
			PersistableBundle bundle = new PersistableBundle();
			bundle.putString("SyncName", APPLICATION_SYNC_DOWN_NAME);
			JobInfo info = new JobInfo.Builder(JOB_ID, componentName)
					.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
					.setPersisted(true)
					.setPeriodic(60 * 60 * 1000)
					.setExtras(bundle)
					.build();

			JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
			int resultCode = scheduler.schedule(info);
			if(resultCode == JobScheduler.RESULT_SUCCESS){
				Log.d(TAG, "Job Scheduled.");
			}else{
				Log.d(TAG, "Job Scheduling Failed.");
			}
		}
	}

	/**
	 * Checks if Job Service is already pending
	 * @param context
	 * @return
	 */
	public static boolean isJobServiceOn( Context context ) {
		JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;

		boolean hasBeenScheduled = false ;

		for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
			if ( jobInfo.getId() == JOB_ID ) {
				hasBeenScheduled = true ;
				break ;
			}
		}

		return hasBeenScheduled ;
	}

	/**
	 * Setup periodic sync
	 */
	private void setupPeriodicSync() {
		Account account = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentAccount();
		/*
		 * Enables sync automatically for this provider. To enable almost
		 * instantaneous sync when records are modified locally, a call needs
		 * to be made by the content provider to notify the sync provider
		 * that the underlying data set has changed. Since we don't use cursors
		 * in this sample application, we simply enable periodic sync every hour.
		 */
		ContentResolver.setSyncAutomatically(account, SYNC_CONTENT_AUTHORITY, true);
		ContentResolver.addPeriodicSync(account, SYNC_CONTENT_AUTHORITY,
				Bundle.EMPTY, SYNC_FREQUENCY_ONE_HOUR);
	}

	/**
	 * Request a sync
	 * @param syncDownOnly if true, only a sync down is done, if false a sync up followed by a sync down is done
	 */
	private void requestSync(boolean syncDownOnly) {
		Log.d("onCreate", "requestSync: called");
		Account account = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentAccount();
		Bundle extras = new Bundle();
		extras.putBoolean(ApplicationSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
		ContentResolver.requestSync(account, SYNC_CONTENT_AUTHORITY, extras);


		Bundle checklistQuestionExtras = new Bundle();
		checklistQuestionExtras.putBoolean(AssessmentQuestionSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
		ContentResolver.requestSync(account, CHECKLIST_QUESTION_SYNC_CONTENT_AUTHORITY, checklistQuestionExtras);

		Bundle checklistSectionExtras = new Bundle();
		checklistSectionExtras.putBoolean(AssessmentChecklistSectionSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
		ContentResolver.requestSync(account, CHECKLIST_SECTION_SYNC_CONTENT_AUTHORITY, checklistSectionExtras);

		Bundle assessmentExtras = new Bundle();
		assessmentExtras.putBoolean(AssessmentSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
		ContentResolver.requestSync(account, ASSESSMENT_SYNC_CONTENT_AUTHORITY, assessmentExtras);
	}

	@Override
	public void onPause() {
		// Unregister broadcast reciever
		if (isRegistered.get()) {
			unregisterReceiver(loadCompleteReceiver);
		}
		isRegistered.set(false);
		// destroy loader
		getLoaderManager().destroyLoader(APPLICATION_LOADER_ID);
		applicationLoader = null;

		super.onPause();

		// Close drawer
		closeDrawer(drawerLayout);
	}

	@Override
	public void onDestroy() {
		loadCompleteReceiver = null;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar_menu, menu);
		final MenuItem searchItem = menu.findItem(R.id.action_search);
		final SearchView searchView = new SearchView(this);
		searchView.setOnQueryTextListener(this);
		searchView.setOnCloseListener(this);
		searchItem.setActionView(searchView);
		return super.onCreateOptionsMenu(menu);
	}

	private void launchSmartStoreInspectorActivity() {
		this.startActivity(SmartStoreInspectorActivity.getIntent(this, false, null));
	}

	private void launchAccountSwitcherActivity() {
		final Intent i = new Intent(this, SalesforceSDKManager.getInstance().getAccountSwitcherActivityClass());
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(i);
	}

	@Override
	public Loader<List<ApplicationObject>> onCreateLoader(int id, Bundle args) {
		// Start loading dialog
		//loadingDialog.startAlertDialog();

		applicationLoader = new ApplicationListLoader(this, MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser());
		return applicationLoader;
	}

	@Override
	public void onLoaderReset(Loader<List<ApplicationObject>> loader) {
		refreshList(null);
	}

	@Override
	public void onLoadFinished(Loader<List<ApplicationObject>> loader,
							   List<ApplicationObject> data) {
		refreshList(data);
		//loadingDialog.dismissDialog();
	}

	@Override
	public boolean onClose() {
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		nameFilter.setFilterTerm(query);
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		nameFilter.setFilterTerm(newText);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final ApplicationObject sObject = appListAdapter.getItem(position);
		launchDetailActivity(sObject);
	}

	private void refreshList() {
		getLoaderManager().getLoader(APPLICATION_LOADER_ID).forceLoad();
	}

	private void refreshList(List<ApplicationObject> data) {
		// NB: We feed the data to nameFilter, and in turns it feeds the (filtered) data to listAdapter
		nameFilter.setData(data);
	}

	private void launchDetailActivity(ApplicationObject application) {
		final Intent detailIntent = new Intent(this, AssessmentOverviewActivity.class);
		detailIntent.addCategory(Intent.CATEGORY_DEFAULT);
		detailIntent.putExtra(OBJECT_ID_KEY, application.getObjectId());
		startActivity(detailIntent);
	}

	/**
	 * Custom array adapter to supply data to the list view for applications.
	 *
	 * @author shahedmiah
	 */
	private static class ApplicationListAdapter extends ArrayAdapter<ApplicationObject> {

		private final int listItemLayoutId;
		private List<ApplicationObject> sObjects;

		/**
		 * Parameterized constructor.
		 *
		 * @param context Context.
		 * @param listItemLayoutId List item view resource ID.
		 */
		public ApplicationListAdapter(Context context, int listItemLayoutId) {
			super(context, listItemLayoutId);
			this.listItemLayoutId = listItemLayoutId;
		}



		/**
		 * Sets data to this adapter.
		 *
		 * @param data Data.
		 */
		public void setData(List<ApplicationObject> data) {
			clear();
			sObjects = data;
			if (data != null) {
				addAll(data);
				notifyDataSetChanged();
			}
		}

		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(listItemLayoutId, null);
			}
			if (sObjects != null) {
				final ApplicationObject sObject = sObjects.get(position);
				if (sObject != null) {
					// Get views from within list_item layout
					final TextView objName = (TextView) convertView.findViewById(R.id.obj_name);
					final TextView assessmentNumber = (TextView) convertView.findViewById(R.id.assessmentNumberValue);
					final TextView siteAddress = (TextView) convertView.findViewById(R.id.siteAddress);
					final TextView objAssessmentDayNumber = (TextView) convertView.findViewById(R.id.obj_assessmentDayNumber);
					final TextView objAssessmentDayText = (TextView) convertView.findViewById(R.id.obj_assessmentDayText);
					final TextView progressPercentage = (TextView) convertView.findViewById(R.id.progressPercentage);
					final ProgressBar pb = (ProgressBar) convertView.findViewById(R.id.progressBar);

					// Site and Membership Name
					if (objName != null) {
						String siteNameAndMembershipNumber = sObject.getSiteName() + " | " + sObject.getMembershipNumber();
						objName.setText(siteNameAndMembershipNumber); // Application Site Name + membership number
					}

					// Assessment Number
					if(assessmentNumber != null) {
						assessmentNumber.setText(sObject.getName());
					}

					// Site Address
					if (siteAddress != null) {
						String address = sObject.getAddress();
						address = address.replace("<br>",", ");
						address = address.replace("&amp;","&");
						siteAddress.setText(address);// Application Site Name
					}

					// Assessment Date
					final String assessmentDateString = sObject.getAssessmentDate();
					if (objAssessmentDayNumber != null) {
						try {
							@SuppressLint("SimpleDateFormat") Date assessmentDateValue=new SimpleDateFormat("yyyy-MM-dd").parse(assessmentDateString);
							Calendar cal = Calendar.getInstance();
							assert assessmentDateValue != null;
							cal.setTime(assessmentDateValue);
							int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
							@SuppressLint("DefaultLocale") String dayOfMonthStr = String.format("%02d",dayOfMonth);
							objAssessmentDayNumber.setText(dayOfMonthStr);
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					if(objAssessmentDayText != null){
						try {
							@SuppressLint("SimpleDateFormat") Date assessmentDateValue=new SimpleDateFormat("yyyy-MM-dd").parse(assessmentDateString);

							@SuppressLint("SimpleDateFormat") DateFormat format2=new SimpleDateFormat("EE");
							assert assessmentDateValue != null;
							String finalDay=format2.format(assessmentDateValue);
							objAssessmentDayText.setText(finalDay);
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}

					// Progress Percentage
					String applicationProgress = sObject.getChecklistCompletion();
					if(applicationProgress.equals("")){
						applicationProgress = "0";
					}
					if(progressPercentage != null){
						String applicationProgressText = applicationProgress + "%";
						progressPercentage.setText(applicationProgressText);
					}
					// Progress bar
					if(pb != null){
						pb.setProgress(Integer.parseInt(applicationProgress));
					}

					// Sync status
					final ImageView syncImage = convertView.findViewById(R.id.sync_status_view);
					if (syncImage != null && sObject.isLocallyModified()) {
						syncImage.setImageResource(R.drawable.sync_local);
					} else {
						assert syncImage != null;
						syncImage.setImageResource(R.drawable.sync_save);
					}
				}
			}
			return convertView;
		}


	}

	/**
	 * A simple utility class to implement filtering.
	 *
	 * @author shahedmiah
	 */
	private static class NameFieldFilter extends Filter {

		private final ApplicationListAdapter adapter;
		private List<ApplicationObject> data;
		private String filterTerm;

		/**
		 * Parameterized constructor.
		 *
		 * @param adapter List adapter.
		 * @param origList List to perform filtering against.
		 */
		public NameFieldFilter(ApplicationListAdapter adapter, List<ApplicationObject> origList) {
			this.adapter = adapter;
			this.data = origList;
			this.filterTerm = null;
		}

		/**
		 * Sets the original data set.
		 *
		 * @param data Original data set.
		 */
		public void setData(List<ApplicationObject> data) {
			this.data = data;
			filter(filterTerm);
		}

		/**
		 * Sets the filter term
		 * @param filterTerm Text to filter the records
		 */
		public void setFilterTerm(String filterTerm) {
			this.filterTerm = filterTerm;
			filter(filterTerm);
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			if (data == null) {
				return null;
			}
			final FilterResults results = new FilterResults();
			if (TextUtils.isEmpty(constraint)) {
				results.values = data;
				results.count = data.size();
				return results;
			}
			final String filterString = constraint.toString().toLowerCase();
			int count = data.size();
			String filterableString;
			final List<ApplicationObject> resultSet = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				filterableString = data.get(i).getName();
				if (filterableString.toLowerCase().contains(filterString)) {
					resultSet.add(data.get(i));
				}
			}
			results.values = resultSet;
			results.count = resultSet.size();
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results != null && results.values != null) {
				adapter.setData((List<ApplicationObject>) results.values);
			}
		}
	}

	/**
	 * A simple receiver for load complete events.
	 *
	 * @author shahedmiah
	 */
	private class LoadCompleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				final String action = intent.getAction();
				if (ApplicationListLoader.LOAD_COMPLETE_INTENT_ACTION.equals(action)) {
					refreshList();
				}
			}
		}
	}

}