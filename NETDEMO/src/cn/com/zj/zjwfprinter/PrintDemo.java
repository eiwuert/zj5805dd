package cn.com.zj.zjwfprinter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import zj.com.customize.sdk.Other;
import android.annotation.SuppressLint;
import android.app.Activity;  
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;  
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import cn.com.zj.command.sdk.Command;
import cn.com.zj.command.sdk.PrintPicture;
import cn.com.zj.command.sdk.PrinterCommand;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zj.wfsdk.*;

public class PrintDemo extends Activity {
    private Button btnConn = null;
	private Button btnPrint = null;
	private Button btn_test = null;
	private Button btnClose = null;
	private Button btn_prtpicture = null;
	private EditText edtContext = null;
	private WifiCommunication wfComm = null;
	private EditText txt_ip = null;
	private Button btn_printA = null;
    private ImageView imageViewPicture = null;
    private static boolean is58mm = true;
	private RadioButton width_58mm, width_80;
	private CheckBox hexBox;
	private Button btn_ChoseCommand = null;
	private Button btn_prtsma = null;
	private Button btn_prttableButton = null;
	private Button btn_prtcodeButton = null;
	private Button btn_scqrcode = null;
	private Button btn_camer = null;
	
	int  connFlag = 0;
	revMsgThread revThred = null;
	//checkPrintThread cheThread = null;
	private static final int WFPRINTER_REVMSG = 0x06;
	
	// Intent request codes
	private static final int REQUEST_CHOSE_BMP = 1;
	private static final int REQUEST_CAMER = 2;
	
	//QRcode
	private static final int QR_WIDTH = 350;
	private static final int QR_HEIGHT = 350;

/******************************************************************************************************/
    final String[] items = { "复位打印机", "打印并走纸", "标准ASCII字体", "压缩ASCII字体", "取消倍高倍宽",
			"倍高倍宽", "取消加粗模式", "选择加粗模式", "取消倒置打印", "选择倒置打印", "取消黑白反显", "选择黑白反显",
			"取消顺时针旋转90°", "选择顺时针旋转90°", "走纸到切刀位置并切纸", "蜂鸣指令", "标准钱箱指令", 
			"实时弹钱箱指令", "进入字符模式", "进入中文模式", "打印自检页", "禁止按键", "取消禁止按键" ,
			"设置汉字字符下划线", "取消汉字字符下划线", "进入十六进制模式" };
    final String[] itemsen = { "Print Init", "Print and Paper", "Standard ASCII font", "Compressed ASCII font", "Normal size",			
    	    "Double high power wide", "Twice as high power wide", "Three times the high-powered wide", "Off emphasized mode", "Choose bold mode", "Cancel inverted Print", "Invert selection Print", "Cancel black and white reverse display", "Choose black and white reverse display",
    		"Cancel rotated clockwise 90 °", "Select the clockwise rotation of 90 °", "Feed paper Cut", "Beep", "Standard CashBox", 
    		"Open CashBox", "Char Mode", "Chinese Mode", "Print SelfTest", "DisEnable Button", "Enable Button" ,
    		"Set Underline", "Cancel Underline", "Hex Mode" };
	final byte[][] byteCommands = { 
			{ 0x1b, 0x40, 0x0a },// 复位打印机
			{ 0x0a }, //打印并走纸
			{ 0x1b, 0x4d, 0x00 },// 标准ASCII字体
			{ 0x1b, 0x4d, 0x01 },// 压缩ASCII字体
			{ 0x1d, 0x21, 0x00 },// 字体不放大
			{ 0x1d, 0x21, 0x11 },// 宽高加倍
			{ 0x1b, 0x45, 0x00 },// 取消加粗模式
			{ 0x1b, 0x45, 0x01 },// 选择加粗模式
			{ 0x1b, 0x7b, 0x00 },// 取消倒置打印
			{ 0x1b, 0x7b, 0x01 },// 选择倒置打印
			{ 0x1d, 0x42, 0x00 },// 取消黑白反显
			{ 0x1d, 0x42, 0x01 },// 选择黑白反显
			{ 0x1b, 0x56, 0x00 },// 取消顺时针旋转90°
			{ 0x1b, 0x56, 0x01 },// 选择顺时针旋转90°
			{ 0x0a, 0x1d, 0x56, 0x42, 0x01, 0x0a },//切刀指令
			{ 0x1b, 0x42, 0x03, 0x03 },//蜂鸣指令
			{ 0x1b, 0x70, 0x00, 0x50, 0x50 },//钱箱指令
			{ 0x10, 0x14, 0x00, 0x05, 0x05 },//实时弹钱箱指令
			{ 0x1c, 0x2e },// 进入字符模式
			{ 0x1c, 0x26 }, //进入中文模式
			{ 0x1f, 0x11, 0x04 }, //打印自检页
			{ 0x1b, 0x63, 0x35, 0x01 }, //禁止按键
			{ 0x1b, 0x63, 0x35, 0x00 }, //取消禁止按键
			{ 0x1b, 0x2d, 0x02, 0x1c, 0x2d, 0x02 }, //设置下划线
			{ 0x1b, 0x2d, 0x00, 0x1c, 0x2d, 0x00 }, //取消下划线
			{ 0x1f, 0x11, 0x03 }, //打印机进入16进制模式
	};
/******************************************************************************************************/
/***************************条                          码***************************************************************/
	final String[] codebar = { "UPC_A", "UPC_E", "JAN13(EAN13)", "JAN8(EAN8)", 
							   "CODE39", "ITF", "CODABAR", "CODE93", "CODE128", "QR Code" };
	final byte[][] byteCodebar = { 
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
	};
/******************************************************************************************************/
	
