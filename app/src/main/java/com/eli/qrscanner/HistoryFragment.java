package com.eli.qrscanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.eli.qrscanner.room.ScanRecord;
import com.eli.qrscanner.room.ScanRecordDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HistoryFragment extends Fragment {
    @Inject
    ScanRecordDao dao;

    private Toolbar toolbar;
    private SearchView searchView;
    private MenuItem searchMenuItem, optionsMenuItem;
    private RecyclerView recyclerView;
    @Inject
    Executor executor = Executors.newSingleThreadExecutor();
    List<ScanRecord> data = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        searchView = view.findViewById(R.id.search_view);
        recyclerView = view.findViewById(R.id.recycler_view);

        toolbar.inflateMenu(R.menu.history_top_menu);
        searchMenuItem = toolbar.getMenu().findItem(R.id.action_search);
        optionsMenuItem = toolbar.getMenu().findItem(R.id.action_options);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_search) {
                    searchView.setVisibility(View.VISIBLE);
                    searchView.setIconified(false); // Show search view
                } else if (id == R.id.action_options) {
                    // Handle options button click
                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.setVisibility(View.GONE);
                searchMenuItem.setVisible(true);
                optionsMenuItem.setVisible(true);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO:Handle search query submission
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO:Handle search text change
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.setVisibility(View.GONE);
                return false;
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (dao != null) {
                    data = dao.getAll();
                    ScanRecordAdapter adapter = new ScanRecordAdapter();
                    // Update the UI on the main thread
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setAdapter(adapter);
                            adapter.setScanRecords(data);
                        }
                    });
                }
            }
        });
        return view;
    }
}

