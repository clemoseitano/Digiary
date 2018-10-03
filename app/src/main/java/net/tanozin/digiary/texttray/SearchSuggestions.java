package net.tanozin.digiary.texttray;

import android.content.SearchRecentSuggestionsProvider;

/* SearchSuggestions
 * 		Pretty simple to provide search suggestions */
public class SearchSuggestions extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "comm.studycafe.texttray.authority";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestions() {
        setupSuggestions(AUTHORITY, MODE);
    }
} // end class SearchSuggestions