	@Override  
	public void onCreate(Bundle savedInstanceState){  
	    super.onCreate(savedInstanceState);  
	    setContentView(R.layout.activity_main);
	    btnConn = (Button) this.findViewById(R.id.btn_conn); 
	    btnConn.setOnClickListener(new ClickEvent());
	    btnPrint = (Button) this.findViewById(R.id.btnSend);
	    btnPrint.setOnClickListener(new ClickEvent());
	    btn_test = (Button) this.findViewById(R.id.btn_test);
	    btn_test.setOnClickListener(new ClickEvent());
	    btnClose = (Button) this.findViewById(R.id.btnClose);
	    btnClose.setOnClickListener(new ClickEvent());
	    edtContext = (EditText) this.findViewById(R.id.txt_content);
	    txt_ip = (EditText)this.findViewById(R.id.txt_ip);
	    wfComm = new WifiCommunication(mHandler);
	    btn_prtpicture = (Button)this.findViewById(R.id.btn_picture);
	    btn_prtpicture.setOnClickListener(new ClickEvent());
	    
	    btn_printA = (Button)findViewById(R.id.btn_prtAr);
		btn_printA.setOnClickListener(new ClickEvent());
		
		btn_ChoseCommand = (Button)findViewById(R.id.btn_prtcommand);
		btn_ChoseCommand.setOnClickListener(new ClickEvent());
		
		btn_prtsma = (Button)findViewById(R.id.btn_prtsma);
		btn_prtsma.setOnClickListener(new ClickEvent());
		
		btn_prttableButton = (Button)findViewById(R.id.btn_prttable);
		btn_prttableButton.setOnClickListener(new ClickEvent());
		
		btn_prtcodeButton = (Button)findViewById(R.id.btn_prtbarcode);
		btn_prtcodeButton.setOnClickListener(new ClickEvent());
		
		btn_camer = (Button)findViewById(R.id.btn_dyca);
		btn_camer.setOnClickListener(new ClickEvent());
		
		btn_scqrcode = (Button)findViewById(R.id.btn_scqr);
		btn_scqrcode.setOnClickListener(new ClickEvent());
		
		imageViewPicture = (ImageView) findViewById(R.id.imageViewPictureUSB);
		imageViewPicture.setOnClickListener(new ClickEvent());
		
		Bitmap bm = getImageFromAssetsFile("demo.bmp");
		if (null != bm) {
			imageViewPicture.setImageBitmap(bm);
		}
		
		hexBox = (CheckBox)findViewById(R.id.checkBoxHEX);
		hexBox.setOnClickListener(new ClickEvent());
		
		width_58mm = (RadioButton)findViewById(R.id.width_58mm);
		width_58mm.setOnClickListener(new ClickEvent());
		
		width_80 = (RadioButton)findViewById(R.id.width_80mm);
		width_80.setOnClickListener(new ClickEvent());
	    
	    btnConn.setEnabled(true);
	    btnPrint.setEnabled(false);
	    btn_test.setEnabled(false);
	    btnClose.setEnabled(false);
	    btn_prtpicture.setEnabled(false);
	    btn_printA.setEnabled(false);
	    btn_ChoseCommand.setEnabled(false);
	    btn_prtsma.setEnabled(false);
	    btn_prttableButton.setEnabled(false);
	    btn_prtcodeButton.setEnabled(false);
	    btn_scqrcode.setEnabled(false);
	    btn_camer.setEnabled(false);
	}   
	  
	@Override
	protected void onDestroy() {
		super.onDestroy();
		wfComm.close();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		case REQUEST_CHOSE_BMP:{
        	if (resultCode == Activity.RESULT_OK){
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaColumns.DATA };
	
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
	
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
	
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(picturePath, opts);
				opts.inJustDecodeBounds = false;
				if (opts.outWidth > 1200) {
					opts.inSampleSize = opts.outWidth / 1200;
				}
				Bitmap bitmap = BitmapFactory.decodeFile(picturePath, opts);
				if (null != bitmap) {
					imageViewPicture.setImageBitmap(bitmap);
				}
        	}else{
				Toast.makeText(this, getString(R.string.msg_statev1), Toast.LENGTH_SHORT).show();
			}
			break;
		}
		case REQUEST_CAMER:{
			if (resultCode == Activity.RESULT_OK){
        		handleSmallCameraPhoto(data);
        	}else{
        		Toast.makeText(this, getText(R.string.camer), Toast.LENGTH_SHORT).show();
        	}
			break;
		}
		default:
			break;
		}
	}
