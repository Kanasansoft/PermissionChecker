package com.kanasansoft.android.PermissionChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class PermissionCheckerActivity extends Activity {

	static String TAG = "PermissionChecker";
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		PackageManager packageManager = getPackageManager();

		List<ApplicationInfo> infos = packageManager.getInstalledApplications(0);

		ArrayList<PermissionData> permissionDataList = new ArrayList<PermissionData>();
		{
			for (ApplicationInfo info : infos) {
				String applicationLabel = (String)packageManager.getApplicationLabel(info);
				String packageName = info.packageName;
				ArrayList<String> permissionNames = new ArrayList<String>();
				try {
					PackageInfo packageInfo = packageManager.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS);
					String[] requestedPermissions = packageInfo.requestedPermissions;
					if (requestedPermissions != null) {
						for (int i = 0; i < requestedPermissions.length; i++) {
							permissionNames.add(requestedPermissions[i]);
						}
					}
				} catch (NameNotFoundException e) {
					Log.e(TAG, applicationLabel);
				}
				permissionDataList.add(new PermissionData(applicationLabel, packageName, permissionNames));
			}
		}

		HashMap<String, ArrayList<String>> applicationMapGroupByPermission = new HashMap<String, ArrayList<String>>();
		{
			for (PermissionData permissionData : permissionDataList) {
				String applicationLabel = permissionData.applicationLabel;
				ArrayList<String> permissionNames = permissionData.permissionNames;
				for (String permissionName : permissionNames) {
					if(!applicationMapGroupByPermission.containsKey(permissionName)){
						applicationMapGroupByPermission.put(permissionName, new ArrayList<String>());
					}
					applicationMapGroupByPermission.get(permissionName).add(applicationLabel);
				}
			}
		}

		HashMap<String, ArrayList<String>> permissionMapGroupByApplication = new HashMap<String, ArrayList<String>>();
		{
			for (PermissionData permissionData : permissionDataList) {
				String applicationLabel = permissionData.applicationLabel;
				ArrayList<String> permissionNames = permissionData.permissionNames;
				permissionMapGroupByApplication.put(applicationLabel, new ArrayList<String>());
				for (String permissionName : permissionNames) {
					permissionMapGroupByApplication.get(applicationLabel).add(permissionName);
				}
			}
		}

		ArrayList<BindItemWithCaption> applicationListGroupByPermissionItems = new ArrayList<BindItemWithCaption>();
		{
			TreeSet<String> permissionNames = new TreeSet<String>(applicationMapGroupByPermission.keySet());
			for (String permissionName : permissionNames) {
				applicationListGroupByPermissionItems.add(new BindItemWithCaption(permissionName, true));
				ArrayList<String> applicationNames = applicationMapGroupByPermission.get(permissionName);
				Collections.sort(applicationNames);
				for (String applicationName : applicationNames) {
					applicationListGroupByPermissionItems.add(new BindItemWithCaption(applicationName, false));
				}
			}
		}

		ArrayList<BindItemWithCaption> permissionListGroupByApplicationItems = new ArrayList<BindItemWithCaption>();
		{
			TreeSet<String> applicationNames = new TreeSet<String>(permissionMapGroupByApplication.keySet());
			for (String applicationName : applicationNames) {
				permissionListGroupByApplicationItems.add(new BindItemWithCaption(applicationName, true));
				ArrayList<String> permissionNames = permissionMapGroupByApplication.get(applicationName);
				Collections.sort(permissionNames);
				for (String permissionName : permissionNames) {
					permissionListGroupByApplicationItems.add(new BindItemWithCaption(permissionName, false));
				}
			}
		}

		AdapterWithCaption adapterApplication = new AdapterWithCaption(this, R.layout.list_item, applicationListGroupByPermissionItems);
		ListView listApplication = (ListView)findViewById(R.id.application_list);
		listApplication.setAdapter(adapterApplication);

		AdapterWithCaption adapterPermission = new AdapterWithCaption(this, R.layout.list_item, permissionListGroupByApplicationItems);
		ListView listPermission = (ListView)findViewById(R.id.permission_list);
		listPermission.setAdapter(adapterPermission);

		TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
		tabHost.setup();

		{
			TabSpec tab = tabHost.newTabSpec("Group By Permission");
			tab.setIndicator("Group By Permission");
			tab.setContent(R.id.application_tab);
			tabHost.addTab(tab);
		}

		{
			TabSpec tab = tabHost.newTabSpec("Group By Application");
			tab.setIndicator("Group By Application");
			tab.setContent(R.id.permission_tab);
			tabHost.addTab(tab);
		}

		tabHost.setCurrentTab(0);

	}

	private class PermissionData {

		String applicationLabel;
		String packageName;
		ArrayList<String> permissionNames;

		PermissionData(String applicationLabel, String packageName, ArrayList<String> permissionNames) {
			this.applicationLabel = applicationLabel;
			this.packageName = packageName;
			this.permissionNames = permissionNames;
		}

	}

	private class BindItemWithCaption {
		String value;
		boolean caption;
		BindItemWithCaption(String value, boolean caption) {
			this.value = value;
			this.caption = caption;
		}
	}

	class AdapterWithCaption extends ArrayAdapter<BindItemWithCaption> {

		private LayoutInflater layoutInflater;
		private int resourceId;


		AdapterWithCaption(Context context, int resourceId, List<BindItemWithCaption> objects) {
			super(context, resourceId, objects);
			this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.resourceId = resourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = layoutInflater.inflate(resourceId, null);
			}

			BindItemWithCaption bindItemWithCaption = (BindItemWithCaption)getItem(position);

			TextView textView = (TextView)convertView.findViewById(R.id.value);
			textView.setText(bindItemWithCaption.value);
			if (bindItemWithCaption.caption) {
				textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				textView.setBackgroundColor(Color.BLUE);
			} else {
				textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
				textView.setBackgroundColor(Color.BLACK);
			}
			return convertView;

		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

	}

}
