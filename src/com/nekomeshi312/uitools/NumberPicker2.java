package com.nekomeshi312.uitools;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class NumberPicker2 extends LinearLayout {
	public interface OnChangedListener{
		public void onChange(int newCount);
	}
	public void setOnChangedListener(OnChangedListener listener){
		mChangedListener = listener;
	}
	private OnChangedListener mChangedListener = null;
	
	private com.nekomeshi312.uitools.NumberPicker mPicker = null;
    private android.widget.NumberPicker mPickerV11 = null;
    
    private Context mContext = null;

	public NumberPicker2(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	public NumberPicker2(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("NewApi")
	public NumberPicker2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
		// TODO Auto-generated constructor stub
        mContext = context;
        
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.number_picker2, this, true);
        
        Object picker = (Object)findViewById(R.id.picker2);
    	if(picker instanceof com.nekomeshi312.uitools.NumberPicker){
    		mPicker = (com.nekomeshi312.uitools.NumberPicker)picker;
    	}
    	else{
    		mPickerV11 = (android.widget.NumberPicker)picker;
    	}
		if(null != mPicker){
			mPicker.setOnChangeListener(new com.nekomeshi312.uitools.NumberPicker.OnChangedListener(){

				@Override
				public void onChanged(NumberPicker picker, int oldVal,
						int newVal) {
					// TODO Auto-generated method stub
					if(mChangedListener != null) mChangedListener.onChange(getCurrent());
				}
			});
		}
		else{
			//標準のnumberpickerをオリジナルのnumberpickerと同じlayoutparamで配置
			mPickerV11.setOnValueChangedListener(new android.widget.NumberPicker.OnValueChangeListener() {
				@SuppressLint("NewApi")
				@Override
				public void onValueChange(android.widget.NumberPicker picker, int oldVal,
						int newVal) {
					// TODO Auto-generated method stub
					if(mChangedListener != null) mChangedListener.onChange(getCurrent());
				}
			});
		}
	}
	@SuppressLint({ "NewApi", "NewApi" })
	public void setRange(int minValue, int maxValue){
		if(null != mPicker){
	    	mPicker.setRange(minValue, maxValue);
		}
		else{
			mPickerV11.setMinValue(minValue);
			mPickerV11.setMaxValue(maxValue);
		}
	}
	@SuppressLint("NewApi")
	public void setCurrent(int value){
		if(null !=  mPicker){
	    	mPicker.setCurrent(value);
		}
		else{
	    	mPickerV11.setValue(value);
		}
		
	}
	public int getCurrent(){
		if(null !=  mPicker){
			return mPicker.getCurrent();
		}
		else{
			return mPickerV11.getValue();
		}
	}
	/* (non-Javadoc)
	 * @see android.view.View#setEnabled(boolean)
	 */
	@SuppressLint("NewApi")
	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		if(null !=  mPicker){
			mPicker.setEnabled(enabled);
		}
		else{
			mPickerV11.setEnabled(enabled);
		}
	}
}