/**************************************************************************************************/	  
	class ClickEvent implements View.OnClickListener {
		public void onClick(View v) {
			if (v == btnConn) {	
				if( connFlag == 0 ){   //避免连续点击此按钮创建多个连接线程
					connFlag = 1;
				    Log.d("wifi调试","点击\"连接\"");
				    String strAddressIp = txt_ip.getText().toString();
				    wfComm.initSocket(strAddressIp,9100);
				}
			} else if (v == btnPrint) {
                String msg = edtContext.getText().toString();
                if( msg.length() > 0 ){
           			byte[] tcmd = new byte[3];
        			tcmd[0] = 0x10;
        			tcmd[1] = 0x04;
        			tcmd[2] = 0x01;     //检测是否有纸指令
                    wfComm.sndByte(tcmd);
                	wfComm.sendMsg(msg,"gbk");
                    byte[] tail = new byte[1];
                    wfComm.sndByte(tail);
                }
			} else if (v == btnClose) {
				wfComm.close();
			}else if( v == btn_prtpicture ){ 
				Print_BMP();
			}else if (v == btn_test) {
				BluetoothPrintTest();
			}else if(v == width_58mm || v == width_80){
				is58mm = v == width_58mm;
				width_58mm.setChecked(is58mm);
				width_80.setChecked(!is58mm);
			}else if(v == btn_printA){

				String txt_msg = edtContext.getText().toString(); 
				if(txt_msg.length() == 0){
					Toast.makeText(PrintDemo.this, getText(R.string.empty1), Toast.LENGTH_SHORT).show();
					return;
				}else{
					Bitmap bm1 = getImageFromAssetsFile("demo.jpg");
					if(width_58mm.isChecked()){
						Bitmap bmp = Other.createAppIconText(bm1,txt_msg,24,is58mm,200);
						imageViewPicture.setImageBitmap(bmp);
						byte[] buffer = PrinterCommand.POS_Set_PrtInit();
						byte[] sp = PrinterCommand.POS_Set_LineSpace(0);
						int nMode = 0;
						int nPaperWidth = 384;
						
						if(bmp != null)
						{
							wfComm.sndByte(buffer);
							wfComm.sndByte(sp);
							byte[] data = PrintPicture.POS_PrintBMP(bmp, nPaperWidth, nMode);
							wfComm.sndByte(data);
							wfComm.sndByte(buffer);
						}
					}
					else if (width_80.isChecked()){
						Bitmap bmp = Other.createAppIconText(bm1,txt_msg,24,false,200);
						imageViewPicture.setImageBitmap(bmp);
						byte[] buffer = PrinterCommand.POS_Set_PrtInit();
						byte[] sp = PrinterCommand.POS_Set_LineSpace(0);
						int nMode = 0;
						
						int nPaperWidth = 576;
						if(bmp != null)
						{
							wfComm.sndByte(buffer);
							wfComm.sndByte(sp);
							byte[] data = PrintPicture.POS_PrintBMP(bmp, nPaperWidth, nMode);
							wfComm.sndByte(data);
							wfComm.sndByte(buffer);
						}
					}
				}
			}else if(v == btn_camer){
				dispatchTakePictureIntent(REQUEST_CAMER);
			}else if(v == btn_ChoseCommand){

				String lang = getString(R.string.strLang);
				if((lang.compareTo("ch")) == 0){
				new AlertDialog.Builder(PrintDemo.this).setTitle(getText(R.string.chosecommand))
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						SendDataByte(byteCommands[which]);
						try {
							if(which == 16 || which == 17 || which == 18 || which == 19 || which == 22
							|| which == 23 || which == 24|| which == 0 || which == 1 || which == 27){
								return ;
							}else {
								SendDataByte("热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n".getBytes("GBK"));
							}
							
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).create().show();	
				}else if((lang.compareTo("en")) == 0){
					new AlertDialog.Builder(PrintDemo.this).setTitle(getText(R.string.chosecommand))
					.setItems(itemsen, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							SendDataByte(byteCommands[which]);
							try {
								if(which == 16 || which == 17 || which == 18 || which == 19 || which == 22
								|| which == 23 || which == 24|| which == 0 || which == 1 || which == 27){
									return ;
								}else {
									SendDataByte("Thermal Receipt Printer ABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\n".getBytes("GBK"));
								}
								
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}).create().show();	
					}
			}else if(v == btn_prtsma){
				Print_Ex();
			}else if(v == btn_prttableButton){
				PrintTable();
			}else if(v == btn_scqrcode){
				createImage();
			}else if(v == btn_prtcodeButton){
				printBarCode();
			}else if(v == imageViewPicture){
				Intent loadpicture = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(loadpicture, REQUEST_CHOSE_BMP);
			}
		}
	}  

