package com.movtery.zalithlauncher.ui.subassembly.filelist;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.databinding.ItemFileListViewBinding;
import com.movtery.zalithlauncher.utils.image.ImageUtilsKt;
import com.movtery.zalithlauncher.utils.stringutils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.InnerHolder> {
    private final List<FileItemBean> mData = new ArrayList<>();
    private final List<FileItemBean> selectedFiles = new ArrayList<>();
    private boolean isMultiSelectMode = false;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnMultiSelectListener mOnMultiSelectListener;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InnerHolder(ItemFileListViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
        holder.setData(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(List<FileItemBean> items) {
        this.mData.clear();
        this.mData.addAll(items);
        notifyDataSetChanged();
    }

    public List<FileItemBean> getData() {
        return mData;
    }

    public boolean isNoFile() {
        return (mData.size() == 1 && !mData.get(0).isCanCheck) || mData.isEmpty();
    }

    private void toggleSelection(FileItemBean itemBean, CheckBox checkBox) {
        if (itemBean.isCanCheck) {
            if (selectedFiles.contains(itemBean)) {
                selectedFiles.remove(itemBean);
                checkBox.setChecked(false);
            } else {
                selectedFiles.add(itemBean);
                checkBox.setChecked(true);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMultiSelectMode(boolean multiSelectMode) {
        isMultiSelectMode = multiSelectMode;
        if (!multiSelectMode) selectedFiles.clear();
        notifyDataSetChanged();
    }

    public List<FileItemBean> getSelectedFiles() {
        return selectedFiles;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnMultiSelectListener(OnMultiSelectListener listener) {
        this.mOnMultiSelectListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, FileItemBean itemBean);
    }

    public interface OnMultiSelectListener {
        void onMultiSelect(List<FileItemBean> itemBeans);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, FileItemBean itemBean);
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private final ItemFileListViewBinding binding;
        private int mPosition;
        private FileItemBean mFileItemBean;

        public InnerHolder(@NonNull ItemFileListViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.check.setOnClickListener(v -> {
                if (isMultiSelectMode) toggleSelection(mFileItemBean, binding.check);
            });
            itemView.setOnClickListener(v -> {
                if (isMultiSelectMode) {
                    toggleSelection(mFileItemBean, binding.check);
                } else if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mPosition, mFileItemBean);
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (isMultiSelectMode) {
                    if (mOnMultiSelectListener != null) mOnMultiSelectListener.onMultiSelect(getSelectedFiles());
                } else if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(mPosition, mFileItemBean);
                }
                return true;
            });
        }

        public void setData(FileItemBean fileItemBean, int position) {
            mPosition = position;
            mFileItemBean = fileItemBean;
            File file = fileItemBean.file;

            binding.name.setText(fileItemBean.name);

            int infoLayoutVisible = View.GONE;
            if (fileItemBean.date != null) {
                String date = StringUtils.formatDate(fileItemBean.date, Locale.getDefault(), TimeZone.getDefault());
                binding.time.setText(date);
                binding.time.setVisibility(View.VISIBLE);
                infoLayoutVisible = View.VISIBLE;
            } else binding.time.setVisibility(View.GONE);

            if (fileItemBean.size != null) {
                String size = Formatter.formatShortFileSize(binding.getRoot().getContext(), fileItemBean.size);
                binding.size.setText(size);
                binding.size.setVisibility(View.VISIBLE);
                infoLayoutVisible = View.VISIBLE;
            } else binding.size.setVisibility(View.GONE);

            binding.infoLayout.setVisibility(infoLayoutVisible);

            if (fileItemBean.isHighlighted) {
                binding.name.setTextColor(Color.rgb(69, 179, 162));
            } else {
                binding.name.setTextColor(binding.name.getResources().getColor(R.color.black_or_white, binding.name.getContext().getTheme()));
            }

            if (fileItemBean.isCanCheck) {
                binding.check.setVisibility(isMultiSelectMode ? View.VISIBLE : View.GONE);
                binding.check.setChecked(selectedFiles.contains(fileItemBean));
            } else {
                binding.check.setVisibility(View.GONE);
            }

            if (file != null && file.isFile() && ImageUtilsKt.isImageFile(file)) {
                Drawable custom = Drawable.createFromPath(file.getAbsolutePath());
                binding.image.setImageDrawable(custom != null ? custom : fileItemBean.image);
            } else {
                binding.image.setImageDrawable(fileItemBean.image);
            }
        }
    }
}

