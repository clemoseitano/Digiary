package net.tanozin.digiary;

import android.os.Environment;
import android.provider.BaseColumns;

public final class Constants implements BaseColumns {
	public static final String SD_CARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String DEFAULT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/StudyCafe";
	public static final String PROFILE_PIC = Environment.getExternalStorageDirectory().getAbsolutePath() + "/StudyCafe/user/profile_thumb";
	public static final String NOTE_DIR = DEFAULT_DIR + "/Note";
	public static final String NOTE_TEXT_DIR = NOTE_DIR + "/Text";
	public static final String NOTE_IMAGE_DIR = NOTE_DIR + "/Images";
	public static final String NOTE_AUDIO_DIR = NOTE_DIR + "/Audio";
	public static final String NOTE_VIDEO_DIR = NOTE_DIR + "/Videos";
	public static final String MATERIAL_DIR = DEFAULT_DIR + "/Material";
	public static final String PREFERRED_DATE = "yyyy MMMM dd";
	public static final String PREFERRED_TIME = "hh:mm";
	public static final String PREFERRED_DATE_TIME = "yyyy MMMM dd, hh:mm";
	public static final boolean mounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	public static final String PIC = "profile.png";
	public static final String SORT_ORDER = "toc_sort_order";
	public static final String LAST_FILE = "last_file";
	public static final String FIRST_RUN = "first_run";
	public static final String VERTICAL_TIMELINE = "veritcal_timeline";
	public static final String PERIOD_LENGTH = "period_length";
	public static final String PREF_KEY_USER = "user_name";
	public static final String PREF_KEY_EMAIL = "email_address";
	public static final String PREF_KEY_NAME = "display_name";
	public static final String DP = "profile_pic";
	public static final String FILES_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileStudy/Media/";
	public static final String STORAGE_PATH_UPLOADS = "media";
}
