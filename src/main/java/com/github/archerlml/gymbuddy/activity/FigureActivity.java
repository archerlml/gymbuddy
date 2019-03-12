package com.github.archerlml.gymbuddy.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.model.Figure;
import com.github.archerlml.gymbuddy.model.Record;
import com.github.archerlml.gymbuddy.model.UserPreference;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.TimeUtil;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class FigureActivity extends BaseActivity implements MyEventBus.Callback {

    @BindView(R.id.chart)
    LineChartView mChart;

    @BindView(R.id.date)
    EditText mDate;

    @BindView(R.id.value)
    EditText mValue;

    @BindView(R.id.type)
    Spinner mType;

    @BindView(R.id.add_button)
    Button button;

    Long mSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i();
        mChart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                List<Record> data = getCharData();
                Record record = data.get(pointIndex);
                mSelectedDate = record.time;
                mDate.setText(record.toMMDD());
                mValue.setText(String.valueOf(record.value));
                updateButton();
            }

            @Override
            public void onValueDeselected() {

            }
        });

        button.setOnClickListener(v -> {
            hideKeyboard();
            if (mSelectedDate == null) {
                return;
            }

            Figure.Type type = getSelectedType();
            Figure figure = getUserData().getOrNewMy(Figure.class);
            LongSparseArray<Record> data = figure.figureAsSparseArray(type, new LongSparseArray<>());

            switch (ButtonText.fromString(button.getText().toString())) {
                case Add:
                case Update:
                    data.put(mSelectedDate, new Record(mSelectedDate, Float.valueOf(mValue.getText().toString())));
                    break;
                case Delete:
                    data.remove(mSelectedDate);
                    break;
            }

            figure.setFigure(type, data);
            getUserData().putMy(figure, figureOptional -> {
                ViewUtil.toast("update data ", figureOptional.isPresent() ? "successfully" : "failed");
                if (!figureOptional.isPresent()) {
                    return;
                }
                refreshUI();
            });
        });

        mDate.setOnClickListener(v -> {
            Calendar myCalendar = TimeUtil.getCalendar(Util.or(mSelectedDate, System.currentTimeMillis()));
            new DatePickerDialog(this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        myCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        myCalendar.set(Calendar.MINUTE, 0);
                        myCalendar.set(Calendar.SECOND, 0);
                        myCalendar.set(Calendar.MILLISECOND, 0);
                        mSelectedDate = myCalendar.getTimeInMillis();
                        mDate.setText(TimeUtil.formatTime(mSelectedDate, "MM/dd"));
                        hideKeyboard();
                        updateButton();
                    },
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
        mValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateButton();
            }
        });

        List<String> types = Util.map(getTypesToDisplay(), Figure.Type::toString);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.figure_type, types);
        mType.setAdapter(dataAdapter);
        mType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mType.setSelection(0);
    }

    private void updateButton() {
        Record record = getRecordByInputDate();
        if (record == null) {
            setAddButton();
        } else if (String.valueOf(record.value).equals(mValue.getText().toString())) {
            setDeleteButton();
        } else {
            setUpdateButton();
        }
        updateButtonEnableState();
        switch (getSelectedType()) {
            case Steps:
            case Distance:
                mDate.setEnabled(false);
                mValue.setEnabled(false);
                button.setVisibility(View.GONE);
                break;
            default:
                mDate.setEnabled(true);
                mValue.setEnabled(true);
                button.setVisibility(View.VISIBLE);
        }
    }

    private enum ButtonText {
        Delete,
        Add,
        Update;

        static ButtonText fromString(String s) {
            for (ButtonText text : ButtonText.values()) {
                if (text.toString().toLowerCase().equals(s.toLowerCase())) {
                    return text;
                }
            }
            return Add;
        }
    }

    private void setDeleteButton() {
        button.setText(ButtonText.Delete.toString());
        button.setBackgroundResource(R.drawable.red_button_selector);
    }

    private void setAddButton() {
        button.setText(ButtonText.Add.toString());
        button.setBackgroundResource(R.drawable.green_button_selector);
    }

    private void setUpdateButton() {
        button.setText(ButtonText.Update.toString());
        button.setBackgroundResource(R.drawable.green_button_selector);
    }

    private void updateButtonEnableState() {
        if (Util.isEmpty(mDate.getText().toString()) ||
                Util.isEmpty(mValue.getText().toString())) {
            button.setEnabled(false);
        } else {
            button.setEnabled(true);
        }
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

    private Record getRecordByInputDate() {
        if (mSelectedDate == null) {
            return null;
        }
        Figure.Type type = getSelectedType();
        Figure figure = getUserData().getOrNewMy(Figure.class);
        LongSparseArray<Record> recordLongSparseArray = figure.figureAsSparseArray(type);
        return recordLongSparseArray == null ? null : recordLongSparseArray.get(mSelectedDate);
    }

    private List<Record> getCharData() {
        Figure.Type type = getSelectedType();
        Figure figure = getUserData().getOrNewMy(Figure.class);
        return figure.figureAsList(type);
    }

    private Figure.Type getSelectedType() {
        String typeString = mType.getSelectedItem().toString();
        return Figure.Type.forValue(typeString);
    }

    private void showChart(Figure.Type type) {
        Figure figure = getUserData().getOrNewMy(Figure.class);
        List<Record> records = figure.figureAsList(type);

        List<PointValue> values = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        if (records != null) {
            for (int j = 0; j < records.size(); ++j) {
                Record record = records.get(j);
                values.add(new PointValue(j, record.value));
                AxisValue axisValue = new AxisValue(j);
                axisValue.setLabel(record.toMMDD());
                axisValues.add(axisValue);
            }
        }

        Line line = new Line(values);
        line.setColor(ChartUtils.COLORS[type.ordinal() % ChartUtils.COLORS.length]);
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);

        LineChartData data = new LineChartData(Collections.singletonList(line));

        Axis axisX = new Axis(axisValues);
        Axis axisY = new Axis().setHasLines(true);
        axisY.setName(type.toString());
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mChart.setLineChartData(data);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_figure;
    }

    @Override
    public boolean acceptEvent(GBEvent event) {
        return event.what == GBEvent.Type.REFRESH_SCHEDULE;
    }

    @Override
    public void onAcceptedEvent(GBEvent GBEvent) {
        refreshUI();
    }

    @Override
    public void refreshUI() {
        super.refreshUI();
        Figure.Type type = getSelectedType();
        showChart(type);
        mSelectedDate = null;
        mDate.setText(null);
        mValue.setText(null);
        updateButton();
    }
}
