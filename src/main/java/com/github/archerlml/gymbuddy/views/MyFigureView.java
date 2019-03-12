package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.adapter.BaseListAdapter;
import com.github.archerlml.gymbuddy.model.Figure;
import com.github.archerlml.gymbuddy.model.Record;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;

/**
 * Created by archerlml on 10/28/17.
 */

public class MyFigureView extends TypedBaseView<Figure> {

    @BindView(R.id.latest_figure)
    public RecyclerView mRecyclerView;

    List<Entry> mEntries = new ArrayList<>();

    public MyFigureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(AttributeSet attrs) {
        super.init(attrs);

        mRecyclerView.setAdapter(new BaseListAdapter<Entry>() {

            @Override
            public void applyData(Entry entry, BaseViewHolder holder, int position) {
                holder.getView(R.id.name, TextView.class).setText(entry.name);
                holder.getView(R.id.value, TextView.class).setText(String.valueOf(entry.value));
                holder.root.setOnClickListener(l);
            }

            @Override
            public int itemLayout() {
                return R.layout.item_latest_figure;
            }

            @Override
            public List<Entry> getItems() {
                return mEntries;
            }
        });
    }

    OnClickListener l;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.l = l;
    }

    private List<Figure.Type> getTypesToDisplay() {
        UserPreference userPreference = getUserData().getOrNewMy(UserPreference.class);
        if (userPreference == null) {
            return Arrays.asList(Figure.Type.values());
        }

        List<String> dataTypesToDisplay = Util.map(Arrays.asList(Figure.Type.values()), Figure.Type::toString);
        List<Figure.Type> types = new ArrayList<>();
        for (String type : dataTypesToDisplay) {
            types.add(Figure.Type.forValue(type.toLowerCase()));
        }
        Collections.sort(types);
        return types;
    }

    @Override
    public MyFigureView applyData(Figure figure) {
        super.applyData(figure);
        if (figure != null) {
            List<Figure.Type> types = getTypesToDisplay();
            mEntries = Stream.of(types)
                    .filter(figure::contains)
                    .filter(type -> figure.figureAsSparseArray(type).size() != 0)
                    .map(type -> {
                        List<Record> records = figure.figureAsList(type);
                        Record record = records.get(records.size() - 1);
                        return new Entry(type.toString(), record.value);
                    })
                    .collect(Collectors.toList());
        }
        mRecyclerView.getAdapter().notifyDataSetChanged();
        return this;
    }

    @Override
    public int layoutId() {
        return R.layout.figure_summary;
    }

    private static class Entry {
        public String name;
        public float value;

        public Entry(String name, float value) {
            this.name = name;
            this.value = value;
        }
    }
}
