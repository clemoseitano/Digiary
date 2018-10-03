package net.tanozin.digiary.texttray;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.tanozin.digiary.Constants;
import net.tanozin.digiary.R;
import net.tanozin.digiary.fileexplorer.FileDialog;
import net.tanozin.digiary.fileexplorer.FileViewArrayAdapter;
import net.tanozin.digiary.note.NoteContentProvider;
import net.tanozin.digiary.note.NoteItem;

public class EditorActivity extends AppCompatActivity implements FileDialog.FileListener {
    // dialog ids
    //TODO: make this have a toolbar
    private final static int DIALOG_SAVE_FILE = 1;
    private final static int DIALOG_OPEN_FILE = 2;
    private final static int DIALOG_SHOULD_SAVE = 3;
    private final static int DIALOG_OVERWRITE = 4;
    private final static int DIALOG_SAVE_ERROR = 5;
    private final static int DIALOG_SAVE_ERROR_PERMISSIONS = 6;
    private final static int DIALOG_SAVE_ERROR_SDCARD = 7;
    private final static int DIALOG_READ_ERROR = 8;
    private final static int DIALOG_NOTFOUND_ERROR = 9;
    private final static int DIALOG_SHOULD_SAVE_INTENT = 13;
    private final static int DIALOG_MODIFIED = 14;

    private final static int DIALOG_RECENT_FILE_DIALOG = 12;

    // file format ids
    private final static int FILEFORMAT_NL = 1;
    private final static int FILEFORMAT_CR = 2;
    private final static int FILEFORMAT_CRNL = 3;

    // other activities
    private final static int REQUEST_CODE_PREFERENCES = 1;
    private final static int REQUEST_FILE_BROWSER_OPEN = 2;
    private final static int REQUEST_FILE_BROWSER_SAVE = 3;
    public static final String PLAIN_TEXT = "net.tanozin.digiary.texttray.plain_text";

    // some global variables
    protected EditText text = null;

    protected CharSequence filename = "";
    protected long lastModified = 0;
    protected boolean untitled = true;

    static private List<String> items = null;
    static private List<String> recentItems = null;

    protected AlertDialog openRecentDialog;
    protected ListView openRecentListView;
    protected static FileViewArrayAdapter recentFilesAdapter;

    private boolean creatingFile = false;
    private boolean savingFile = false;
    private boolean openingFile = false;
    private boolean openingError = false;
    private boolean openingRecent = false;
    private boolean sendingAttachment = false;
    private static CharSequence temp_filename = "";

    private boolean fromIntent = false;
    private boolean openingIntent = false;
    private Intent newIntent = null;

    private boolean fromSearch = false;
    private String queryString = "";

    private CharSequence errorFname = "File";
    private boolean errorSaving = false;

    private int fileformat;
    private boolean textChanged = true;//safer to use true, doesn't hurt to save empty file


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateStatus();
        updateOptions();

