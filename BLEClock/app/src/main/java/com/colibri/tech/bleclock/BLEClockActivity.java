package com.colibri.tech.bleclock;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BLEClockActivity extends AppCompatActivity {

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothLeScanner mBluetoothLeScanner;
	private List<BluetoothGattService> mServiceList = null;
	private BluetoothDevice mBluetoothDevice = null;
	private ScanCallback mScanCallback;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic = null;
	private TextView ui_log;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bleclock);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ui_log = findViewById(R.id.txt_terminal);
		FloatingActionButton fab = findViewById(R.id.fab_send);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ui_log.setText("");
				if(mBluetoothDevice == null) {
					ui_log.append("Start BLE Scan ...\n");
					mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
					mBluetoothLeScanner.startScan(mScanCallback);
				}else{
					if (mBluetoothGatt == null) {
						ui_log.append("Connecting GATT ...\n");
						mBluetoothDevice.connectGatt(BLEClockActivity.this, false, mGattCallback);
					}else{
						ui_log.append("Discovering Services ...\n");
						mBluetoothGatt.discoverServices();
					}
				}
			}
		});

		mScanCallback = new LeScanCallback();
		ui_log.setText("");
		initBluetooth();
	}

	private void initBluetooth() {
		mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);//get Bluetooth service
		mBluetoothAdapter = mBluetoothManager.getAdapter();//get Bluetooth Adapter
		if (mBluetoothAdapter == null) {	//platform not support bluetooth
			ui_log.append("Bluetooth is not support\n");
		}
		else{
			ui_log.append("Found Bluetooth Adapter\n");
			int status = mBluetoothAdapter.getState();
			if (status == BluetoothAdapter.STATE_OFF) {	//bluetooth is disabled
				ui_log.append("Turn on Bluetooth Adapter\n");
				mBluetoothAdapter.enable();	// enable bluetooth
			}
			ui_log.append("Bluetooth Adapter Enabled\n");
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private class LeScanCallback  extends ScanCallback{
		/*** 扫描结果的回调，每次扫描到一个设备，就调用一次。
		 * @param callbackType
		 * @param result		 */
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			if(result != null){
				ui_log.append("Found Device: " + result.getDevice().getAddress() + " " + result.getDevice().getName() + "\n");//此处，我们尝试连接设备
				if (result.getDevice().getName() != null && result.getDevice().getName().equals("power watch2")) {//扫描到我们想要的设备后，立即停止扫描
					mBluetoothDevice = result.getDevice();
					ui_log.append("Stop Scan ...\n");
					mBluetoothLeScanner.stopScan(mScanCallback);
					mBluetoothLeScanner.flushPendingScanResults(mScanCallback);
					ui_log.append("Connecting GATT ...\n");
					mBluetoothDevice.connectGatt(BLEClockActivity.this, false, mGattCallback);
				}
			}
		}
	}

	private class AppendText implements Runnable {
		String text;
		AppendText(String text) {
			this.text = text;
		}
		@Override
		public void run() {
				ui_log.append(text+"\n");
		}
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){
		BLEClockActivity m_activity;
		/**		 * Callback indicating when GATT client has connected/disconnected to/from a remote GATT server		 *
		 * @param gatt 返回连接建立的gatt对象
		 * @param status 返回的是此次gatt操作的结果，成功了返回0
		 * @param newState 每次client连接或断开连接状态变化，STATE_CONNECTED 0，STATE_CONNECTING 1,STATE_DISCONNECTED 2,STATE_DISCONNECTING 3		 */
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
			m_activity = BLEClockActivity.this;
			mBluetoothGatt = gatt;
			m_activity.runOnUiThread(new AppendText("onConnectionStateChange status:" + status + " newState:" + newState));
			if(newState == BluetoothProfile.STATE_CONNECTED){
				m_activity.runOnUiThread(new AppendText("Discovering Services ..."));
				gatt.discoverServices();
			}else{
				m_activity.runOnUiThread(new AppendText("Error"));
			}
		}
		/**		 * Callback invoked when the list of remote services, characteristics and descriptors for the remote device have been updated, ie new services have been discovered.		 *
		 * @param gatt 返回的是本次连接的gatt对象
		 * @param status		 */
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			m_activity = BLEClockActivity.this;
			m_activity.runOnUiThread(new AppendText("onServicesDiscovered status " + status ));
			mServiceList = gatt.getServices();
			if(mServiceList != null) {
				m_activity.runOnUiThread(new AppendText("Services count: " + mServiceList.size()));
			}else{
				m_activity.runOnUiThread(new AppendText("No Services"));
			}
			for(BluetoothGattService service : mServiceList){
				List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
				m_activity.runOnUiThread(new AppendText("Service: " + service.getUuid()));
				for (BluetoothGattCharacteristic characteristic : characteristics) {
					m_activity.runOnUiThread(new AppendText("    characteristic: " + characteristic.getUuid().toString().substring(0,9)));
				}
			}
			Date mDate = Calendar.getInstance().getTime();
			m_activity.runOnUiThread(new AppendText("Set Time: " + mDate			));
			byte[] mValue = new byte[10];
			mValue[0] = (byte)0xC2;
			mValue[1] = (byte)0x07;
			mValue[2] = (byte)(mDate.getYear()-2000);
			mValue[3] = (byte)mDate.getMonth();
			mValue[4] = (byte)mDate.getDate();
			mValue[5] = (byte)mDate.getHours();
			mValue[6] = (byte)mDate.getMinutes();
			mValue[7] = (byte)mDate.getSeconds();
			mValue[8] = (byte)mDate.getDay();
			mValue[9] = (byte)(mValue[2]^mValue[3]^mValue[4]^mValue[5]^mValue[6]^mValue[7]^mValue[8]);
			mBluetoothGattCharacteristic = mServiceList.get(4).getCharacteristics().get(0);
			mBluetoothGattCharacteristic.setValue(mValue);
			m_activity.runOnUiThread(new AppendText("Value: " +  bytesToHex(mValue)));
			mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
		}

		/**		 * Callback triggered as a result of a remote characteristic notification.		 *
		 * @param gatt
		 * @param characteristic		 */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			m_activity.runOnUiThread(new AppendText("onCharacteristicChanged"));
		}

		/**		 * Callback indicating the result of a characteristic write operation.		 *
		 * @param gatt
		 * @param characteristic
		 * @param status		 */
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			m_activity.runOnUiThread(new AppendText("onCharacteristicWrite status " + status));
		}

		/**		 *Callback reporting the result of a characteristic read operation.		 *
		 * @param gatt
		 * @param characteristic
		 * @param status		 */
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			m_activity.runOnUiThread(new AppendText("onCharacteristicRead status " + status));
		}

		/**		 * Callback indicating the result of a descriptor write operation.		 *
		 * @param gatt
		 * @param descriptor
		 * @param status		 */
		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			m_activity.runOnUiThread(new AppendText("onDescriptorWrite status" + status));
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_bleclock, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
