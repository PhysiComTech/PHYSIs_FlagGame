package com.physicomtech.kit.physis_flaggame.customize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.physicomtech.kit.physis_flaggame.R;

public class NumberPickView extends LinearLayout {

    public interface OnNumberChangeListener {
        void onMinus(int pickerTag, int currentValue);
        void onPlus(int pickerTag, int currentValue);
    }

    private OnNumberChangeListener onNumberChangeListener = null;

    public void setOnNumberChanged(OnNumberChangeListener listener){
        this.onNumberChangeListener = listener;
    }

    TextView tvPickerTitle;
    ImageButton btnMinus, btnPlus;
    EditText etNumber;

    private String pickerTag = null;
    private String pickerTitle = null;
    private String pickNumber = "0";

    public NumberPickView(Context context) {
        super(context);
    }

    @SuppressLint({"Recycle", "CustomViewStyleable"})
    public NumberPickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NumberPickAttrs);
        pickerTitle = (String) typedArray.getText(R.styleable.NumberPickAttrs_setTitle);
        pickNumber = (String) typedArray.getText(R.styleable.NumberPickAttrs_setNumber);
        pickerTag = (String) typedArray.getText(R.styleable.NumberPickAttrs_setPickTag);

        initView();
    }

    private void initView() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        final View view = layoutInflater.inflate(R.layout.view_number_picker, this, false);
        addView(view);

        tvPickerTitle = view.findViewById(R.id.tv_number_picker);
        btnMinus = view.findViewById(R.id.btn_minus);
        btnPlus = view.findViewById(R.id.btn_plus);
        etNumber = view.findViewById(R.id.et_number);
        etNumber.setText(pickNumber);

        setPickerTitle(pickerTitle);
        setPickerTag(pickerTag);

        btnMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onNumberChangeListener != null)
                    onNumberChangeListener.onMinus(Integer.valueOf(etNumber.getTag().toString()), Integer.valueOf(etNumber.getText().toString()));
            }
        });

        btnPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onNumberChangeListener != null)
                    onNumberChangeListener.onPlus(Integer.valueOf(etNumber.getTag().toString()), Integer.valueOf(etNumber.getText().toString()));
            }
        });

    }

    public void setPickerTag(String tag){
        if(tag != null)
            etNumber.setTag(tag);
    }

    public void setPickerTitle(String title){
        if(pickerTitle != null)
            tvPickerTitle.setText(title);
    }

    public int getNumber(){
        return Integer.valueOf(etNumber.getText().toString());
    }

    public void setNumber(int num){
        etNumber.setText(String.valueOf(num));
    }

}
