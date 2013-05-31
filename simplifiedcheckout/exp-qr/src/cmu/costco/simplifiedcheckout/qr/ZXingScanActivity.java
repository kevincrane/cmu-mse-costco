package cmu.costco.simplifiedcheckout.qr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simplifiedcheckout.qr.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ZXingScanActivity extends Activity implements OnClickListener {
	private Button scanButton;
	private TextView resultTextView;
	
	/**
	 * Initializes controls, values...
	 */
	private void init() {
		scanButton = (Button) findViewById(R.id.main_button_scan);
		scanButton.setOnClickListener(this);
		resultTextView = (TextView) findViewById(R.id.main_textview_scan_result);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(scanResult != null) {
			Log.i("SCAN", "scan result: " + scanResult);
			resultTextView.setText(scanResult.toString());
		} else {
			Log.e("SCAN", "Sorry, the scan was unsuccessful...");
			Toast.makeText(this, "Sorry, the scan was unsuccessful...", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void onClick(View view) {
		int id = view.getId();
		if(id == R.id.main_button_scan) {
			// start scanning
			IntentIntegrator intentIntegrator = new IntentIntegrator(this);
			intentIntegrator.initiateScan();
		}
	}
	
	
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_zxing);
		
		init();
	}
}
