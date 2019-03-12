package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.Figure;
import com.github.archerlml.gymbuddy.model.Rule;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.Util;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * Created by archerlml on 10/28/17.
 */

public class RuleView extends TypedBaseView<Rule> {

    @BindView(R.id.save_layout)
    public View mSaveLayout;

    @BindView(R.id.type)
    public Spinner mType;

    @BindView(R.id.aggregation)
    public Spinner mAggregation;

    @BindView(R.id.min_value)
    public EditText mMinValue;

    @BindView(R.id.max_value)
    public EditText mMaxValue;

    public RuleView(@NonNull Context context) {
        super(context);
    }

    public RuleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(AttributeSet attrs) {
        super.init(attrs);

        List<String> types = Util.map(Arrays.asList(Figure.Type.values()), Figure.Type::toString);
        mType.setAdapter(new ArrayAdapter<>(getContext(), R.layout.figure_type, types));

        List<String> aggregations = Util.map(Arrays.asList(Rule.Aggregation.values()), Rule.Aggregation::toString);
        mAggregation.setAdapter(new ArrayAdapter<>(getContext(), R.layout.figure_type, aggregations));

        applyData(new Rule());
    }

    @Override
    public int layoutId() {
        return R.layout.rule_view;
    }

    @Override
    public TypedBaseView applyData(Rule rule) {
        super.applyData(rule);
        return this;
    }

    @Override
    public Rule getData() {
        Rule rule = super.getData();
        rule.type = Figure.Type.forValue(mType.getSelectedItem().toString());
        rule.aggregation = Rule.Aggregation.forValue(mAggregation.getSelectedItem().toString());
        rule.min = Util.parseFloat(mMinValue.getText().toString());
        rule.max = Util.parseFloat(mMaxValue.getText().toString());
        return rule;
    }

    public boolean isValid() {
        Float min = null, max = null;
        try {
            min = Float.parseFloat(mMinValue.getText().toString());
        } catch (NumberFormatException e) {
            Log.e(e);
        }
        try {
            max = Float.parseFloat(mMaxValue.getText().toString());
        } catch (NumberFormatException e) {
            Log.e(e);
        }
        return min != null || max != null;
    }
}
