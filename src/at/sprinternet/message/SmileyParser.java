/*
 * Copyright (C) 2012 Christoph Haas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.sprinternet.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.SecureSMS;

/**
 * A class for annotating a CharSequence with spans to convert textual emoticons
 * to graphical ones.
 */
public class SmileyParser {
    private final Context mContext;
    private final String[] mSmileyTexts;
    private final Pattern mPattern;
    private final HashMap<String, Integer> mSmileyToRes;
    // NOTE: if you change anything about this array, you must make the corresponding change
    // to the string arrays: default_smiley_texts and default_smiley_names in res/values/arrays.xml
    public final int[] DEFAULT_SMILEY_RES_IDS = new int[27];
    
    static class Smileys {
    	private final Context mContext;
    	
        private static final int[] sIconIdsCustom = {
            R.drawable.emo_im_happy,
            R.drawable.emo_im_sad,
            R.drawable.emo_im_winking,
            R.drawable.emo_im_tongue_sticking_out,
            R.drawable.emo_im_surprised,
            R.drawable.emo_im_kissing,
            R.drawable.emo_im_yelling,
            R.drawable.emo_im_cool,
            R.drawable.emo_im_money_mouth,
            R.drawable.emo_im_foot_in_mouth,
            R.drawable.emo_im_embarrassed,
            R.drawable.emo_im_angel,
            R.drawable.emo_im_undecided,
            R.drawable.emo_im_crying,
            R.drawable.emo_im_lips_are_sealed,
            R.drawable.emo_im_laughing,
            R.drawable.emo_im_wtf,
            R.drawable.emo_im_heart_new,
            R.drawable.emo_im_mad,
            R.drawable.emo_im_smirk,
            R.drawable.emo_im_pokerface
        };
        
        private static final int[] sIconIdsICS = {
            R.drawable.emo_im_happy,
            R.drawable.emo_im_sad,
            R.drawable.emo_im_winking,
            R.drawable.emo_im_tongue_sticking_out,
            R.drawable.emo_im_surprised,
            R.drawable.emo_im_kissing,
            R.drawable.emo_im_yelling,
            R.drawable.emo_im_cool,
            R.drawable.emo_im_money_mouth,
            R.drawable.emo_im_foot_in_mouth,
            R.drawable.emo_im_embarrassed,
            R.drawable.emo_im_angel,
            R.drawable.emo_im_undecided,
            R.drawable.emo_im_crying,
            R.drawable.emo_im_lips_are_sealed,
            R.drawable.emo_im_laughing,
            R.drawable.emo_im_wtf,
            R.drawable.emo_im_heart_smiley,
            R.drawable.emo_im_mad,
            R.drawable.emo_im_smirk,
            R.drawable.emo_im_pokerface
        };
        
        private static final int[] sIconIdsOld = {
            R.drawable.emo_im_old_happy,
            R.drawable.emo_im_old_sad,
            R.drawable.emo_im_old_winking,
            R.drawable.emo_im_old_tongue_sticking_out,
            R.drawable.emo_im_old_surprised,
            R.drawable.emo_im_old_kissing,
            R.drawable.emo_im_old_yelling,
            R.drawable.emo_im_old_cool,
            R.drawable.emo_im_old_money_mouth,
            R.drawable.emo_im_old_foot_in_mouth,
            R.drawable.emo_im_old_embarrassed,
            R.drawable.emo_im_old_angel,
            R.drawable.emo_im_old_undecided,
            R.drawable.emo_im_old_crying,
            R.drawable.emo_im_old_lips_are_sealed,
            R.drawable.emo_im_old_laughing,
            R.drawable.emo_im_old_wtf,
            -1,
            -1,
            -1,
            -1
        };

