package com.mohammadag.googlesearchapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.Properties;
import java.util.Set;

import static com.mohammadag.googlesearchapi.GoogleSearchApi.UPDATE_URL;


public class IntroFragment extends Fragment {
	private TextView mStatusTextView;
	private TextView mStatusVersionView;

	// Google API Version
	private String version = null;
	private String versionName = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_intro, container, false);

		mStatusTextView = (TextView) view.findViewById(R.id.status_text);

		if (mStatusTextView.getText().toString().contains("Status: Up and running")) {
            setStatusText(R.string.we_have_liftoff);
		} else {
            setStatusText(R.string.we_have_a_problem);
        }


		mStatusVersionView = (TextView) view.findViewById(R.id.status_version);

		Drawable icon = UiUtils.getGoogleSearchIcon(getActivity());
		if (icon == null) {
			setStatusText(R.string.google_search_not_installed);
		} else {
			ImageView iconView = (ImageView) view.findViewById(R.id.gsearch_icon);
			iconView.setImageDrawable(icon);
			//getActionBar().setIcon(icon);
		}

		if(version != null){
			mStatusVersionView.setText(Html.fromHtml("<b>Google API:</b> " + versionName + " ("+version+")"));
		}else{
			mStatusVersionView.setText(Html.fromHtml("<b>Google API:</b> Not Found !"));
		}

		view.findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                new DownloadTask().execute(UPDATE_URL);
			}
		});

		TextView copyrightView = (TextView) view.findViewById(R.id.copyright_text);
		copyrightView.setSelected(true);

		final SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(getActivity());

		return view;
	}



	private void setStatusText(int resId) {
		setStatusString(getString(resId));
	}

	private void setStatusString(String text) {
		mStatusTextView.setText(Html.fromHtml(getString(R.string.status_text, text)));
	}

	public void setGoogleVersion(String versionName, String version) {
		this.versionName = versionName;
		this.version = version;
	}

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

            SharedPreferences prfs = getActivity().getSharedPreferences("Hooks", Context.MODE_WORLD_READABLE);
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

                toast = "New hook not found !";

            }else if (found.equalsIgnoreCase(currentHook)){

                toast = "You already have the latest hooks";

            }else{

                setHooks(found);

                toast = "Hooks have been updated.\nPlease reboot!";

            }

            setToast(toast);
        }

        private String convertStreamToString(InputStream is) throws UnsupportedEncodingException {
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


    public void setHooks(String data) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Hooks", Context.MODE_WORLD_READABLE).edit();
        editor.putString("Hook", data);
        editor.putString("Version", version);
        editor.apply();
    }


    public void setToast(String message) {
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

}
