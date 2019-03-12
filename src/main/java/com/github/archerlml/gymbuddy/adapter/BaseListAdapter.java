package com.github.archerlml.gymbuddy.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public abstract class BaseListAdapter<Model> extends RecyclerView.Adapter<BaseListAdapter.BaseViewHolder> {

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(itemLayout(), parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        Model model = getItems().get(position);
        applyData(model, holder, position);
    }

    @Override
    public int getItemCount() {
        return getItems() == null ? 0 : getItems().size();
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        SparseArray<View> subviews;
        public View root;

        public BaseViewHolder(final View v) {
            super(v);
            subviews = new SparseArray<>();
            this.root = v;
        }

        public <T extends View> T getView(int id) {
            return (T) getView(id, View.class);
        }

        public <T extends View> T getView(int id, Class<T> cls) {
            View view = subviews.get(id);
            if (view == null) {
                view = this.root.findViewById(id);
                subviews.put(id, view);
            }
            return (T) view;
        }
    }

    public abstract void applyData(Model model, BaseViewHolder holder, int position);

    public abstract int itemLayout();

    public abstract List<Model> getItems();
}
