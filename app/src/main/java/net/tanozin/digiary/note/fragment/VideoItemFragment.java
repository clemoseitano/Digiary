package net.tanozin.digiary.note.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.tanozin.digiary.R;
import net.tanozin.digiary.note.NoteContentProvider;
import net.tanozin.digiary.note.NoteItem;
import net.tanozin.digiary.note.NoteItemAdapter;
import net.tanozin.digiary.note.VideoPlayer;
import net.tanozin.digiary.note.activity.TextActivity;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */

public class VideoItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.OnItemClickListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    public List<NoteItem> ITEMS = new ArrayList<>();
    private static final String PREF_DELETE = "sync_frequency";
    
    public NoteItemAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VideoItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static VideoItemFragment newInstance(int columnCount) {
        VideoItemFragment fragment = new VideoItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* int i = 0;
        while (i < 20) {
            NoteItem a = new NoteItem("image", "Hello " + (++i), "dummy");
            ITEMS.add(a);
        }
        */
        adapter = new NoteItemAdapter(this.getContext(), ITEMS);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        getLoaderManager().initLoader(0, null, this);
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden)
            getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_textitem, container, false);

        // Set the adapter

        Context context = view.getContext();
        final AbsListView recyclerView = (AbsListView) view.findViewById(R.id.list);
        recyclerView.setOnItemClickListener(this);
        ((AdapterView<ListAdapter>) recyclerView).setAdapter(adapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            recyclerView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        recyclerView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private int nr = 0;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                adapter.clearSelection();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub

                nr = 0;
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.menu_note_list, menu);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean enableShare = sp.getBoolean("example_switch", false);
                if(!enableShare){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                    }
                    menu.findItem(R.id.action_share).setEnabled(false);}
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem mitem) {
                // TODO Auto-generated method stub
                switch (mitem.getItemId()) {

                    case R.id.action_delete: {
                        nr = 0;
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        int del = 1;
                        if (sp.getString(PREF_DELETE, "1").equals("2"))
                            del = 2;
                        int count = 0;
                        for (int a : adapter.getCurrentCheckedPosition()) {
                            NoteItem item = (NoteItem) adapter.getItem(a);
                            String where = NoteContentProvider.KEY_NAME+ " = ?;";
                            String[] array = new String[]{item.getName()};

                            if (del == 2) {
                                new File(item.getLink()).delete();
                            }
                            getContext().getContentResolver().delete(NoteContentProvider.CONTENT_URI, where, array);
                            Log.d("COUNT ", Integer.toString(count++));
                            adapter.notifyDataSetChanged();
                        }
                        adapter.clearSelection();
                        getLoaderManager().restartLoader(0, null, VideoItemFragment.this);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            mode.finish();
                        }
                        break;
                    }
                    case R.id.action_share: {
                        ArrayList<Uri> audioUris = new ArrayList<>();
                        for (int a : adapter.getCurrentCheckedPosition()) {
                            NoteItem i = (NoteItem) adapter.getItem(a);
                            audioUris.add(Uri.parse(i.getLink()));
                        }
                        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, audioUris);
                        shareIntent.setType("video/*");
                        startActivity(Intent.createChooser(shareIntent, "Share via"));
                        break;
                    }

                }
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // TODO Auto-generated method stub
                if (checked) {
                    nr++;
                    adapter.setNewSelection(position, checked);
                } else {
                    nr--;
                    adapter.removeSelection(position);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mode.setTitle(nr + " selected");
                }

            }
        });}
        recyclerView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                if(!adapter.isPositionChecked(position))
                 recyclerView.setSelection(position);
                return false;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext(),
                NoteContentProvider.CONTENT_URI, null, null, null, null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ITEMS.clear();
        while (data.moveToNext()) {
            NoteItem newItem = new NoteItem(data);
            if (newItem.getType().trim().contains("video"))
                ITEMS.add(newItem);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NoteItem item = (NoteItem) parent.getAdapter().getItem(position);
        Intent intent;
        if (Build.VERSION.SDK_INT < 14) {
            intent = new Intent(getContext(), TextActivity.class);
            intent.putExtra("uri", "file://" + item.getLink());
            intent.putExtra("name", item.getName());
            startActivity(intent);
        } else {
            intent = new Intent(getContext(), VideoPlayer.class);
            intent.setData(Uri.parse(item.getLink()));
            startActivity(intent);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(NoteItem item);
    }
}