        public static int HAPPY = 0;
        public static int SAD = 1;
        public static int WINKING = 2;
        public static int TONGUE_STICKING_OUT = 3;
        public static int SURPRISED = 4;
        public static int KISSING = 5;
        public static int YELLING = 6;
        public static int COOL = 7;
        public static int MONEY_MOUTH = 8;
        public static int FOOT_IN_MOUTH = 9;
        public static int EMBARRASSED = 10;
        public static int ANGEL = 11;
        public static int UNDECIDED = 12;
        public static int CRYING = 13;
        public static int LIPS_ARE_SEALED = 14;
        public static int LAUGHING = 15;
        public static int WTF = 16;
        public static int MAD = 17;
        public static int HEART = 18;
        public static int SMIRK = 19;
        public static int POKERFACE = 20;
        
        public Smileys(Context context) {
            mContext = context;
        }

        // choose smiley package
    	public int getSmileyResource(int which) {
    		if("ics".equals(PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("pref_smiley_package", "custom"))) {
    			return sIconIdsICS[which];
    		} else if("old".equals(PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("pref_smiley_package", "custom"))) {
    			return sIconIdsOld[which];
    		} else {
    			return sIconIdsCustom[which];
    		}
        }	
    }

    public SmileyParser(Context context) {
        mContext = context;       
        
        // NOTE: if you change anything about this array, you must make the corresponding change
        // to the string arrays: default_smiley_texts and default_smiley_names in res/values/arrays.xml
        Smileys s = new Smileys(mContext);
        
        this.DEFAULT_SMILEY_RES_IDS[0] = s.getSmileyResource(Smileys.HAPPY); 
        this.DEFAULT_SMILEY_RES_IDS[1] = s.getSmileyResource(Smileys.HAPPY); 
        this.DEFAULT_SMILEY_RES_IDS[2] = s.getSmileyResource(Smileys.SAD); 
        this.DEFAULT_SMILEY_RES_IDS[3] = s.getSmileyResource(Smileys.SAD); 
        this.DEFAULT_SMILEY_RES_IDS[4] = s.getSmileyResource(Smileys.WINKING); 
        this.DEFAULT_SMILEY_RES_IDS[5] = s.getSmileyResource(Smileys.WINKING); 
        this.DEFAULT_SMILEY_RES_IDS[6] = s.getSmileyResource(Smileys.TONGUE_STICKING_OUT); 
        this.DEFAULT_SMILEY_RES_IDS[7] = s.getSmileyResource(Smileys.TONGUE_STICKING_OUT); 
        this.DEFAULT_SMILEY_RES_IDS[8] = s.getSmileyResource(Smileys.SURPRISED); 
        this.DEFAULT_SMILEY_RES_IDS[9] = s.getSmileyResource(Smileys.SURPRISED); 
        this.DEFAULT_SMILEY_RES_IDS[10] = s.getSmileyResource(Smileys.KISSING); 
        this.DEFAULT_SMILEY_RES_IDS[11] = s.getSmileyResource(Smileys.YELLING); 
        this.DEFAULT_SMILEY_RES_IDS[12] = s.getSmileyResource(Smileys.COOL); 
        this.DEFAULT_SMILEY_RES_IDS[13] = s.getSmileyResource(Smileys.MONEY_MOUTH); 
        this.DEFAULT_SMILEY_RES_IDS[14] = s.getSmileyResource(Smileys.FOOT_IN_MOUTH); 
        this.DEFAULT_SMILEY_RES_IDS[15] = s.getSmileyResource(Smileys.EMBARRASSED); 
        this.DEFAULT_SMILEY_RES_IDS[16] = s.getSmileyResource(Smileys.ANGEL); 
        this.DEFAULT_SMILEY_RES_IDS[17] = s.getSmileyResource(Smileys.UNDECIDED); 
        this.DEFAULT_SMILEY_RES_IDS[18] = s.getSmileyResource(Smileys.CRYING); 
        this.DEFAULT_SMILEY_RES_IDS[19] = s.getSmileyResource(Smileys.LIPS_ARE_SEALED); 
        this.DEFAULT_SMILEY_RES_IDS[20] = s.getSmileyResource(Smileys.LAUGHING); 
        this.DEFAULT_SMILEY_RES_IDS[21] = s.getSmileyResource(Smileys.LAUGHING); 
        this.DEFAULT_SMILEY_RES_IDS[22] = s.getSmileyResource(Smileys.WTF); 
        this.DEFAULT_SMILEY_RES_IDS[23] = s.getSmileyResource(Smileys.MAD); 
        this.DEFAULT_SMILEY_RES_IDS[24] = s.getSmileyResource(Smileys.HEART); 
        this.DEFAULT_SMILEY_RES_IDS[25] = s.getSmileyResource(Smileys.SMIRK); 
        this.DEFAULT_SMILEY_RES_IDS[26] = s.getSmileyResource(Smileys.POKERFACE);
        
        mSmileyTexts = mContext.getResources().getStringArray(DEFAULT_SMILEY_TEXTS);
        mSmileyToRes = buildSmileyToRes();
        mPattern = buildPattern();
    }