        Intent intent = getIntent();
        newIntent(intent);
    }

    private void updateStatus() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= 21) {
                actionBar.setElevation(0);
            }

            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.primary));
            }
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));

        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(new File(title.toString()).exists()?new File(title.toString()).getName():title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void createDialog(FileDialog.Mode mode, int requestCode) {
        FileDialog f = new FileDialog();
        Bundle arguments = new Bundle();
        arguments.putInt(FileDialog.ARG_REQUEST_CODE, requestCode);
        f.setMode(mode);
        f.setTitle("Pick File");
        f.setTempDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
        f.setArguments(arguments);
        f.show(getSupportFragmentManager(), "FileDialog");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        openingFile = false;
        creatingFile = false;

        switch (item.getItemId()) {
            case R.id.action_text_type:
                Intent plainIntent = new Intent(EditorActivity.this, EditorActivity.class);
                plainIntent.setAction(EditorActivity.PLAIN_TEXT);
                plainIntent.putExtra(EditorActivity.PLAIN_TEXT, text.getText().toString());
                startActivity(plainIntent);
                finish();
                break;
            case R.id.action_save:    // Save
                savingFile = true;
                if (untitled) {
                    createDialog(FileDialog.Mode.SAVE_FILE, DIALOG_SAVE_FILE);
                } else
                    saveNote(filename);
                break;
            case R.id.action_save_as: // Save as
                savingFile = true;
                createDialog(FileDialog.Mode.SAVE_FILE, DIALOG_SAVE_FILE);
                break;

            case R.id.action_recent: // Open Recent List
                openingRecent = true;

                if (isTextChanged())
                    showDialog(DIALOG_SHOULD_SAVE);
                else
                    showDialog(DIALOG_RECENT_FILE_DIALOG);

                break;
            case R.id.action_open: // Open
                openingFile = true;
                openingIntent = false;

                if (isTextChanged())
                    showDialog(DIALOG_SHOULD_SAVE);
                else {
                    createDialog(FileDialog.Mode.PICK_FILE, DIALOG_OPEN_FILE);
                }

                break;
            case R.id.action_settings: // Options
                startActivityForResult(new Intent(this, EditPreferences.class), REQUEST_CODE_PREFERENCES);

                break;
            case R.id.action_new: // Newfile
                creatingFile = true;
                if (isTextChanged())
                    showDialog(DIALOG_SHOULD_SAVE);
                else
                    createNew();

                break;

            case R.id.action_email_content: // Email Text
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text.getText());
                sendIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(sendIntent, "Send email with"));
                break;

            case R.id.action_search: // Trigger search
                this.onSearchRequested();
                break;

            case R.id.action_email_attachment: // Email Attachment
                sendingAttachment = true;

                if (isTextChanged())
                    showDialog(DIALOG_SHOULD_SAVE);
                else if (untitled) {
                    Toast.makeText(this, R.string.onSendEmptyMessage, Toast.LENGTH_SHORT).show();
                } else
                    sendAttachment();

                break;

            case R.id.action_send_uno: // Trigger search
                Toast.makeText(EditorActivity.this, "Add messenger", Toast.LENGTH_LONG).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    } // end onOptionsItemSelected()


    /****************************************************************
     * Random Functions
     */
    public void sendAttachment() {
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("message/rfc822");
        sIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filename));
        startActivity(Intent.createChooser(sIntent, "Send attachment with:"));

        sendingAttachment = false;
    } // end sendAttachment()

    public boolean isTextChanged()    // checks if the text has been changed
    {
        return textChanged;
    } // end isTextChanged()

    public static int countQuotes(String t) // count " in string
    {
        int i = -1;
        int count = -1;

        do {
            i = t.indexOf('"', i + 1);
            count++;
        } while (i != -1);

        return count;
    } // end countQuotes()

    // returns a file list for out autocomplete open and save dialogs
    private static File prevFile = null;

    public void getFileList(CharSequence s, AutoCompleteTextView acView) {
        File f = new File(s.toString());
        File pFile;

        // get the parent directory
        if (f.isDirectory() && s.charAt(s.length() - 1) == '/')
            pFile = f;
        else
            pFile = f.getParentFile();

        // if we have no text and give some defaults
        if (s.equals(""))
            pFile = new File("/");

        if (pFile == null)
            pFile = new File("/");

        // we the parent file is actually different then update it
        if (pFile != null && (prevFile == null || (prevFile != null && !prevFile.equals(pFile)))) {
            if (!pFile.canRead())
                items.clear();    // no permission so no items
            else {
                File[] files = new File[0];

                // get he file list if there is one
                if (pFile.isDirectory())
                    files = pFile.listFiles();

                // add all the items
                if (items == null)
                    items = new ArrayList<String>();
                else
                    items.clear();

                int i, length = files.length;
                for (i = 0; i < length; i++) {
                    if (files[i].isDirectory())
                        items.add(files[i].getPath() + "/");
                    else
                        items.add(files[i].getPath());
                }
            }

            // update the files list
            FileAutoCompleteArrayAdapter adapter = new FileAutoCompleteArrayAdapter(getBaseContext(), android.R.layout.simple_dropdown_item_1line, items);
            acView.setAdapter(adapter);
        }

        prevFile = pFile;
    } // end getFileList()


    /****************************************************************
     * Recent Files Functions
     */
    protected void readRecentFiles() {
        int i;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        int numFiles = prefs.getInt("rf_numfiles", 0);

        // clear the current list
        if (recentItems == null)
            recentItems = new ArrayList<String>();

        recentItems.clear();

        // start adding stuff
        for (i = 0; i < numFiles; i++) {
            recentItems.add(prefs.getString("rf_file" + i, i + ""));
        }
    } // end readRecentFiles()

    protected void addRecentFile(CharSequence f) {
        if (recentItems == null)
            readRecentFiles();

        // remove from list if it is already there
        int i;
        int length = recentItems.size();

        for (i = 0; i < length; i++) {
            String t = recentItems.get(i);
            if (t.equals(f.toString())) {
                recentItems.remove(i);
                i--;
                length--;
            }
        }

        // add the new file
        recentItems.add(0, f.toString());

        // make sure there are 7 max
        if (recentItems.size() > 7)
            recentItems.remove(7);

        // save this list in the preferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        //SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        for (i = 0; i < recentItems.size(); i++) {
            editor.putString("rf_file" + i, recentItems.get(i));
        }

        editor.putInt("rf_numfiles", recentItems.size());
        editor.commit();
    } // end addRecentFile()

    protected void removeRecentFile(CharSequence f) {
        if (recentItems == null)
            readRecentFiles();

        // remove from list if it is already there
        int i;
        int length = recentItems.size();
        for (i = 0; i < length; i++) {
            String t = recentItems.get(i);

            if (t.equals(f.toString())) {
                recentItems.remove(i);
                i--;
                length--;
            }
        }

        // save this list in the preferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        //SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        for (i = 0; i < recentItems.size(); i++) {
            editor.putString("rf_file" + i, recentItems.get(i));
        }

        editor.putInt("rf_numfiles", recentItems.size());
        editor.commit();
    } // end removeRecentFile();

    /****************************************************************
     * onNewIntent()
     */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        newIntent(intent);
    }

    /****************************************************************
     * newIntent()
     */
    public void newIntent(Intent intent) {
        setIntent(intent);
        Uri mUri;
        // search action
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestions.AUTHORITY, SearchSuggestions.MODE);
            suggestions.saveRecentQuery(query, null);

            int number = intent.getIntExtra("number", 0);
            number++;
            intent.putExtra("number", number);

            if (number == 1) {
                fromSearch = true;
                queryString = query;
            }
        } else if (PLAIN_TEXT.equals(intent.getAction())) {
            fromIntent = true;
            mUri = intent.getData();
            if (isTextChanged()) {
                openingIntent = true;
                newIntent = intent;
                showDialog(DIALOG_SHOULD_SAVE_INTENT);
            } else {
                openFile(mUri);
                text.setText(intent.getStringExtra(PLAIN_TEXT));
            }
        } else {

            // opening something action
            mUri = intent.getData();

            if (mUri != null) {
                myResume();

                // this is so we know if this intent was already displayed
                int number = intent.getIntExtra("number", 0);
                number++;
                intent.putExtra("number", number);

                // when you revist the app, from rotate or otherwise, the intent is still
                // with the app. So we don't want to reopen every time
                // I don't know if this is the best way to do this or not, but it works for me.
                if (!mUri.getPath().equals(filename) && number <= 1) {
                    // set stuff up
                    fromIntent = true;

                    // figure out what to do
                    if (!mUri.getPath().equals(filename) && isTextChanged()) {
                        openingIntent = true;
                        newIntent = intent;
                        showDialog(DIALOG_SHOULD_SAVE_INTENT);
                    } else if (!mUri.getPath().equals(filename)) {
                        openFile(mUri);
                    }
                }
            }
        }
    }

    /****************************************************************
     * onSaveInstanceState()
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putBoolean("creatingFile", creatingFile);
        savedInstanceState.putBoolean("savingFile", savingFile);
        savedInstanceState.putBoolean("openingFile", openingFile);
        savedInstanceState.putBoolean("openingError", openingError);
        savedInstanceState.putBoolean("openingRecent", openingRecent);
        savedInstanceState.putBoolean("openingIntent", openingIntent);
        savedInstanceState.putBoolean("sendingAttachment", sendingAttachment);

        savedInstanceState.putString("temp_filename", temp_filename.toString());

        super.onSaveInstanceState(savedInstanceState);
    } // end onSaveInstanceState()


    /****************************************************************
     * onRestoreInstanceState()
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        creatingFile = savedInstanceState.getBoolean("creatingFile");
        savingFile = savedInstanceState.getBoolean("savingFile");
        openingFile = savedInstanceState.getBoolean("openingFile");
        openingError = savedInstanceState.getBoolean("openingError");
        openingRecent = savedInstanceState.getBoolean("openingRecent");
        openingIntent = savedInstanceState.getBoolean("openingIntent");
        sendingAttachment = savedInstanceState.getBoolean("sendingAttachment");

        temp_filename = savedInstanceState.getString("temp_filename");
    } // onRestoreInstanceState()


    /****************************************************************
     * createNew()
     * create a new file
     */
    public void createNew() {
        text.setText("");
        setTitle(R.string.newFileName);

        // clear the saved text
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        if (editor != null) {
            editor.putInt("mode", 1);

            editor.putString("text", "");
            editor.putInt("text-quotes", 0);

            editor.putString("fntext", getTitle().toString());
            editor.putInt("fntext-quotes", countQuotes(getTitle().toString()));

            editor.putString("filename", "");
            editor.putInt("filename-quotes", 0);

            editor.putInt("selection-start", -1);
            editor.putInt("selection-end", -1);
            editor.apply();
        }

        fileformat = FILEFORMAT_NL;
        filename = "";
        lastModified = 0;
        untitled = true;

        creatingFile = false;

        updateOptions();
        text.requestFocus();

    } // end createNew();


    /****************************************************************
     * openFile(Uri mUri)
     * opens a file, duh
     */
    public void openFile(Uri mUri) {
        File ft = new File(mUri.getPath());

        if (ft.isFile()) {
            openFile(mUri.getPath());
            return;
        }

        // figure out file name
        int count = 0;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String location = sharedPref.getString("defaultdir", Constants.NOTE_TEXT_DIR);

        File f = new File(location + "/attachment");

        while (f.isFile()) {
            count++;
            f = new File(location + "/attachment" + count);
        }

        // now read in the file
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(mUri, "r");
            FileReader fis = new FileReader(pfd.getFileDescriptor());

            char[] buffer;
            buffer = new char[1100];    // made it bigger just in case

            StringBuffer result = new StringBuffer();

            int read = 0;

            do {
                read = fis.read(buffer);

                if (read >= 0) {
                    result.append(buffer, 0, read);
                }
            } while (read >= 0);

            openFile((CharSequence) f.toString(), result);
            removeRecentFile((CharSequence) f.toString());

            // indicate it hasn't been saved
            // put a little star in the title if the file is changed
            if (!isTextChanged()) {
                CharSequence temp = getTitle();
                setTitle("* " + temp);
            }

        } catch (Exception e) {
            // error reading file
            errorFname = "attachment";
            openingError = true;
            showDialog(DIALOG_READ_ERROR);
        }
    }

    /****************************************************************
     * openFile(CharSequence fname)
     * opens a file, duh
     */
    public void openFile(CharSequence fname) {
        openingFile = false;
        StringBuffer result = new StringBuffer();

        try {
            // open file
            FileReader f = new FileReader(fname.toString());
            File file = new File(fname.toString());

            if (f == null) {
                throw (new FileNotFoundException());
            }

            if (file.isDirectory()) {
                throw (new IOException());
            }

            // if the file has nothing in it there will be an exception here
            // that actually isn't a problem
            if (file.length() != 0 && !file.isDirectory()) {
                // using just FileReader now. Works better with weird file encoding
                // Thanks to Ondrej Bojar <obo@cuni.cz> for finding the bug.

                // read in the file
                //		do it this way because we need that newline at
                //		the end of the file if there is one
                char[] buffer;
                buffer = new char[1100];    // made it bigger just in case

                int read = 0;

                do {
                    read = f.read(buffer, 0, 1000);

                    if (read >= 0) {
                        result.append(buffer, 0, read);
                    }
                } while (read >= 0);
            }
        } catch (FileNotFoundException e) {
            // file not found
            errorFname = fname;
            openingError = true;
            showDialog(DIALOG_NOTFOUND_ERROR);
        } catch (IOException e) {
            // error reading file
            errorFname = fname;
            openingError = true;
            showDialog(DIALOG_READ_ERROR);
        } catch (Exception e) {
            errorFname = fname;
            openingError = true;
            showDialog(DIALOG_READ_ERROR);
        }

        // now figure out the file format, nl, cr, crnl
        if (!openingError) {
            openFile(fname, result);
        }

        errorSaving = false;
        if (text != null)
            text.requestFocus();
    } // end openFile(CharSequence fname)


    /****************************************************************
     * openFile(CharSequence fname, StringBuffer result)
     * opens a file, duh
     */
    public void openFile(CharSequence fname, StringBuffer result) {
        try {
            // have to do this first because it resets fileformat
            createNew(); // to clear everything out

            String newText = result.toString();

            if (newText.indexOf("\r\n", 0) != -1) {
                fileformat = FILEFORMAT_CRNL;
                newText = newText.replace("\r", "");
            } else if (newText.indexOf("\r", 0) != -1) {
                fileformat = FILEFORMAT_CR;
                newText = newText.replace("\r", "\n");
            } else {
                fileformat = FILEFORMAT_NL;
            }

            // Okay, now we can set everything up
            text.setText(newText);
            setTitle(fname);

            File f = new File(fname.toString());
            lastModified = f.lastModified();
            filename = fname;
            untitled = false;

            addRecentFile(fname);
            openingRecent = false;

            // this is just incase we get an error
        } catch (Exception e) {
            errorFname = fname;
            openingError = true;
            showDialog(DIALOG_READ_ERROR);
        }

        openingIntent = false;
        temp_filename = "";
    } // end openFile(CharSequence fname, StringBuffer result)


    /****************************************************************
     * saveNote()
     * What to do when saving note
     */
    public void saveNote(CharSequence fname) {
        errorSaving = false;

        // actually save the file here
        try {
            File f = new File(fname.toString());

            if ((f.exists() && !f.canWrite()) || (!f.exists() && !f.getParentFile().canWrite())) {
                creatingFile = false;
                openingFile = false;
                errorSaving = true;

                if (fname.toString().indexOf("/sdcard/") == 0)
                    showDialog(DIALOG_SAVE_ERROR_SDCARD);
                else
                    showDialog(DIALOG_SAVE_ERROR_PERMISSIONS);

                text.requestFocus();

                f = null;
                return;
            }
            f = null; // hopefully this gets garbage collected

            // Create file
            FileWriter fstream = new FileWriter(fname.toString());
            BufferedWriter out = new BufferedWriter(fstream);

            if (fileformat == FILEFORMAT_CR) {
                out.write(text.getText().toString().replace("\n", "\r"));
            } else if (fileformat == FILEFORMAT_CRNL) {
                out.write(text.getText().toString().replace("\n", "\r\n"));
            } else {
                out.write(text.getText().toString());
            }

            out.close();
            textChanged = false;
            NoteItem a = new NoteItem("text", fname.toString(), Uri.fromFile(new File(fname.toString())).toString());
            ContentValues value = a.getContentValues();
            Uri uri = getContentResolver().insert(NoteContentProvider.CONTENT_URI, value);
            if (uri != null) {
                getContentResolver().notifyChange(uri, null);
            }
            // give a nice little message
            Toast.makeText(this, R.string.onSaveMessage, Toast.LENGTH_SHORT).show();

            // the filename is the new title
            setTitle(fname);
            filename = fname;
            untitled = false;

            lastModified = (new File(filename.toString())).lastModified();

            temp_filename = "";

            addRecentFile(fname);
        } catch (Exception e) { //Catch exception if any
            creatingFile = false;
            openingFile = false;

            if (fname.toString().indexOf("/sdcard/") == 0)
                showDialog(DIALOG_SAVE_ERROR_SDCARD);
            else
                showDialog(DIALOG_SAVE_ERROR);

            errorSaving = true;
        }

        text.requestFocus();
    } // end saveNote()


    /****************************************************************
     * updateOptions()
     * start options app
     */
    protected void updateOptions() {
        boolean value;
        text = (EditText) findViewById(R.id.editor);
        // load the preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);


        text.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence one, int a, int b, int c) {

                // put a little star in the title if the file is changed

                CharSequence temp = getTitle();
                temp = temp.toString().replace("*", "");
                setTitle("*" + temp.toString());
                textChanged = true;

            }

            // complete the interface
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        /********************************
         * links clickable */
        boolean linksclickable = sharedPref.getBoolean("linksclickable", false);

        if (linksclickable)
            text.setAutoLinkMask(Linkify.ALL);
        else
            text.setAutoLinkMask(0);

        /********************************
         * show/hide filename */
        value = sharedPref.getBoolean("hidefilename", false);
        if (value)
            setTitle(R.string.app_name);

        /********************************
         * line wrap */
      /*  value = true;
        text.setHorizontallyScrolling(false);

        // setup the scroll view correctly
        ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
        if (scroll != null) {
            scroll.setFillViewport(true);
            scroll.setHorizontalScrollBarEnabled(false);
        }*/

        /********************************
         * font face */
        String font = sharedPref.getString("font", "Monospace");

        switch (font) {
            case "Serif":
                text.setTypeface(Typeface.SERIF);
                break;
            case "Sans Serif":
                text.setTypeface(Typeface.SANS_SERIF);
                break;
            default:
                text.setTypeface(Typeface.MONOSPACE);
                break;
        }

        /********************************
         * font size */
        String fontsize = sharedPref.getString("fontsize", "Medium");

        if (fontsize.equals("Extra Small"))
            text.setTextSize(12.0f);
        else if (fontsize.equals("Small"))
            text.setTextSize(16.0f);
        else if (fontsize.equals("Medium"))
            text.setTextSize(20.0f);
        else if (fontsize.equals("Large"))
            text.setTextSize(24.0f);
        else if (fontsize.equals("Huge"))
            text.setTextSize(28.0f);
        else
            text.setTextSize(20.0f);

        /********************************
         * Colors */
        int bgcolor = sharedPref.getInt("bgcolor", 0xFF000000);
        text.setBackgroundColor(bgcolor);

        int fontcolor = sharedPref.getInt("fontcolor", 0xFFCCCCCC);
        text.setTextColor(fontcolor);

        text.setLinksClickable(true);
    } // updateOptions()


    /****************************************************************
     * onActivityResult()
     * results of a launched activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // preferences returned to update the editor
        if (requestCode == REQUEST_CODE_PREFERENCES) {
            updateOptions();
        } else if (requestCode == REQUEST_FILE_BROWSER_OPEN && data != null) {

            createDialog(FileDialog.Mode.PICK_FILE, DIALOG_OPEN_FILE);

        } else if (requestCode == REQUEST_FILE_BROWSER_SAVE && data != null) {

            createDialog(FileDialog.Mode.SAVE_FILE, DIALOG_SAVE_FILE);
        }
    } // end onActivityResult()


    /****************************************************************
     * onPause()
     * What happens when you pause the app
     */
    protected void onPause() {
        super.onPause();

        // save the preferences
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        if (editor != null && text != null) {
            String t;

            t = text.getText().toString();
            editor.putString("text", t);
            editor.putInt("text-quotes", countQuotes(t));

            t = getTitle().toString();
            editor.putString("fntext", t);
            editor.putInt("fntext-quotes", countQuotes(t));

            if (filename != null)
                t = filename.toString();
            else
                t = "";
            editor.putString("filename", t);
            editor.putLong("lastModified", lastModified);
            editor.putInt("filename-quotes", countQuotes(t));

            editor.putInt("selection-start", text.getSelectionStart());
            editor.putInt("selection-end", text.getSelectionEnd());

            editor.putInt("fileformat", fileformat);

            editor.putBoolean("openingIntent", openingIntent);
            editor.apply();
        }
    } // end onPause()


    /****************************************************************
     * onResume()
     * What happens when you resume the app
     */
    protected void onResume() {
        super.onResume();

        if (!fromIntent)
            myResume();

        fromIntent = false;
    }

    private void myResume() {
        try {
            // make sure the correct options are being used
            updateOptions();

            // load the previous text
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            String restoredText = prefs.getString("text", null);
            String titleText = prefs.getString("fntext", null);

            openingIntent = prefs.getBoolean("openingIntent", false);

            int selectionStart = prefs.getInt("selection-start", -1);
            int selectionEnd = prefs.getInt("selection-end", -1);

            lastModified = prefs.getLong("lastModified", lastModified);
            filename = prefs.getString("filename", null);
            if (filename == null || filename == "")
                untitled = true;
            else
                untitled = false;

            fileformat = prefs.getInt("fileformat", FILEFORMAT_NL);

            // clear double quote problem
            if (restoredText != null) {
                int nq = prefs.getInt("text-quotes", 0);
                if (nq != 0 && countQuotes(restoredText) == 2 * nq)
                    restoredText = restoredText.replaceAll("\"\"", "\"");
            }

            if (titleText != null) {
                int nq = prefs.getInt("fntext-quotes", 0);
                if (nq != 0 && countQuotes(titleText) == 2 * nq)
                    titleText = titleText.replaceAll("\"\"", "\"");
            }

            if (filename != null) {
                int nq = prefs.getInt("filename-quotes", 0);
                if (nq != 0 && countQuotes(filename.toString()) == 2 * nq)
                    filename = filename.toString().replaceAll("\"\"", "\"");
            }


            if (restoredText != null && text != null) {
                text.setText(restoredText);

                if (selectionStart != -1 && selectionEnd != -1) {
                    text.setSelection(selectionStart, selectionEnd);
                }
            }

            if (titleText != null) {
                setTitle(titleText);
            }

            if (text != null)
                text.requestFocus();

            // for some reason I have to reset the text at the end to get the caret at the beginning
            // this has to happen at the end for some reason. I think it needs a little bit of delay.
            if (text != null && (selectionStart == 0 && selectionEnd == 0)) {
                if (restoredText != null) {
                    text.setText(restoredText);
                }

                if (titleText != null) {
                    setTitle(titleText);
                }
            }

            // search search search
            if (fromSearch) {
                int start;
                String t = "";
                if ((text != null ? text.getText() : null) != null)
                    t = (text.getText().toString().toLowerCase());

                start = t.indexOf(queryString.toLowerCase(), selectionStart + 1);
                if (start == -1)    // loop search
                    start = t.indexOf(queryString.toLowerCase(), 0);

                if (start != -1) {
                    text.setSelection(start, start + queryString.length());
                } else {
                    Toast.makeText(this, "\"" + queryString + "\" not found", Toast.LENGTH_SHORT).show();
                }

                fromSearch = false;
            }
        } catch (Exception e) {
            createNew();
        }

        // figure out if the the file has been previously modified
        if (!creatingFile && !savingFile && !openingFile && !openingError && !openingRecent
                && !openingIntent && !sendingAttachment) {
            if (lastModified != 0 && lastModified != (new File(filename.toString())).lastModified()) {
                showDialog(DIALOG_MODIFIED);
            }
        }
    } // end onResume()


    /****************************************************************
     * onPrepareDialog()
     * This function is called EVERY time a dialog is displayed
     */
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            // just update the messages for errors
            case DIALOG_NOTFOUND_ERROR:
                dialog.setTitle(errorFname + " not found");
                break;

            case DIALOG_READ_ERROR:
                dialog.setTitle("Error reading " + errorFname);
                break;

            case DIALOG_RECENT_FILE_DIALOG: {
                // make sure we have the most recent recent files in this dialog
                readRecentFiles();
                recentFilesAdapter.notifyDataSetChanged();

                openRecentListView.setSelection(0);
            }
        }
    } // onPrepareDialog()


    /****************************************************************
     * onCreateDialog()
     * This function is called the FIRST time a dialog is displayed
     */
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_RECENT_FILE_DIALOG: {
                // read the file list
                readRecentFiles();

                // update the files list
                recentFilesAdapter = new FileViewArrayAdapter(getBaseContext(), recentItems);

                // custom listview so that we can put the list back up to the top automatically
                // in the on prepare
                LayoutInflater factory = LayoutInflater.from(this);
                openRecentListView = (ListView) factory.inflate(R.layout.openrecent_list, null);


                openRecentListView.setAdapter(recentFilesAdapter);
                openRecentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        openFile(recentItems.get(arg2));
                        openRecentDialog.dismiss();
                    }
                });

                openRecentDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.openRecent)
                        .setView(openRecentListView)
                        .setInverseBackgroundForced(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                openingRecent = false;
                            }
                        })
                        .create();

                return openRecentDialog;
            }

            case DIALOG_SAVE_ERROR_PERMISSIONS: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.accessDenied)
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // what to do when positive clicked
                                if (!openingRecent) {
                                    createDialog(FileDialog.Mode.SAVE_FILE, DIALOG_SAVE_FILE);
                                }
                            }
                        })
                        .create();
            }

            case DIALOG_SAVE_ERROR_SDCARD: {
                return new AlertDialog.Builder(this)
                        .setMessage(R.string.accessDeniedSDcard)
//				.setTitle(R.string.accessDeniedSDcard)
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // what to do when positive clicked
//						if (!openingRecent)
                                {
                                    createDialog(FileDialog.Mode.SAVE_FILE, DIALOG_SAVE_FILE);
                                }
                            }
                        })
                        .create();
            }

            case DIALOG_SAVE_ERROR: {
                return new AlertDialog.Builder(this)
                        .setMessage(R.string.savingError)
//					.setTitle(R.string.savingError)
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // what to do when positive clicked
//							if (!openingRecent)
                                {
                                    createDialog(FileDialog.Mode.SAVE_FILE, DIALOG_SAVE_FILE);
                                }
                            }
                        })
                        .create();
            }

            case DIALOG_NOTFOUND_ERROR: {
                return new AlertDialog.Builder(this)
                        .setTitle(errorFname + " not found")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // what to do when positive clicked
                                // do nothing, just letting the user know
                                if (openingRecent) {
                                    removeRecentFile(errorFname);
                                } else {
                                    createDialog(FileDialog.Mode.PICK_FILE, DIALOG_OPEN_FILE);
                                }
                            }
                        })
                        .create();
            }

            case DIALOG_READ_ERROR: {
                return new AlertDialog.Builder(this)
                        .setTitle("Error reading " + errorFname)
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // what to do when positive clicked
                                // do nothing, just letting the user know
                                if (!openingIntent) {
                                    createDialog(FileDialog.Mode.PICK_FILE, DIALOG_OPEN_FILE);
                                }
                            }
                        })
                        .create();
            }

            case DIALOG_SHOULD_SAVE_INTENT:
            case DIALOG_SHOULD_SAVE: {
                int t;
                if (id == DIALOG_SHOULD_SAVE_INTENT)
                    t = R.string.shouldSaveIntent;
                else
                    t = R.string.shouldSave;

                return new AlertDialog.Builder(this)
                        .setTitle(t)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                savingFile = true;

                                if (untitled) {
                                    createDialog(FileDialog.Mode.SAVE_FILE, DIALOG_SAVE_FILE);
                                } else {
                                    saveNote(filename);

                                    if (!errorSaving && openingRecent)
                                        showDialog(DIALOG_RECENT_FILE_DIALOG);

                                    if (!errorSaving && creatingFile)
                                        createNew();

                                    if (!errorSaving && openingFile) {
                                        createDialog(FileDialog.Mode.PICK_FILE, DIALOG_OPEN_FILE);
                                    }

                                    if (!errorSaving && openingIntent) {
                                        Uri mUri;

                                        if (newIntent != null) {
                                            mUri = newIntent.getData();
                                            if (PLAIN_TEXT.equals(newIntent.getAction())) {
                                                text.setText(newIntent.getStringExtra(PLAIN_TEXT));
                                            } else openFile(mUri);
                                        } else {
                                            mUri = getIntent().getData();

                                            openFile(mUri);
                                        }
                                    }

                                    if (!errorSaving && sendingAttachment)
                                        sendAttachment();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sendingAttachment = false;

                                if (creatingFile)
                                    createNew();

                                if (openingRecent)
                                    showDialog(DIALOG_RECENT_FILE_DIALOG);

                                if (openingFile) {
                                    createDialog(FileDialog.Mode.PICK_FILE, DIALOG_OPEN_FILE);
                                }

                                if (openingIntent) {
                                    Uri mUri;

                                    if (newIntent != null){
                                        mUri = newIntent.getData();
                                        if (PLAIN_TEXT.equals(newIntent.getAction())) {
                                            text.setText(newIntent.getStringExtra(PLAIN_TEXT));
                                            mUri=null;
                                        }
                                    }
                                    else
                                        mUri = getIntent().getData();

                                    if (mUri != null && !mUri.getPath().equals(filename))
                                        openFile(mUri);
                                }
                            }
                        })
                        .create();
            }
            case DIALOG_MODIFIED: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.externalModify)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                openFile(filename);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                lastModified = (new File(filename.toString())).lastModified();
                            }
                        })
                        .create();
            }

            case DIALOG_OVERWRITE: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.shouldOverwrite)
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                saveNote(temp_filename);

                                if (!errorSaving && openingRecent)
                                    showDialog(DIALOG_RECENT_FILE_DIALOG);

                                if (!errorSaving && creatingFile)
                                    createNew();

                                if (!errorSaving && openingFile) {
                                    createDialog(FileDialog.Mode.PICK_FILE, DIALOG_OPEN_FILE);
                                }

                                if (!errorSaving && sendingAttachment)
                                    sendAttachment();

                                if (!errorSaving && openingIntent)
                                    openFile(getIntent().getData());

                                if (!errorSaving)
                                    temp_filename = "";
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                createDialog(FileDialog.Mode.SAVE_FILE, DIALOG_SAVE_FILE);
                            }
                        })
                        .create();
            }
        }

        return null;
    } // end onCreateDialog()

    @Override
    public void onFileReturned(int requestcode, File f) {
        switch (requestcode) {
            case DIALOG_SAVE_FILE:
                boolean exists = f.exists();
                boolean isdir = f.isDirectory();
                boolean canwrite = f.canWrite();

                if (exists && !isdir && canwrite) {
                    temp_filename = f.getAbsolutePath();
                    showDialog(DIALOG_OVERWRITE);
                } else {
                    // this will handle some of the other errors.
                    saveNote(f.getAbsolutePath());

                    savingFile = false;

                    if (!errorSaving && openingRecent)
                        showDialog(DIALOG_RECENT_FILE_DIALOG);

                    if (!errorSaving && creatingFile)
                        createNew();

                    if (!errorSaving && openingFile) {
                        createDialog(FileDialog.Mode.PICK_FILE, DIALOG_OPEN_FILE);
                    }

                    if (!errorSaving && sendingAttachment)
                        sendAttachment();

                    if (!errorSaving && openingIntent && getIntent().getData() != null)
                        openFile(getIntent().getData());
                }
                break;
            case DIALOG_OPEN_FILE:
                openFile(f.getAbsolutePath());
                break;
        }
    }
}
