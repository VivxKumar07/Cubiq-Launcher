package com.movtery.zalithlauncher.ui.subassembly.customcontrols;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.game.control.ControlManager;
import com.movtery.zalithlauncher.path.PathManager;
import com.movtery.zalithlauncher.ui.dialog.DeleteDialog;
import com.movtery.zalithlauncher.ui.subassembly.filelist.RefreshListener;
import com.movtery.zalithlauncher.utils.stringutils.StringFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlsListViewCreator {
    private final Context context;
    private final RecyclerView mainListView;
    private final AtomicInteger searchCount = new AtomicInteger(0);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ControlListAdapter controlListAdapter;
    private ControlSelectedListener selectedListener;
    private RefreshListener refreshListener;
    private File fullPath = PathManager.DIR_CONTROL_LAYOUTS;
    private String filterString = "";
    private boolean showSearchResultsOnly = false;
    private boolean caseSensitive = false;
    private TextView searchCountText;

    public ControlsListViewCreator(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.mainListView = recyclerView;
        init();
    }

    public void init() {
        controlListAdapter = new ControlListAdapter();
        controlListAdapter.setOnItemClickListener(new ControlListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String name) {
                File file = new File(fullPath, name);
                if (selectedListener != null) selectedListener.onItemSelected(file);
            }

            @Override
            public void onLongClick(String name) {
                File file = new File(fullPath, name);
                if (selectedListener != null) selectedListener.onItemLongClick(file);
            }

            @Override
            public void onInvalidItemClick(String name) {
                File file = new File(fullPath, name);
                new DeleteDialog(context, ControlsListViewCreator.this::refresh, Collections.singletonList(file)).show();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        mainListView.setLayoutManager(layoutManager);
        mainListView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.fade_downwards)));
        mainListView.setAdapter(controlListAdapter);
    }

    public void setSelectedListener(ControlSelectedListener listener) {
        this.selectedListener = listener;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    public void setShowSearchResultsOnly(boolean showSearchResultsOnly) {
        this.showSearchResultsOnly = showSearchResultsOnly;
    }

    public int getItemCount() {
        return controlListAdapter.getItemCount();
    }

    private List<ControlItemBean> loadInfoData(File path) {
        List<ControlItemBean> data = new ArrayList<>();
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    if (file.getName().contains("mojo")) {
                        continue;
                    }
                    ControlInfoData controlInfoData = EditControlData.loadFormFile(context, file);
                    ControlItemBean controlItemBean;
                    if (controlInfoData == null) {
                        controlInfoData = new ControlInfoData();
                        controlInfoData.fileName = file.getName();
                        if (file.getName().contains("zalith_2")) {
                            controlInfoData.name = "Zalith Launcher 2 Controls";
                            controlInfoData.author = "MovTery";
                        } else if (file.getName().contains("pocket")) {
                            controlInfoData.name = "Pocket Edition Controls";
                            controlInfoData.author = "Bedrock";
                        } else {
                            controlInfoData.name = file.getName().replace(".json", "").replace("_", " ");
                            controlInfoData.author = "Custom";
                        }
                        controlInfoData.version = "1.0";
                        controlInfoData.desc = "Modern responsive control layout";
                        controlItemBean = new ControlItemBean(controlInfoData);
                        controlItemBean.isInvalid = false;
                    } else {
                        controlItemBean = new ControlItemBean(controlInfoData);
                    }
                    if (shouldHighlight(controlInfoData, file)) {
                        controlItemBean.isHighlighted = true;
                        searchCount.addAndGet(1);
                    } else if (showSearchResultsOnly) {
                        continue;
                    }
                    data.add(controlItemBean);
                }
            }
        }
        return data;
    }

    private boolean shouldHighlight(ControlInfoData controlInfoData, File file) {
        if (filterString == null || filterString.isEmpty()) return false;
        String name = controlInfoData.name;
        String searchString = !name.isEmpty() && !name.equals("null") ? name : file.getName();
        return StringFilter.containsSubstring(searchString, filterString, caseSensitive) ||
                StringFilter.containsSubstring(file.getName(), filterString, caseSensitive);
    }

    public void listAtPath() {
        this.fullPath = controlPath();
        refresh();
    }

    public File getFullPath() {
        return this.fullPath;
    }

    public void searchControls(TextView searchCountText, String filterString, boolean caseSensitive) {
        searchCount.set(0);
        this.filterString = filterString;
        this.caseSensitive = caseSensitive;
        this.searchCountText = searchCountText;
        refresh();
    }

    private File controlPath() {
        File ctrlPath = PathManager.DIR_CONTROL_LAYOUTS;
        if (!ctrlPath.exists()) ctrlPath.mkdirs();
        return ctrlPath;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        executor.execute(() -> {
            List<ControlItemBean> itemBeans = loadInfoData(fullPath);
            filterString = "";
            mainHandler.post(() -> {
                controlListAdapter.updateItems(itemBeans);
                mainListView.scheduleLayoutAnimation();
                if (searchCountText != null) {
                    int count = searchCount.get();
                    searchCountText.setText(searchCountText.getContext().getString(R.string.search_count, count));
                    if (count != 0) searchCountText.setVisibility(View.VISIBLE);
                }
                if (refreshListener != null) refreshListener.onRefresh();
            });
        });
    }
}