    public static final int DEFAULT_SMILEY_TEXTS = R.array.default_smiley_texts;
    public static final int DEFAULT_SMILEY_NAMES = R.array.default_smiley_names;

    /**
     * Builds the hashtable we use for mapping the string version
     * of a smiley (e.g. ":-)") to a resource ID for the icon version.
     */
    private HashMap<String, Integer> buildSmileyToRes() {
        if (this.DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
            // Throw an exception if someone updated DEFAULT_SMILEY_RES_IDS
            // and failed to update arrays.xml
            throw new IllegalStateException("Smiley resource ID/text mismatch, length "+mSmileyTexts.length);
        }

        HashMap<String, Integer> smileyToRes =
                            new HashMap<String, Integer>(mSmileyTexts.length);
        for (int i = 0; i < mSmileyTexts.length; i++) {
            smileyToRes.put(mSmileyTexts[i], this.DEFAULT_SMILEY_RES_IDS[i]);
        }

        return smileyToRes;
    }

    /**
     * Builds the regular expression we use to find smileys in {@link #addSmileySpans}.
     */
    private Pattern buildPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        StringBuilder patternString = new StringBuilder(mSmileyTexts.length * 3);

        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        patternString.append('(');
        for (String s : mSmileyTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length() - 1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }


    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such
     * as :-) with a graphical version.
     *
     * @param text A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any
     *         recognized emoticons.
     */
    public CharSequence addSmileySpans(CharSequence text) {
    	
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        if(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_parse_smileys", true)) {
	        Matcher matcher = mPattern.matcher(text);
	        while (matcher.find()) {
	            int resId = mSmileyToRes.get(matcher.group());
	            if(resId != -1) {
	            	String masterScaleStr = PreferenceManager.getDefaultSharedPreferences(mContext).getString("pref_smiley_scale", "1.0");
	            	float masterScale = new Float(masterScaleStr);
	            	float scaleWidth = 0.7f;
	            	float scaleHeight = 1.0f;
	            	int boundBottom = 0;
	            	
	            	if("old".equals(PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("pref_smiley_package", "custom")) ||
	            	   "ics".equals(PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("pref_smiley_package", "custom"))	) {
		            	
	            		scaleWidth = 1.0f;
		            	scaleHeight = 1.0f;
	            	}
	            	if("old".equals(PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("pref_smiley_package", "custom"))) {
	            		boundBottom = 10;
	            	}
	            	
		            BitmapDrawable d = (BitmapDrawable)mContext.getResources().getDrawable(resId);
		            d.setBounds(0, d.getIntrinsicWidth(), d.getIntrinsicHeight(), boundBottom );
		            Bitmap b = Bitmap.createScaledBitmap(d.getBitmap(),
		                    (int) (d.getIntrinsicWidth() * (scaleWidth*masterScale)),
		                    (int) (d.getIntrinsicHeight() * (scaleHeight*masterScale)),
		                    false);
		            
		            builder.setSpan(new ImageSpan(b),
		                            matcher.start(), matcher.end(),
		                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
	        }
        }

        return builder;
    }
}