/***********************************************************************************************/	
    @SuppressLint("HandlerLeak") private final  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case WifiCommunication.WFPRINTER_CONNECTED:
            	connFlag = 0;
            	Toast.makeText(getApplicationContext(), "Connect the WIFI-printer successful",
                        Toast.LENGTH_SHORT).show();
        	    btnPrint.setEnabled(true);
        	    btn_test.setEnabled(true);
        	    btnClose.setEnabled(true);
        	    btn_prtpicture.setEnabled(true);
        	    btnConn.setEnabled(false);
        	    btn_printA.setEnabled(true);
        	    btn_ChoseCommand.setEnabled(true);
        	    btn_prtsma.setEnabled(true);
        	    btn_prttableButton.setEnabled(true);
        	    btn_prtcodeButton.setEnabled(true);
        	    btn_scqrcode.setEnabled(true);
        	    btn_camer.setEnabled(true);
        	    
        	    revThred = new revMsgThread();
        	    revThred.start();
            	break;
            case WifiCommunication.WFPRINTER_DISCONNECTED:
            	Toast.makeText(getApplicationContext(), "Disconnect the WIFI-printer successful",
                        Toast.LENGTH_SHORT).show();
    		    btnConn.setEnabled(true);
			    btnPrint.setEnabled(false);
			    btn_test.setEnabled(false);
			    btnClose.setEnabled(false);
			    btn_prtpicture.setEnabled(false);
			    btn_printA.setEnabled(false);
			    btn_ChoseCommand.setEnabled(false);
			    btn_prtsma.setEnabled(false);
			    btn_prttableButton.setEnabled(false);
			    btn_prtcodeButton.setEnabled(false);
			    btn_scqrcode.setEnabled(false);
			    btn_camer.setEnabled(false);
			    revThred.interrupt();
            	break;
            case WifiCommunication.SEND_FAILED:
            	connFlag = 0;
            	Toast.makeText(getApplicationContext(), "Send Data Failed,please reconnect",
                        Toast.LENGTH_SHORT).show();
    		    btnConn.setEnabled(true);
			    btnPrint.setEnabled(false);
			    btn_test.setEnabled(false);
			    btnClose.setEnabled(false);
			    btn_prtpicture.setEnabled(false);
			    btn_printA.setEnabled(false);
			    btn_ChoseCommand.setEnabled(false);
			    btn_prtsma.setEnabled(false);
			    btn_prttableButton.setEnabled(false);
			    btn_prtcodeButton.setEnabled(false);
			    btn_scqrcode.setEnabled(false);
			    btn_camer.setEnabled(false);
			    revThred.interrupt();
            	break;
            case WifiCommunication.WFPRINTER_CONNECTEDERR:
            	connFlag = 0;
            	Toast.makeText(getApplicationContext(), "Connect the WIFI-printer error",
                        Toast.LENGTH_SHORT).show();
            	break;
            case WFPRINTER_REVMSG:
            	byte revData = (byte)Integer.parseInt(msg.obj.toString());
            	if(((revData >> 6) & 0x01) == 0x01)
            		Toast.makeText(getApplicationContext(), "The printer has no paper",Toast.LENGTH_SHORT).show();    
                break;
            default:
                break;
            }
        }
    };
    
    class checkPrintThread extends Thread {
    	@Override
    	public void run() {
			byte[] tcmd = new byte[3];
			tcmd[0] = 0x10;
			tcmd[1] = 0x04;
			tcmd[2] = 0x04;
    		try {
                while(true){
				    wfComm.sndByte(tcmd);
				    Thread.sleep(15);
				    Log.d("wifi调试","发送一次调试数据");
                }
    		}catch (InterruptedException e){
				e.printStackTrace();
				Log.d("wifi调试","退出线程");
    		}
    	}
    }
    
    //打印机线程，连接上打印机时创建，关闭打印机时退出
	class revMsgThread extends Thread {	
		@Override
		public void run() {            
			try {
				Message msg = new Message();
				int revData;
				while(true)
	            {
					revData = wfComm.revByte();               //非阻塞单个字节接收数据，如需改成非阻塞接收字符串请参考手册
					if(revData != -1){
						
						msg = mHandler.obtainMessage(WFPRINTER_REVMSG);
		                msg.obj = revData;
		                mHandler.sendMessage(msg);
					}    
				    Thread.sleep(20);
	            }
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.d("wifi调试","退出线程");
			}
		}
	}
