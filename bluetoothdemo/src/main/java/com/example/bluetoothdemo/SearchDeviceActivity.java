package com.example.bluetoothdemo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SearchDeviceActivity extends Activity implements
		OnItemClickListener {

	private BluetoothAdapter blueadapter = null;
	private DeviceReceiver mydevice = new DeviceReceiver();
	private List<String> deviceList = new ArrayList<String>();
	private ListView deviceListview;
	private Button btserch;
	private ArrayAdapter<String> adapter;
	private boolean hasregister = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finddevice);
		setView();
		setBluetooth();

	}

	private void setView() {

		deviceListview = (ListView) findViewById(R.id.devicelist);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, deviceList);
		deviceListview.setAdapter(adapter);
		deviceListview.setOnItemClickListener(this);
		btserch = (Button) findViewById(R.id.start_seach);
		btserch.setOnClickListener(new ClinckMonitor());

	}

	@Override
	protected void onStart() {
		// ע���������չ㲥
		if (!hasregister) {
			hasregister = true;
			IntentFilter filterStart = new IntentFilter(
					BluetoothDevice.ACTION_FOUND);
			IntentFilter filterEnd = new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			registerReceiver(mydevice, filterStart);
			registerReceiver(mydevice, filterEnd);
		}
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		if (blueadapter != null && blueadapter.isDiscovering()) {
			blueadapter.cancelDiscovery();
		}
		if (hasregister) {
			hasregister = false;
			unregisterReceiver(mydevice);
		}
		super.onDestroy();
	}

	/**
	 * Setting Up Bluetooth
	 */
	private void setBluetooth() {
		blueadapter = BluetoothAdapter.getDefaultAdapter();

		if (blueadapter != null) { // Device support Bluetooth
			// ȷ�Ͽ�������
			if (!blueadapter.isEnabled()) {
				// �����û�����
				Intent intent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, RESULT_FIRST_USER);
				// ʹ�����豸�ɼ����������
				Intent in = new Intent(
						BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
				startActivity(in);
				// ֱ�ӿ�������������ʾ
				blueadapter.enable();
			}
		} else { // Device does not support Bluetooth

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("No bluetooth devices");
			dialog.setMessage("Your equipment does not support bluetooth, please change device");

			dialog.setNegativeButton("cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			dialog.show();
		}
	}

	/**
	 * Finding Devices
	 */
	private void findAvalibleDevice() {
		// ��ȡ����������豸
		Set<BluetoothDevice> device = blueadapter.getBondedDevices();

		if (blueadapter != null && blueadapter.isDiscovering()) {
			deviceList.clear();
			adapter.notifyDataSetChanged();
		}
		if (device.size() > 0) { // �����Ѿ���Թ��������豸
			for (Iterator<BluetoothDevice> it = device.iterator(); it.hasNext();) {
				BluetoothDevice btd = it.next();
				deviceList.add(btd.getName() + '\n' + btd.getAddress());
				adapter.notifyDataSetChanged();
			}
		} else { // �������Ѿ���Թ��������豸
			deviceList.add("No can be matched to use bluetooth");
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case RESULT_OK:
			findAvalibleDevice();
			break;
		case RESULT_CANCELED:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class ClinckMonitor implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (blueadapter.isDiscovering()) {
				blueadapter.cancelDiscovery();
				btserch.setText("repeat search");
			} else {
				findAvalibleDevice();
				blueadapter.startDiscovery();
				btserch.setText("stop search");
			}
		}
	}

	/**
	 * ��������״̬�㲥����
	 * 
	 * @author Andy
	 * 
	 */
	private class DeviceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) { // ���������豸
				BluetoothDevice btd = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// ����û������Ե������豸
				if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {
					deviceList.add(btd.getName() + '\n' + btd.getAddress());
					adapter.notifyDataSetChanged();
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) { // ��������

				if (deviceListview.getCount() == 0) {
					deviceList.add("No can be matched to use bluetooth");
					adapter.notifyDataSetChanged();
				}
				btserch.setText("repeat search");
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {

		Log.e("msgParent", "Parent= " + arg0);
		Log.e("msgView", "View= " + arg1);
		Log.e("msgChildView",
				"ChildView= "
						+ arg0.getChildAt(pos - arg0.getFirstVisiblePosition()));

		final String msg = deviceList.get(pos);

		if (blueadapter != null && blueadapter.isDiscovering()) {
			blueadapter.cancelDiscovery();
			btserch.setText("repeat search");
		}

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);// ����һ�����������
		dialog.setTitle("Confirmed connecting device");
		dialog.setMessage(msg);
		dialog.setPositiveButton("client",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						BluetoothMsg.BlueToothAddress = msg.substring(msg
								.length() - 17);

						if (BluetoothMsg.lastblueToothAddress != BluetoothMsg.BlueToothAddress) {
							BluetoothMsg.lastblueToothAddress = BluetoothMsg.BlueToothAddress;
						}
						BluetoothMsg.serviceOrCilent=BluetoothMsg.ServerOrCilent.CILENT;
						Intent in = new Intent(SearchDeviceActivity.this,
								BluetoothActivity.class);
						startActivity(in);

					}
				});
		dialog.setNegativeButton("service",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
//						BluetoothMsg.BlueToothAddress = null;
						BluetoothMsg.BlueToothAddress = msg.substring(msg
								.length() - 17);

						if (BluetoothMsg.lastblueToothAddress != BluetoothMsg.BlueToothAddress) {
							BluetoothMsg.lastblueToothAddress = BluetoothMsg.BlueToothAddress;
						}
						BluetoothMsg.serviceOrCilent=BluetoothMsg.ServerOrCilent.SERVICE;
						Intent in = new Intent(SearchDeviceActivity.this,
								BluetoothActivity.class);
						startActivity(in);
					}
				});
		dialog.show();
	}


}
