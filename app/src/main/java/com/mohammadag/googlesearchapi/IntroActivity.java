package com.mohammadag.googlesearchapi;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class IntroActivity extends FragmentActivity implements OnInitListener {
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	/* Yes I was bored, but hey, it's an example! 
	 * These replies are randomized with randInt below.
	 */
	public static final String[] VOICE_REPLIES = {
		"Thought you'd never ask.",
		"Welcome, to the Google Now API.",
		"Wow, you're reading this again?",
		"I'm flattered you asked for this again",
		"Cool, so you figured it out!"
	};

	private TextToSpeech mTts;
	private boolean mStartedFromXposed = false;
	public IntroFragment mIntroFragment;
	public PluginsFragment mPluginsFragment;
	private BroadcastReceiver mPackageReceiver;
    String version = "123";

	class DownloadTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {
			String responseString = null;

			try {
				URL u = new URL(uri[0]);
				URLConnection c = u.openConnection();
				c.connect();

				InputStream inputStream = c.getInputStream();

				responseString = convertStreamToString(inputStream);
			} catch (Exception e) {
				responseString = null;
			}


			return responseString;
		}

		@Override
		protected void onPostExecute(String result) {

			if(result == null || result.equals("")){
				setToast("Download Fail !");
			}

			// Check current google now api version
			List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
			for (int i = 0; i < packs.size(); i++) {
				PackageInfo p = packs.get(i);
				if (p.packageName.equals("com.google.android.googlequicksearchbox")) {
					version = Integer.toString(p.versionCode);
				}
			}

			SharedPreferences prfs = getSharedPreferences("Hooks", Context.MODE_WORLD_READABLE);
			String currentHook = prfs.getString("Hook", Constants.DEFAULT_HOOK);

			Properties properties = new Properties();
			try {
				properties.load(new StringReader(result));
			} catch (IOException e) {
                // none
			}

            String found = properties.getProperty(version);

            // Try best match (-1)
            if(found == null){

                version = version.substring(0, version.length() - 1);

                Set<Object> versions = properties.keySet();

                for (Object v : versions) {

                    String ver = v.toString().substring(0, v.toString().length() - 1);

                    if(ver.equals(version)){
                        found = properties.getProperty(ver);
                    }

                }

            }

            String toast = null;

            if(found == null){

                setHooks(Constants.DEFAULT_HOOK);

                toast = "Using default hooks";

            }else if (found.equalsIgnoreCase(Constants.DEFAULT_HOOK) || found.equalsIgnoreCase(currentHook)){

                toast = "You already have the latest hooks";

            }else{

                setHooks(found);

                toast = "Hooks have been updated.\nPlease reboot!";

            }

			setToast(toast);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        SharedPreferences prfs = getSharedPreferences("Hooks", Context.MODE_WORLD_READABLE);
        String hookcheck = prfs.getString("Hooks", null);


		Set<String> categories = getIntent().getCategories();
		if (categories != null) {
			if (categories.contains("de.robv.android.xposed.category.MODULE_SETTINGS")) {
				setTheme(android.R.style.Theme_DeviceDefault);
				getActionBar().setDisplayHomeAsUpEnabled(true);
				mStartedFromXposed = true;
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		}

		setContentView(R.layout.activity_intro);

		if (getIntent().getBooleanExtra(GoogleSearchApi.KEY_VOICE_TYPE, false))
			mTts = new TextToSpeech(this, this);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		getActionBar().setIcon(UiUtils.getGoogleSearchIcon(this));

		mPackageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (mPluginsFragment != null) {
					mPluginsFragment.handlePackageState(context, intent);
				}
			}	
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.intro, menu);
		return true;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				IntroFragment fragment = new IntroFragment();
				return fragment;
			} else if (position == 1) {
				mPluginsFragment = new PluginsFragment();
				return mPluginsFragment;
			}

			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	public void onBackPressed() {
		if (mStartedFromXposed) {
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mStartedFromXposed)
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

		registerBroadcastReceiver();
	}

	private void registerBroadcastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addDataScheme("package");
		registerReceiver(mPackageReceiver, intentFilter);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mPackageReceiver);
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_about:
			showAbout();
			return true;
		case R.id.menu_visit_support_thread:
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("http://mohammadag.xceleo.org/redirects/google_now_api.html"));
			startActivity(i);
			return true;
        case R.id.menu_change:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Insert New Hooks");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = getSharedPreferences("Hooks", Context.MODE_WORLD_READABLE).edit();
                    editor.putString("Hook", input.getText().toString());
                    editor.apply();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            return true;
		case R.id.menu_donate:
			Intent donate = new Intent(Intent.ACTION_VIEW);
			donate.setData(Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=5MW3FZSKRSP3Ll"));
			startActivity(donate);
			return true;
        case R.id.menu_hooks:
            new DownloadTask().execute("https://raw.githubusercontent.com/ricardojlrufino/Google-Search-API/master/hooksversion.properties");
            return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	};

	private void showAbout() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(IntroActivity.this)
		.setTitle(R.string.app_name)
		.setMessage(R.string.about_text);

		alertDialog.show();
	}

	@Override
	public void onInit(int result) {
		if (result == TextToSpeech.SUCCESS) {
			mTts.speak(VOICE_REPLIES[randInt(0, VOICE_REPLIES.length-1)],
					TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	class PInfo {
	    private String pname = "";
	    private String versionName = "";
	    private void prettyPrint() {
	    }
	}

	private ArrayList<PInfo> getPackages() {
	    ArrayList<PInfo> apps = getInstalledApps(false); /* false = no system packages */
	    final int max = apps.size();
	    for (int i=0; i<max; i++) {
	        apps.get(i).prettyPrint();
	    }
	    return apps;
	}

	private ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {
	    ArrayList<PInfo> res = new ArrayList<PInfo>();        
	    List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
	    for(int i=0;i<packs.size();i++) {
	        PackageInfo p = packs.get(i);
	        if ((!getSysPackages) && (p.versionName == null)) {
	            continue ;
	        }
	        PInfo newInfo = new PInfo();
	        newInfo.pname = p.packageName;
	        newInfo.versionName = p.versionName;
	        
	        if (newInfo.pname.equals("com.google.android.googlequicksearchbox")) {
	     		Toast.makeText(getApplicationContext(), newInfo.versionName,
	 	 			   Toast.LENGTH_LONG).show();
	        }
	    }
	    return res; 
	}

	/* From http://stackoverflow.com/a/363692 */
	private static int randInt(int min, int max) {
		// Usually this can be a field rather than a method variable
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	public void setToast(String message) {
		Toast toast = Toast.makeText(IntroActivity.this, message, Toast.LENGTH_SHORT);
		TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
		if (v != null) v.setGravity(Gravity.CENTER);
		toast.show();
	}

    public void setHooks(String data) {
		SharedPreferences.Editor editor = getSharedPreferences("Hooks", Context.MODE_WORLD_READABLE).edit();
		editor.putString("Hook", data);
		editor.putString("Version", version);
		editor.apply();
    }

	private static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}