/****************************************************************************************************/

	/*
	 * 打印图片
	 */
	private void Print_BMP(){

		byte[] buffer = PrinterCommand.POS_Set_PrtInit();
		Bitmap mBitmap = ((BitmapDrawable) imageViewPicture.getDrawable())
				.getBitmap();
		int nMode = 0;
		int nPaperWidth = 384;
		if(width_58mm.isChecked())
			nPaperWidth = 384;
		else if (width_80.isChecked())
			nPaperWidth = 576;
		if(mBitmap != null)
		{
			byte[] data = PrintPicture.POS_PrintBMP(mBitmap, nPaperWidth, nMode);
			wfComm.sndByte(buffer);
			wfComm.sndByte(data);
			wfComm.sndByte(new byte[]{0x0a});
		}		
	}
	
	/**
	 * 打印自定义表格
	 */
	@SuppressLint("SimpleDateFormat") private void PrintTable(){

		String lang = getString(R.string.strLang);
		if((lang.compareTo("ch")) == 0){
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String str = formatter.format(curDate);
		String date = str + "\n\n\n\n\n\n";	
		if(is58mm){

			Command.ESC_Align[2] = 0x02;
			byte[][] allbuf;
			try {
				allbuf = new byte[][]{

						Command.ESC_Init, Command.ESC_Three,
						String.format("┏━━┳━━━┳━━┳━━━━┓\n").getBytes("GBK"),
						String.format("┃发站┃%-4s┃到站┃%-6s┃\n","深圳","成都").getBytes("GBK"),
						String.format("┣━━╋━━━╋━━╋━━━━┫\n").getBytes("GBK"),
						String.format("┃件数┃%2d/%-3d┃单号┃%-8d┃\n",1,222,555).getBytes("GBK"),
						String.format("┣━━┻┳━━┻━━┻━━━━┫\n").getBytes("GBK"),
						String.format("┃收件人┃%-12s┃\n","【送】测试/测试人").getBytes("GBK"),
						String.format("┣━━━╋━━┳━━┳━━━━┫\n").getBytes("GBK"),
						String.format("┃业务员┃%-2s┃名称┃%-6s┃\n","测试","深圳").getBytes("GBK"),
						String.format("┗━━━┻━━┻━━┻━━━━┛\n").getBytes("GBK"),
						Command.ESC_Align, "\n".getBytes("GBK")
				};
				byte[] buf = Other.byteArraysToBytes(allbuf);
				SendDataByte(buf);
				SendDataString(date);
				SendDataByte(Command.GS_V_m_n);
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}else {

			Command.ESC_Align[2] = 0x02;
			byte[][] allbuf;
			try {
				allbuf = new byte[][]{

						Command.ESC_Init, Command.ESC_Three,
						String.format("┏━━┳━━━━━━━┳━━┳━━━━━━━━┓\n").getBytes("GBK"),
						String.format("┃发站┃%-12s┃到站┃%-14s┃\n", "深圳", "成都").getBytes("GBK"),
						String.format("┣━━╋━━━━━━━╋━━╋━━━━━━━━┫\n").getBytes("GBK"),
						String.format("┃件数┃%6d/%-7d┃单号┃%-16d┃\n", 1, 222, 55555555).getBytes("GBK"),
						String.format("┣━━┻┳━━━━━━┻━━┻━━━━━━━━┫\n").getBytes("GBK"),
						String.format("┃收件人┃%-28s┃\n", "【送】测试/测试人").getBytes("GBK"),
						String.format("┣━━━╋━━━━━━┳━━┳━━━━━━━━┫\n").getBytes("GBK"),
						String.format("┃业务员┃%-10s┃名称┃%-14s┃\n", "测试", "深圳").getBytes("GBK"),
						String.format("┗━━━┻━━━━━━┻━━┻━━━━━━━━┛\n").getBytes("GBK"),
						Command.ESC_Align, "\n".getBytes("GBK")
				};
				byte[] buf = Other.byteArraysToBytes(allbuf);
				SendDataByte(buf);
				SendDataString(date);
				SendDataByte(Command.GS_V_m_n);
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		}else if((lang.compareTo("en")) == 0){
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd/ HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间
			String str = formatter.format(curDate);
			String date = str + "\n\n\n\n\n\n";	
			if(is58mm){

				Command.ESC_Align[2] = 0x02;
				byte[][] allbuf;
				try {
					allbuf = new byte[][]{

							Command.ESC_Init, Command.ESC_Three,
							String.format("┏━━┳━━━┳━━┳━━━━┓\n").getBytes("GBK"),
							String.format("┃XXXX┃%-6s┃XXXX┃%-8s┃\n","XXXX","XXXX").getBytes("GBK"),
							String.format("┣━━╋━━━╋━━╋━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXX┃%2d/%-3d┃XXXX┃%-8d┃\n",1,222,555).getBytes("GBK"),
							String.format("┣━━┻┳━━┻━━┻━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXXXX┃%-18s┃\n","【XX】XXXX/XXXXXX").getBytes("GBK"),
							String.format("┣━━━╋━━┳━━┳━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXXXX┃%-2s┃XXXX┃%-8s┃\n","XXXX","XXXX").getBytes("GBK"),
							String.format("┗━━━┻━━┻━━┻━━━━┛\n").getBytes("GBK"),
							Command.ESC_Align, "\n".getBytes("GBK")
					};
					byte[] buf = Other.byteArraysToBytes(allbuf);
					SendDataByte(buf);
					SendDataString(date);
					SendDataByte(Command.GS_V_m_n);
				} catch (UnsupportedEncodingException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}else {

				Command.ESC_Align[2] = 0x02;
				byte[][] allbuf;
				try {
					allbuf = new byte[][]{

							Command.ESC_Init, Command.ESC_Three,
							String.format("┏━━┳━━━━━━━┳━━┳━━━━━━━━┓\n").getBytes("GBK"),
							String.format("┃XXXX┃%-14s┃XXXX┃%-16s┃\n", "XXXX", "XXXX").getBytes("GBK"),
							String.format("┣━━╋━━━━━━━╋━━╋━━━━━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXX┃%6d/%-7d┃XXXX┃%-16d┃\n", 1, 222, 55555555).getBytes("GBK"),
							String.format("┣━━┻┳━━━━━━┻━━┻━━━━━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXXXX┃%-34s┃\n", "【XX】XXXX/XXXXXX").getBytes("GBK"),
							String.format("┣━━━╋━━━━━━┳━━┳━━━━━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXXXX┃%-12s┃XXXX┃%-16s┃\n", "XXXX", "XXXX").getBytes("GBK"),
							String.format("┗━━━┻━━━━━━┻━━┻━━━━━━━━┛\n").getBytes("GBK"),
							Command.ESC_Align, "\n".getBytes("GBK")
					};
					byte[] buf = Other.byteArraysToBytes(allbuf);
					SendDataByte(buf);
					SendDataString(date);
					SendDataByte(Command.GS_V_m_n);
				} catch (UnsupportedEncodingException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
			}
	}

	/**
	 * 打印自定义小票
	 */
	@SuppressLint("SimpleDateFormat") private void Print_Ex(){

		String lang = getString(R.string.strLang);
		if((lang.compareTo("ch")) == 0){
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String str = formatter.format(curDate);
		String date = str + "\n\n\n\n\n\n";	
		if (is58mm) {

			try {
				byte[] qrcode = PrinterCommand.getBarCommand("资江电子热敏票据打印机!", 0, 3, 6);//
				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				SendDataByte(qrcode);

				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x11;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("NIKE专卖店\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x00;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x00;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("门店号: 888888\n单据  S00003333\n收银员：1001\n单据日期：xxxx-xx-xx\n打印时间：xxxx-xx-xx  xx:xx:xx\n".getBytes("GBK"));
				SendDataByte("品名       数量    单价    金额\nNIKE跑鞋   10.00   899     8990\nNIKE篮球鞋 10.00   1599    15990\n".getBytes("GBK"));
				SendDataByte("数量：                20.00\n总计：                16889.00\n付款：                17000.00\n找零：                111.00\n".getBytes("GBK"));
				SendDataByte("公司名称：NIKE\n公司网址：www.xxx.xxx\n地址：深圳市xx区xx号\n电话：0755-11111111\n服务专线：400-xxx-xxxx\n================================\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x11;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("谢谢惠顾,欢迎再次光临!\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x00;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x00;
				SendDataByte(Command.GS_ExclamationMark);
				
				SendDataByte("(以上信息为测试模板,如有苟同，纯属巧合!)\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x02;
				SendDataByte(Command.ESC_Align);
				SendDataString(date);
				SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
				SendDataByte(Command.GS_V_m_n);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				byte[] qrcode = PrinterCommand.getBarCommand("资江电子热敏票据打印机!", 0, 3, 8);
				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				SendDataByte(qrcode);

				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x11;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("NIKE专卖店\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x00;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x00;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("门店号: 888888\n单据  S00003333\n收银员：1001\n单据日期：xxxx-xx-xx\n打印时间：xxxx-xx-xx  xx:xx:xx\n".getBytes("GBK"));
				SendDataByte("品名            数量    单价    金额\nNIKE跑鞋        10.00   899     8990\nNIKE篮球鞋      10.00   1599    15990\n".getBytes("GBK"));
				SendDataByte("数量：                20.00\n总计：                16889.00\n付款：                17000.00\n找零：                111.00\n".getBytes("GBK"));
				SendDataByte("公司名称：NIKE\n公司网址：www.xxx.xxx\n地址：深圳市xx区xx号\n电话：0755-11111111\n服务专线：400-xxx-xxxx\n===========================================\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x11;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("谢谢惠顾,欢迎再次光临!\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x00;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x00;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("(以上信息为测试模板,如有苟同，纯属巧合!)\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x02;
				SendDataByte(Command.ESC_Align);
				SendDataString(date);
				SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
				SendDataByte(Command.GS_V_m_n);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}else if((lang.compareTo("en")) == 0){
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd/ HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间
			String str = formatter.format(curDate);
			String date = str + "\n\n\n\n\n\n";	
			if (is58mm) {

				try {
					byte[] qrcode = PrinterCommand.getBarCommand("Zijiang Electronic Thermal Receipt Printer!", 0, 3, 6);//
					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					SendDataByte(qrcode);

					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x11;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("NIKE Shop\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x00;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x00;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("Number:  888888\nReceipt  S00003333\nCashier：1001\nDate：xxxx-xx-xx\nPrint Time：xxxx-xx-xx  xx:xx:xx\n".getBytes("GBK"));
					SendDataByte("Name    Quantity    price  Money\nShoes   10.00       899     8990\nBall    10.00       1599    15990\n".getBytes("GBK"));
					SendDataByte("Quantity：             20.00\ntotal：                16889.00\npayment：              17000.00\nKeep the change：      111.00\n".getBytes("GBK"));
					SendDataByte("company name：NIKE\nSite：www.xxx.xxx\naddress：ShenzhenxxAreaxxnumber\nphone number：0755-11111111\nHelpline：400-xxx-xxxx\n================================\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x11;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("Welcome again!\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x00;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x00;
					SendDataByte(Command.GS_ExclamationMark);
					
					SendDataByte("(The above information is for testing template, if agree, is purely coincidental!)\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x02;
					SendDataByte(Command.ESC_Align);
					SendDataString(date);
					SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
					SendDataByte(Command.GS_V_m_n);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					byte[] qrcode = PrinterCommand.getBarCommand("Zijiang Electronic Thermal Receipt Printer!", 0, 3, 8);
					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					SendDataByte(qrcode);

					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x11;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("NIKE Shop\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x00;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x00;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("Number: 888888\nReceipt  S00003333\nCashier：1001\nDate：xxxx-xx-xx\nPrint Time：xxxx-xx-xx  xx:xx:xx\n".getBytes("GBK"));
					SendDataByte("Name                    Quantity price  Money\nNIKErunning shoes        10.00   899     8990\nNIKEBasketball Shoes     10.00   1599    15990\n".getBytes("GBK"));
					SendDataByte("Quantity：               20.00\ntotal：                  16889.00\npayment：                17000.00\nKeep the change：                111.00\n".getBytes("GBK"));
					SendDataByte("company name：NIKE\nSite：www.xxx.xxx\naddress：shenzhenxxAreaxxnumber\nphone number：0755-11111111\nHelpline：400-xxx-xxxx\n================================================\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x11;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("Welcome again!\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x00;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x00;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("(The above information is for testing template, if agree, is purely coincidental!)\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x02;
					SendDataByte(Command.ESC_Align);
					SendDataString(date);
					SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
					SendDataByte(Command.GS_V_m_n);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}
	}

	/**
	 * 打印条码、二维码
	 */
	public void printBarCode() {

		new AlertDialog.Builder(PrintDemo.this).setTitle(getText(R.string.btn_prtcode))
		.setItems(codebar, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SendDataByte(byteCodebar[which]);
				String str = edtContext.getText().toString();
				if(which == 0)
				{
					if(str.length() == 11 || str.length() == 12)
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 65, 3, 168, 0, 2);
						SendDataByte(new byte[]{0x1b, 0x61, 0x00});
						SendDataString("UPC_A\n");
						SendDataByte(code);
					}else {
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(which == 1)
				{
					if(str.length() == 6 || str.length() == 7)
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 66, 3, 168, 0, 2);
						SendDataByte(new byte[]{0x1b, 0x61, 0x00});
						SendDataString("UPC_E\n");
						SendDataByte(code);
					}else {
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(which == 2)
				{
					if(str.length() == 12 || str.length() == 13)
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 67, 3, 168, 0, 2);
						SendDataByte(new byte[]{0x1b, 0x61, 0x00});
						SendDataString("JAN13(EAN13)\n");
						SendDataByte(code);
					}else {
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(which == 3)
				{
					if(str.length() >0 )
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 68, 3, 168, 0, 2);
						SendDataByte(new byte[]{0x1b, 0x61, 0x00});
						SendDataString("JAN8(EAN8)\n");
						SendDataByte(code);
					}else {
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(which == 4)
				{
					if(str.length() == 0)
					{
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 69, 3, 168, 1, 2);
						SendDataString("CODE39\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 5)
				{
					if(str.length() == 0)
					{
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 70, 3, 168, 1, 2);
						SendDataString("ITF\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 6)
				{
					if(str.length() == 0)
					{
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 71, 3, 168, 1, 2);
						SendDataString("CODABAR\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 7)
				{
					if(str.length() == 0)
					{
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 72, 3, 168, 1, 2);
						SendDataString("CODE93\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 8)
				{
					if(str.length() == 0)
					{
						Toast.makeText(PrintDemo.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 73, 3, 168, 1, 2);
						SendDataString("CODE128\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 9)
				{
					if(str.length() == 0)
					{
						Toast.makeText(PrintDemo.this, getText(R.string.empty1), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getBarCommand(str, 1, 3, 8);
						SendDataString("QR Code\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
			}
		}).create().show();	
	}

	/*
	 * 打印测试页
	 * @param mPrinter
	 */
	private void BluetoothPrintTest() {
		String msg = "";
		String lang = getString(R.string.strLang);
		if((lang.compareTo("en")) == 0){
			msg = "Division I is a research and development, production and services in one high-tech research and development, production-oriented enterprises, specializing in POS terminals finance, retail, restaurants, bars, songs and other areas, computer terminals, self-service terminal peripheral equipment R & D, manufacturing and sales! \n company's organizational structure concise and practical, pragmatic style of rigorous, efficient operation. Integrity, dedication, unity, and efficient is the company's corporate philosophy, and constantly strive for today, vibrant, the company will be strong scientific and technological strength, eternal spirit of entrepreneurship, the pioneering and innovative attitude, confidence towards the international information industry, with friends to create brilliant information industry !!! \n\n\n";
			SendDataString(msg);
		}else if((lang.compareTo("ch")) == 0){
			msg = "我司是一家集科研开发、生产经营和服务于一体的高技术研发、生产型企业，专业从事金融、商业零售、餐饮、酒吧、歌吧等领域的POS终端、计算机终端、自助终端周边配套设备的研发、制造及销售！\n公司的组织机构简练实用，作风务实严谨，运行高效。诚信、敬业、团结、高效是公司的企业理念和不断追求今天，朝气蓬勃，公司将以雄厚的科技力量，永恒的创业精神，不断开拓创新的姿态，充满信心的朝着国际化信息产业领域，与朋友们携手共创信息产业的辉煌!!!\n\n\n";
			SendDataString(msg);
		}
	}
		
/************************************************************************************************/
	private void SendDataByte(byte[] data){
		wfComm.sndByte(data);
	}
	
	private void SendDataString(String data){
		wfComm.sendMsg(data, "GBK");
	}
/************************************************************************************************/	
	/* 
	 * 生成QR图 
	 */
    private void createImage() {
	        try {
	            // 需要引入zxing包
	            QRCodeWriter writer = new QRCodeWriter();

	            String text = edtContext.getText().toString();

	         //   Log.i(TAG, "生成的文本：" + text);
	            if (text == null || "".equals(text) || text.length() < 1) {
	            	Toast.makeText(this, getText(R.string.empty), Toast.LENGTH_SHORT).show();
	            	return;
	            }

	            // 把输入的文本转为二维码
	            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
	                    QR_WIDTH, QR_HEIGHT);

	            System.out.println("w:" + martix.getWidth() + "h:"
	                    + martix.getHeight());

	            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
	            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
	            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
	                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
	            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
	            for (int y = 0; y < QR_HEIGHT; y++) {
	                for (int x = 0; x < QR_WIDTH; x++) {
	                    if (bitMatrix.get(x, y)) {
	                        pixels[y * QR_WIDTH + x] = 0xff000000;
	                    } else {
	                        pixels[y * QR_WIDTH + x] = 0xffffffff;
	                    }

	                }
	            }

	            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
	                    Bitmap.Config.ARGB_8888);

	            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
	            imageViewPicture.setImageBitmap(bitmap);
	            
	            byte[] data = PrintPicture.POS_PrintBMP(bitmap, 384, 0);
	            wfComm.sndByte(data);
	        } catch (WriterException e) {
	            e.printStackTrace();
	        }
	    }
//************************************************************************************************//
  	/*
  	 * 调用系统相机
  	 */
  	private void dispatchTakePictureIntent(int actionCode) {
  	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  	    startActivityForResult(takePictureIntent, actionCode);
  	}
  	
  	private void handleSmallCameraPhoto(Intent intent) {
  	    Bundle extras = intent.getExtras();
  	    Bitmap mImageBitmap = (Bitmap) extras.get("data");
  	    imageViewPicture.setImageBitmap(mImageBitmap);
  	}
/****************************************************************************************************/
	 /**
	 * 加载assets文件资源
	 */
	private Bitmap getImageFromAssetsFile(String fileName) {
			Bitmap image = null;
			AssetManager am = getResources().getAssets();
			try {
				InputStream is = am.open(fileName);
				image = BitmapFactory.decodeStream(is);
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return image;

		}
}



