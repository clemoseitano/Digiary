package net.tanozin.digiary.note.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import net.tanozin.digiary.R;

import net.tanozin.digiary.note.NoteContentProvider;
import net.tanozin.digiary.note.NoteItem;
import net.tanozin.digiary.note.NoteItemAdapter;
import net.tanozin.digiary.note.activity.AudioActivity;
import net.tanozin.digiary.note.activity.ImageActivity;
import net.tanozin.digiary.note.activity.TextActivity;
import net.tanozin.digiary.texttray.EditorActivity;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    public List<NoteItem> ITEMS = new ArrayList<>();
    private final Context context = getContext();
    public NoteItemAdapter adapter;
    public Bundle bundle1;
    private static final String PREF_DELETE = "sync_frequency";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SearchFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SearchFragment newInstance(int columnCount) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String searchTerm = getActivity().getIntent().getStringExtra("search");
        String[] projection = new String[]{NoteContentProvider.KEY_LINK,
                NoteContentProvider.KEY_DATE_TAKEN, NoteContentProvider.KEY_NAME,
                NoteContentProvider.KEY_TYPE, NoteContentProvider.KEY_DATE_MODIFIED};
        Cursor c = getActivity().getContentResolver().query(NoteContentProvider.CONTENT_URI, projection, null, null, null);
        if (c == null)
            return;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String fullCursor = c.getString(c.getColumnIndex(NoteContentProvider.KEY_NAME)) + " " + c.getString(c.getColumnIndex(NoteContentProvider.KEY_TYPE));

            if (fullCursor.toLowerCase().contains(searchTerm.toLowerCase()))
                ITEMS.add(new NoteItem(c));
            c.moveToNext();
        }
        adapter = new NoteItemAdapter(this.getContext(), ITEMS);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setRetainInstance(false);

    }

    @Override
    public void setRetainInstance(boolean retain) {
        super.setRetainInstance(retain);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        // Set the adapter
        final AbsListView recyclerView = (AbsListView) view.findViewById(R.id.listView);
        recyclerView.setOnItemClickListener(this);
        ((AdapterView<ListAdapter>)recyclerView).setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NoteItem item = (NoteItem) parent.getAdapter().getItem(position);

        if (item.getType().trim().contains("image")) {
            Intent intent = new Intent(getContext(), ImageActivity.class);
            intent.putExtra("uri", item.getLink());
            intent.putExtra("name", item.getName());
            startActivity(intent);
        } else if (item.getType().trim().contains("text")) {
            Intent intent = new Intent(getContext(), EditorActivity.class);
            //intent.putExtra("uri", item.getLink());
            //intent.putExtra("name", item.getName());
            intent.setData(Uri.parse(item.getLink()));
            startActivity(intent);
        } else if (item.getType().trim().contains("video")) {
            Intent intent = new Intent(getContext(), TextActivity.class);
            intent.putExtra("uri", item.getLink());
            intent.putExtra("name", item.getName());
            startActivity(intent);
        } else if (item.getType().trim().contains("audio")) {
            Intent intent = new Intent(getContext(), AudioActivity.class);
            intent.putExtra("uri", item.getLink());
            intent.putExtra("name", item.getName());
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
