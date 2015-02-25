package com.blackmoonit.androidbits.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.blackmoonit.androidbits.app.AppPreferenceBase;

/**
 * The About dialog for the app.
 */
public class DialogAbout extends Dialog implements View.OnClickListener {
	private static final String TAG = "BITS.lib.dialog.DialogAbout";

	protected static int getResId(Context aContext, String aResType, String aResName) {
		return aContext.getApplicationContext().getResources().getIdentifier(aResName,aResType,
				aContext.getPackageName());
	}

	protected int getResId(String aResType, String aResName) {
		return getResId(getContext(),aResType,aResName);
	}

	private int R_layout_dialog_about = 0;
	private int R_drawable_app_icon = 0;
	private int R_string_app_name = 0;
	private int R_string_dialog_about_version_display = 0;
	private int R_string_version_name = 0;
	private int R_string_dialog_about_text_thanks = 0;
	private int R_string_dialog_about_link_changelog = 0;
	private int R_string_dialog_about_link_faq = 0;
	private int R_string_dialog_about_link_email = 0;
	private int R_id_dialog_about_app_version = 0;
	private int R_id_dialog_about_text_thanks = 0;
	private int R_id_dialog_about_text_author = 0;
	private int R_id_dialog_about_button_changelog = 0;
	private int R_id_dialog_about_button_faq = 0;
	private int R_id_dialog_about_button_email = 0;
	private int R_id_dialog_about_button_positive = 0;

	private void getResIds() {
		String thePackageName = getContext().getPackageName();
		Resources theRes = getContext().getResources();
		R_layout_dialog_about = theRes.getIdentifier("dialog_about","layout",thePackageName);
		R_drawable_app_icon = theRes.getIdentifier("app_icon","drawable",thePackageName);
		R_string_app_name = theRes.getIdentifier("app_name","string",thePackageName);
		R_string_dialog_about_version_display = theRes.getIdentifier("dialog_about_version_display","string",thePackageName);
		R_string_version_name = theRes.getIdentifier("version_name","string",thePackageName);
		R_string_dialog_about_text_thanks = theRes.getIdentifier("dialog_about_text_thanks","string",thePackageName);
		R_string_dialog_about_link_changelog = theRes.getIdentifier("dialog_about_link_changelog","string",thePackageName);
		R_string_dialog_about_link_faq = theRes.getIdentifier("dialog_about_link_faq","string",thePackageName);
		R_string_dialog_about_link_email = theRes.getIdentifier("dialog_about_link_email","string",thePackageName);
		R_id_dialog_about_app_version = theRes.getIdentifier("dialog_about_app_version","id",thePackageName);
		R_id_dialog_about_text_thanks = theRes.getIdentifier("dialog_about_text_thanks","id",thePackageName);
		R_id_dialog_about_text_author = theRes.getIdentifier("dialog_about_text_author","id",thePackageName);
		R_id_dialog_about_button_changelog = theRes.getIdentifier("dialog_about_button_changelog","id",thePackageName);
		R_id_dialog_about_button_faq = theRes.getIdentifier("dialog_about_button_faq","id",thePackageName);
		R_id_dialog_about_button_email = theRes.getIdentifier("dialog_about_button_email","id",thePackageName);
		R_id_dialog_about_button_positive = theRes.getIdentifier("ButtonPositive","id",thePackageName);
	}

	//fields
	protected int mFontSize = 0;

	public DialogAbout(Context aContext) {
		super(aContext);
		getResIds();

		setTitle(R_string_app_name);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R_layout_dialog_about);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,R_drawable_app_icon);

		getPrefs(AppPreferenceBase.getPrefs(getContext()));
		setup();
	}

	protected void getPrefs(SharedPreferences aSettings) {
		if (aSettings!=null) {
			//app wide UI prefs would go here, like FontSize.
        }
	}

	protected void setup() {
		Context theContext = getContext();
		View v;
		String theText;

		theText = theContext.getString(R_string_dialog_about_version_display,
				theContext.getString(R_string_version_name,AppPreferenceBase.getAppVersionName(theContext)));
		v = findViewById(R_id_dialog_about_app_version);
		((TextView)v).setText(Html.fromHtml(theText));
		if (mFontSize>0)
			((TextView)v).setTextSize(mFontSize);

		theText = theContext.getString(R_string_dialog_about_text_thanks);
		v = findViewById(R_id_dialog_about_text_thanks);
		((TextView)v).setText(Html.fromHtml(theText));
		if (mFontSize>0)
			((TextView)v).setTextSize(mFontSize);
		Linkify.addLinks((TextView)v,Linkify.ALL);
		((TextView)v).setMovementMethod(LinkMovementMethod.getInstance());

		v = findViewById(R_id_dialog_about_text_author);
		if (mFontSize>0)
			((TextView)v).setTextSize(mFontSize);

		v = findViewById(R_id_dialog_about_button_changelog);
		v.setOnClickListener(this);
		v = findViewById(R_id_dialog_about_button_faq);
		v.setOnClickListener(this);
		v = findViewById(R_id_dialog_about_button_email);
		v.setOnClickListener(this);
		v = findViewById(R_id_dialog_about_button_positive);
		v.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		dismiss();

		Uri theUri = null;
		Intent theIntent;
		int theButtonId = v.getId();
		try {
			if (theButtonId==R_id_dialog_about_button_changelog) {
				theIntent = new Intent(Intent.ACTION_VIEW);
				theUri = Uri.parse(getContext().getString(R_string_dialog_about_link_changelog));
				theIntent.setData(theUri);
				getContext().startActivity(theIntent);
			} else if (theButtonId==R_id_dialog_about_button_faq) {
				theIntent = new Intent(Intent.ACTION_VIEW);
				theUri = Uri.parse(getContext().getString(R_string_dialog_about_link_faq));
				theIntent.setData(theUri);
				getContext().startActivity(theIntent);
			} else if (theButtonId==R_id_dialog_about_button_email) {
				theIntent = new Intent(Intent.ACTION_SEND);
				theIntent.setType("text/plain");
				theIntent.putExtra(Intent.EXTRA_EMAIL,
						new String[] { getContext().getString(R_string_dialog_about_link_email) });
				theIntent.putExtra(Intent.EXTRA_SUBJECT,
						"~"+getContext().getString(R_string_app_name)+"~");
				getContext().startActivity(Intent.createChooser(theIntent,
						((Button)findViewById(R_id_dialog_about_button_email)).getText()));
			}
		} catch (Exception e) {
			Log.w(TAG,e.getMessage());
		}
	}

}
