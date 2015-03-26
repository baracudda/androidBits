package com.blackmoonit.androidbits.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.blackmoonit.androidbits.net.WebUtils;


/**
 * The About dialog for an app.
 *
 * @author Ryan Fischbach
 */
public class AppAbout extends Activity implements View.OnClickListener {
	static private final String TAG = "BITS.lib.app."+AppAbout.class.getSimpleName();

	public Context getContext() {
		return this;
	}

	protected int getResId(String aResType, String aResName) {
		Context theContext = getContext();
		return theContext.getResources().getIdentifier(aResName,aResType,theContext.getPackageName());
	}

	private int R_layout_app_about = 0;
	private int R_drawable_app_icon = 0;
	private int R_string_app_name = 0;
	private int R_string_app_about_version_display = 0;
	private int R_string_version_name = 0;
	private int R_string_app_about_text_thanks = 0;
	private int R_string_app_about_link_changelog = 0;
	private int R_string_app_about_link_faq = 0;
	private int R_string_app_about_link_email = 0;
	private int R_id_app_about_app_version = 0;
	private int R_id_app_about_text_thanks = 0;
	private int R_id_app_about_text_author = 0;
	private int R_id_app_about_button_changelog = 0;
	private int R_id_app_about_button_faq = 0;
	private int R_id_app_about_button_email = 0;
	private int R_id_app_about_button_positive = 0;

	private void getResIds() {
		String thePackageName = getContext().getPackageName();
		Resources theRes = getContext().getResources();
		R_layout_app_about = theRes.getIdentifier("app_about","layout",thePackageName);
		R_drawable_app_icon = theRes.getIdentifier("app_icon","drawable",thePackageName);
		R_string_app_name = theRes.getIdentifier("app_name","string",thePackageName);
		R_string_app_about_version_display = theRes.getIdentifier("app_about_version_display","string",thePackageName);
		R_string_version_name = theRes.getIdentifier("version_name","string",thePackageName);
		R_string_app_about_text_thanks = theRes.getIdentifier("app_about_text_thanks","string",thePackageName);
		R_string_app_about_link_changelog = theRes.getIdentifier("app_about_link_changelog","string",thePackageName);
		R_string_app_about_link_faq = theRes.getIdentifier("app_about_link_faq","string",thePackageName);
		R_string_app_about_link_email = theRes.getIdentifier("app_about_link_email","string",thePackageName);
		R_id_app_about_app_version = theRes.getIdentifier("app_about_app_version","id",thePackageName);
		R_id_app_about_text_thanks = theRes.getIdentifier("app_about_text_thanks","id",thePackageName);
		R_id_app_about_text_author = theRes.getIdentifier("app_about_text_author","id",thePackageName);
		R_id_app_about_button_changelog = theRes.getIdentifier("app_about_button_changelog","id",thePackageName);
		R_id_app_about_button_faq = theRes.getIdentifier("app_about_button_faq","id",thePackageName);
		R_id_app_about_button_email = theRes.getIdentifier("app_about_button_email","id",thePackageName);
		R_id_app_about_button_positive = theRes.getIdentifier("ButtonPositive","id",thePackageName);
	}

	//fields
	protected int mFontSize = 0;

	@Override
	protected void onCreate(Bundle aSavedInstanceState) {
		super.onCreate(aSavedInstanceState);
		getResIds();

		setTitle(R_string_app_name);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R_layout_app_about);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,R_drawable_app_icon);

		getPrefs(AppPreferenceBase.getPrefs(getContext()));
		setup();
	}

	/**
	 * If we had any prefs that needed to be loaded, we would do so here.
	 * @param aSettings - the app settings
	 */
	protected void getPrefs(SharedPreferences aSettings) {
		//if (aSettings!=null) {
        //}
	}

	protected void setup() {
		Context theContext = getContext();
		View v;
		String theText;

		theText = theContext.getString(R_string_app_about_version_display,
				theContext.getString(R_string_version_name,AppPreferenceBase.getAppVersionName(theContext)));
		v = findViewById(R_id_app_about_app_version);
		((TextView)v).setText(Html.fromHtml(theText));
		if (mFontSize>0)
			((TextView)v).setTextSize(mFontSize);

		theText = theContext.getString(R_string_app_about_text_thanks);
		v = findViewById(R_id_app_about_text_thanks);
		((TextView)v).setText(Html.fromHtml(theText));
		if (mFontSize>0)
			((TextView)v).setTextSize(mFontSize);
		Linkify.addLinks((TextView)v,Linkify.ALL);
		((TextView)v).setMovementMethod(LinkMovementMethod.getInstance());

		v = findViewById(R_id_app_about_text_author);
		if (mFontSize>0)
			((TextView)v).setTextSize(mFontSize);

		v = findViewById(R_id_app_about_button_changelog);
		v.setOnClickListener(this);
		v = findViewById(R_id_app_about_button_faq);
		v.setOnClickListener(this);
		v = findViewById(R_id_app_about_button_email);
		v.setOnClickListener(this);
		v = findViewById(R_id_app_about_button_positive);
		v.setOnClickListener(this);
	}

	protected void openUrl(String aUrl) {
		Context theContext = getContext();
		Uri theUri = Uri.parse(aUrl);
		Intent theIntent = new Intent(Intent.ACTION_VIEW,theUri);
		theContext.startActivity(theIntent);
	}

	protected void openEmailComposer(CharSequence aChooserText) {
		Context theContext = getContext();
		Intent theIntent = WebUtils.newEmailIntent(theContext.getString(R_string_app_about_link_email));

		String theDefaultSubject = "~"+theContext.getString(R_string_app_name)+"~";
		theIntent.putExtra(Intent.EXTRA_SUBJECT,theDefaultSubject);

		theContext.startActivity(theIntent);//Intent.createChooser(theIntent,aChooserText));
	}

	@Override
	public void onClick(View v) {
		Context theContext = getContext();
		int theButtonId = v.getId();
		try {
			if (theButtonId==R_id_app_about_button_changelog) {
				openUrl(theContext.getString(R_string_app_about_link_changelog));
			} else if (theButtonId==R_id_app_about_button_faq) {
				openUrl(theContext.getString(R_string_app_about_link_faq));
			} else if (theButtonId==R_id_app_about_button_email) {
				openEmailComposer(((Button)v).getText());
			}
		} catch (Exception e) {
			Log.w(TAG,e.getMessage());
		}
		finish();
	}

}
