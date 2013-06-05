package cmu.costco.simplifiedcheckout.qr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.example.simplifiedcheckout.qr.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	/**
	 * Scan barcode with ZXing Intent
	 */
	public void scanNowZXing(View view) {
		Intent scanIntent = new Intent(this, ZXingScanActivity.class);
		startActivity(scanIntent);
	}
	
	/**
	 * Scan barcode with Scandit Intent
	 */
	public void scanNowScandit(View view) {
		Intent scanIntent = new Intent(this, ScanditScanActivity.class);
		startActivity(scanIntent);
	}

}
