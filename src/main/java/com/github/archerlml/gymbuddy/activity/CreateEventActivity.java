package com.github.archerlml.gymbuddy.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.adapter.BaseListAdapter;
import com.github.archerlml.gymbuddy.model.Event;
import com.github.archerlml.gymbuddy.model.Rule;
import com.github.archerlml.gymbuddy.util.GBEvent;
import com.github.archerlml.gymbuddy.util.Log;
import com.github.archerlml.gymbuddy.util.MyEventBus;
import com.github.archerlml.gymbuddy.util.TimeUtil;
import com.github.archerlml.gymbuddy.util.Util;
import com.github.archerlml.gymbuddy.util.ViewUtil;
import com.github.archerlml.gymbuddy.views.RuleView;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;

public class CreateEventActivity extends BaseActivity implements MyEventBus.Callback {

    @BindView(R.id.backdrop)
    ImageView backdrop;

    String imageUrl;

    @BindView(R.id.title_line)
    TextView title;

    @BindView(R.id.rules)
    RecyclerView mRules;

    @BindView(R.id.description)
    EditText description;

    @BindView(R.id.start_time)
    EditText mStartTime;

    @BindView(R.id.end_time)
    EditText mEndTime;

    @BindView(R.id.button)
    Button button;

    Long startTimeMS;
    Long endTimeMS;

    List<Rule> rules = new ArrayList<>();
    final Rule ADD_RULE = new Rule();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStartTime.setOnClickListener(v ->
                new TimePickerDialog.Builder()
                        .setCallBack((timePickerView, millseconds) -> {
                            startTimeMS = millseconds;
                            mStartTime.setText(TimeUtil.formatTime(startTimeMS, "MMM/dd/yyyy HH:mm"));
                            updateButton();
                        })
                        .setCancelStringId("Cancel")
                        .setSureStringId("OK")
                        .setTitleStringId("Start time")
                        .setYearText("")
                        .setMonthText("")
                        .setDayText("")
                        .setHourText("")
                        .setMinuteText("")
                        .setCyclic(false)
                        .setCurrentMillseconds(Util.or(startTimeMS, System.currentTimeMillis()))
                        .setThemeColor(getResources().getColor(R.color.timepicker_dialog_bg))
                        .setType(Type.ALL)
                        .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                        .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                        .setWheelItemTextSize(12)
                        .build()
                        .show(getSupportFragmentManager(), "start"));

        mEndTime.setOnClickListener(v ->
                new TimePickerDialog.Builder()
                        .setCallBack((timePickerView, millseconds) -> {
                            endTimeMS = millseconds;
                            mEndTime.setText(TimeUtil.formatTime(endTimeMS, "MMM/dd/yyyy HH:mm"));
                            updateButton();
                        })
                        .setCancelStringId("Cancel")
                        .setSureStringId("OK")
                        .setTitleStringId("End time")
                        .setYearText("")
                        .setMonthText("")
                        .setDayText("")
                        .setHourText("")
                        .setMinuteText("")
                        .setCyclic(false)
                        .setCurrentMillseconds(Util.or(endTimeMS, System.currentTimeMillis()))
                        .setThemeColor(getResources().getColor(R.color.timepicker_dialog_bg))
                        .setType(Type.ALL)
                        .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                        .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                        .setWheelItemTextSize(12)
                        .build()
                        .show(getSupportFragmentManager(), "start"));

        title.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setText(title.getText());
            new AlertDialog.Builder(this)
                    .setTitle("Title")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) -> {
                        title.setText(input.getText());
                        updateButton();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                    .show();
        });

        backdrop.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, returnedIntent -> {
                Uri image = returnedIntent.getData();
                if (image == null) {
                    Log.d("image is null");
                    return;
                }
                showProgressDialog();
                mStorageReference
                        .child("images/" + UUID.randomUUID() + ".jpg")
                        .putFile(image)
                        .addOnSuccessListener(taskSnapshot -> {
                            hideProgressDialog();
                            imageUrl = taskSnapshot.getDownloadUrl().toString();
                            Glide.with(this)
                                    .load(image)
                                    .into(backdrop);
                        })
                        .addOnFailureListener(e -> hideProgressDialog());
            });
        });

        rules.add(ADD_RULE);
        mRules.setAdapter(new BaseListAdapter<Rule>() {

            @Override
            public void applyData(Rule rule, BaseViewHolder holder, int position) {
                TextView textView = holder.getView(R.id.text);
                ImageView imageView = holder.getView(R.id.icon);

                if (rule == ADD_RULE) {
                    textView.setText("Add a rule");
                    imageView.setOnClickListener(v -> {

                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                        RuleView ruleView = new RuleView(getContext());
                        ruleView.mSaveLayout.setOnClickListener(v1 -> {
                            if (!ruleView.isValid()) {
                                ViewUtil.toast("Min value or Max value is required");
                                return;
                            }
                            bottomSheetDialog.dismiss();
                            rules.add(ruleView.getData());
                            notifyDataSetChanged();
                            updateButton();
                        });
                        bottomSheetDialog.setContentView(ruleView);
                        bottomSheetDialog.show();

                    });
                    imageView.setImageResource(R.drawable.ic_add_black_48px);
                    return;
                }

                textView.setText(rule.toString());
                imageView.setImageResource(R.drawable.ic_highlight_off_black_48px);
                imageView.setOnClickListener(v -> {
                    rules.remove(rule);
                    updateButton();
                    notifyDataSetChanged();
                });
            }

            @Override
            public int itemLayout() {
                return R.layout.item_rule;
            }

            @Override
            public List<Rule> getItems() {
                return rules;
            }

        });

        button.setOnClickListener(v -> {
            Event event = new Event();
            event.title = title.getText().toString();
            event.imageUrl = imageUrl;
            event.startTime = startTimeMS;
            event.endTime = endTimeMS;
            event.rules = Stream.of(rules).filter(r -> r.type != null).collect(Collectors.toList());
            event.description = description.getText().toString();
            enterSubActivity(EventActivity.class, event);
        });

        description.addTextChangedListener(new TextWatcher() {
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
        Log.i();
        refreshUI();
    }

    private boolean isValid() {
        String titleString = title.getText().toString();
        return !Util.isEmpty(titleString)
                && startTimeMS != null
                && endTimeMS != null
                && !rules.isEmpty()
                && !Util.isEmpty(description.getText().toString());

    }

    private void updateButton() {
        if (isValid()) {
            button.setEnabled(true);
        } else {
            button.setEnabled(false);
        }
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_create_event;
    }

    @Override
    public boolean acceptEvent(GBEvent gbEvent) {
        return gbEvent.what == GBEvent.Type.REFRESH_SCHEDULE;
    }

    @Override
    public void onAcceptedEvent(GBEvent GBEvent) {
        refreshUI();
    }

    @Override
    public void refreshUI() {
        super.refreshUI();
    }
}
