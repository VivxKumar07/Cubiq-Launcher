package com.movtery.zalithlauncher.ui.subassembly.customcontrols;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.databinding.ItemControlListViewBinding;
import com.movtery.zalithlauncher.databinding.ItemFileListViewBinding;
import com.movtery.zalithlauncher.ui.dialog.ControlInfoDialog;
import com.movtery.zalithlauncher.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.Tools;

import java.util.ArrayList;
import java.util.List;

public class ControlListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_VALID = 0;
    private static final int VIEW_TYPE_INVALID = 1;
    private final List<ControlItemBean> mData = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_VALID) {
            return new ValidViewHolder(ItemControlListViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
        } else {
            return new InvalidViewHolder(ItemFileListViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ControlItemBean controlItemBean = this.mData.get(position);
        if (getItemViewType(position) == VIEW_TYPE_VALID) {
            ((ValidViewHolder) holder).setData(controlItemBean);
            holder.itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(controlItemBean.controlInfoData.fileName);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onLongClick(controlItemBean.controlInfoData.fileName);
                    return true;
                }
                return false;
            });
        } else {
            ((InvalidViewHolder) holder).setData(controlItemBean);
            holder.itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onInvalidItemClick(controlItemBean.controlInfoData.fileName);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (this.mData.get(position).isInvalid) ? VIEW_TYPE_INVALID : VIEW_TYPE_VALID;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(List<ControlItemBean> items) {
        this.mData.clear();
        this.mData.addAll(items);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String name);
        void onLongClick(String name);
        void onInvalidItemClick(String name);
    }

    public static class InvalidViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final ItemFileListViewBinding binding;

        public InvalidViewHolder(@NonNull ItemFileListViewBinding binding) {
            super(binding.getRoot());
            context = binding.getRoot().getContext();
            this.binding = binding;
            binding.check.setVisibility(View.GONE);
            binding.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_disabled));
        }

        public void setData(ControlItemBean controlItemBean) {
            String text = StringUtils.insertSpace(context.getString(R.string.controls_info_invalid), controlItemBean.controlInfoData.fileName);
            binding.name.setText(text);
            binding.name.setTextColor(Color.rgb(255, 60, 60));
            binding.name.setTypeface(null, Typeface.BOLD);
            binding.name.setTextSize(14);
        }
    }

    public class ValidViewHolder extends RecyclerView.ViewHolder {
        private final Context mContext;
        private final ItemControlListViewBinding binding;

        public ValidViewHolder(@NonNull ItemControlListViewBinding binding) {
            super(binding.getRoot());
            mContext = binding.getRoot().getContext();
            this.binding = binding;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setData(ControlItemBean controlItemBean) {
            ControlInfoData controlInfoData = controlItemBean.controlInfoData;

            binding.infoButton.setOnClickListener(v -> {
                ControlInfoDialog controlInfoDialog = new ControlInfoDialog(
                        mContext,
                        controlInfoData,
                        this::notifyDataSetChangedSafe
                );
                controlInfoDialog.show();
            });
            binding.infoLayout.removeAllViews();

            List<TextView> infoViews = new ArrayList<>();
            if (!controlInfoData.name.isEmpty() && !controlInfoData.name.equals("null")) {
                if (controlInfoData.name.equals("control.default.title.text")) {
                    controlInfoData.name = mContext.getString(R.string.controls_info_default_title);
                }
                binding.title.setText(controlInfoData.name);
                infoViews.add(getAInfoTextView(R.string.controls_info_file_name, controlInfoData.fileName));
            } else {
                binding.title.setText(controlInfoData.fileName);
            }

            int color = controlItemBean.isHighlighted ?
                    Color.rgb(69, 179, 162) :
                    binding.title.getResources().getColor(R.color.primary_text, binding.title.getContext().getTheme());
            binding.title.setTextColor(color);

            if (!controlInfoData.author.isEmpty() && !controlInfoData.author.equals("null")) {
                infoViews.add(getAInfoTextView(R.string.controls_info_author, controlInfoData.author));
            }
            if (!controlInfoData.version.isEmpty() && !controlInfoData.version.equals("null")) {
                infoViews.add(getAInfoTextView(R.string.controls_info_version, controlInfoData.version));
            }

            if (!controlInfoData.desc.isEmpty() && !controlInfoData.desc.equals("null")) {
                if (controlInfoData.desc.equals("control.default.desc.text")) {
                    controlInfoData.desc = mContext.getString(R.string.controls_info_default_desc);
                }
                binding.desc.setText(controlInfoData.desc);
            } else {
                binding.desc.setText(R.string.controls_info_no_info);
            }

            for (TextView infoView : infoViews) {
                binding.infoLayout.addView(infoView);
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private void notifyDataSetChangedSafe() {
            notifyDataSetChanged();
        }

        private TextView getAInfoTextView(int string, String value) {
            TextView textView = new TextView(mContext);
            textView.setText(StringUtils.insertSpace(mContext.getString(string), value));
            FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, (int) Tools.dpToPx(8), 0);
            textView.setLayoutParams(layoutParams);
            return textView;
        }
    }
}

