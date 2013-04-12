package com.example.tictactoe;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;

public class MenuScreenActivity extends Activity implements Constants {
	
	private final static int MIN_DIVISIONS = 3;
	private final static int MAX_DIVISIONS = 10;
	
	private Button singlePlayer;
	private Button twoPlayerLocal;
	private Button twoPlayerNetwork;
		
	private NumberPicker numRowsPicker;
	private NumberPicker numColumnsPicker;
	private NumberPicker numNeededToWinPicker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.activity_menu_screen);
		final Intent buttonIntent = new Intent(MenuScreenActivity.this, TTTMainScreenActivity.class);
		final Intent networkButtonIntent = new Intent(MenuScreenActivity.this, NetworkActivity.class);
		setupMenuScreen();
		setButtonClickOn(singlePlayer, Constants.SINGLE_PLAYER, buttonIntent);
		setButtonClickOn(twoPlayerLocal, Constants.TWO_PLAYER_LOCAL, buttonIntent);
		setButtonClickOn(twoPlayerNetwork, Constants.TWO_PLAYER_NETWORK, networkButtonIntent);
	}
	
	 private void setButtonClickOn(Button buttClick, final String buttName, final Intent buttIntent){
	    	buttClick.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					buttIntent.putExtra(Constants.BUTTON_KEY, buttName);
					buttIntent.putExtra(Constants.ROW_KEY, Integer.toString(numRowsPicker.getValue()));
					buttIntent.putExtra(Constants.COLUMN_KEY, Integer.toString(numColumnsPicker.getValue()));
					buttIntent.putExtra(Constants.WIN_KEY, Integer.toString(numNeededToWinPicker.getValue()));
					MenuScreenActivity.this.startActivity(buttIntent);
				}
			});
	    }
	 
	 private void setupMenuScreen(){
		numRowsPicker = (NumberPicker) findViewById(R.id.numberPickerRows);
		numColumnsPicker = (NumberPicker) findViewById(R.id.numberPickerColumns);
		numNeededToWinPicker = (NumberPicker) findViewById(R.id.numberPickerNeedXToWin);
		setNumberPicker(numRowsPicker);
		setNumberPicker(numColumnsPicker);
		setNumberPicker(numNeededToWinPicker);
		singlePlayer = (Button) findViewById(R.id.buttonSinglePlayer);
		twoPlayerLocal = (Button) findViewById(R.id.buttonTwoPlayerLocal);
		twoPlayerNetwork = (Button) findViewById(R.id.buttonTwoPlayerNetwork);
	 }
	 
	 private void setNumberPicker(NumberPicker numPick){
		 numPick.setMinValue(MIN_DIVISIONS);
		 numPick.setMaxValue(MAX_DIVISIONS);
		 numPick.setWrapSelectorWheel(false);
	 }



	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_screen, menu);
		return true;
	}*/

}
