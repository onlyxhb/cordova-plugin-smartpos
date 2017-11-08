package com.example.plugin;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.start.smartpos.aidl.ServiceProvider;
import com.start.smartpos.aidl.constants.OperationResult;
import com.start.smartpos.aidl.device.cardreader.CardInformation;
import com.start.smartpos.aidl.device.cardreader.CardReaderListener;
import com.start.smartpos.aidl.device.cardreader.CardReaderProvider;
import com.start.smartpos.aidl.device.cardreader.CpuCardProvider;
import com.start.smartpos.aidl.device.cardreader.RFCardProvider;
import com.start.smartpos.aidl.device.emv.EmvAidParam;
import com.start.smartpos.aidl.device.emv.EmvCapk;
import com.start.smartpos.aidl.device.emv.EmvChannel;
import com.start.smartpos.aidl.device.emv.EmvConstant;
import com.start.smartpos.aidl.device.emv.EmvIccOnlineData;
import com.start.smartpos.aidl.device.emv.EmvMsgDisplay;
import com.start.smartpos.aidl.device.emv.EmvProvider;
import com.start.smartpos.aidl.device.emv.EmvTermConfig;
import com.start.smartpos.aidl.device.emv.EmvTransData;
import com.start.smartpos.aidl.device.emv.EmvTransListener;
import com.start.smartpos.aidl.device.emv.EmvTransResult;
import com.start.smartpos.aidl.device.pinpad.KeyInfo;
import com.start.smartpos.aidl.device.pinpad.PinPadConfig;
import com.start.smartpos.aidl.device.pinpad.PinPadConstant;
import com.start.smartpos.aidl.device.pinpad.PinPadListener;
import com.start.smartpos.aidl.device.pinpad.PinPadOperationResult;
import com.start.smartpos.aidl.device.pinpad.PinPadProvider;
import com.start.smartpos.aidl.device.printer.PrinterFormat;
import com.start.smartpos.aidl.device.printer.PrinterProvider;
import com.start.smartpos.aidl.device.psam.PSAMProvider;
import com.start.smartpos.aidl.device.scancode.ScanCodeListener;
import com.start.smartpos.aidl.device.scancode.ScanCodeOperationResult;
import com.start.smartpos.aidl.device.scancode.ScanCodeProvider;
import com.start.smartpos.aidl.device.system.DriverInformations;
import com.start.smartpos.aidl.device.system.SystemProvider;
import com.start.smartposdemo.util.Order;
import com.start.smartposdemo.util.Convert;
import com.start.smartposdemo.util.HexUtil;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SmartPos extends CordovaPlugin {
    private Activity activity ;
    private ServiceProvider mAIDLService = null;
    private CardReaderProvider cardReaderProvider;
    private PrinterProvider printerProvider;
    private PinPadProvider pinPadProvider;
    private SystemProvider systemProvider;
    private ScanCodeProvider scanCodeProvider;
    private EmvProvider emvProvider;
    private int operatorResult = OperationResult.ERROR;
    private ServiceConnection conn=null;//得到连接服务
    private CallbackContext callbackContext=null;//返回的回调
    private String msg="";
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        activity = cordova.getActivity();
    }

    /**
     String action：一个类里面可以提供多个功能，action就是指名了要调用哪个功能。
     CordovaArgs args：web以json的数据格式传递给Android native，CordovaArgs 是对JSONArray 的一个封装。
     CallbackContext callbackContext：这个是回调给web，有success和error两种回调方法。
     */
    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callback) throws JSONException {
        callbackContext=callback;
        String param = args.getString(0);
        if ("isbind".equals(action)) {
            boolean isbind=mAIDLService != null?true:false;
            callbackContext.success("是否绑定服务:"+isbind);
            return true;
        }else if ("bind".equals(action)) {
           bindService();
           return true;
       }else  if("unbind".equals(action)){
            try{
                activity.unbindService(conn);
                callbackContext.success("解绑成功");
                return true;
            }catch (RuntimeException e){
                callbackContext.error("解绑失败,已经解绑或者未连接");
                return false;
            }
        }else  if("searchCard".equals(action)){
           searchCard();
           return true;
        }else if("print".equals(action)){
             print(param);
             return true;
        }else if("pinpad".equals(action)){
            pinpad();
             return true;
        }else if("systemInterface".equals(action)){
            systemInterface();
           return true;
       }else if("emv".equals(action)){
            emv();
            return true;
        }else if("pin".equals(action)){
            pin();
            return true;
        }else if("scan".equals(action)){
            scan();
            return true;
        }else if("stopScan".equals(action)){
            stopScan();
            return true;
        }else if("ICApdu".equals(action)){
            ICApdu();
            return true;
        }else if("RFApdu".equals(action)){
            RFApdu();
            return true;
        }else if("PsamApdu".equals(action)){
            PsamApdu();
            return true;
        }else{
            callbackContext.error("调用方法名有误!");
            Toast.makeText(cordova.getActivity(),"调用方法名有误!",Toast.LENGTH_LONG).show();;
            return false;
        }
    }

    //寻卡
    public void searchCard(){
        if(mAIDLService != null) {
            msg="";
            try {
                cardReaderProvider = mAIDLService.getCardReaderProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }

            try {
                Toast.makeText(cordova.getActivity(),"寻卡中!",Toast.LENGTH_LONG).show();;
                operatorResult = cardReaderProvider.readCard(60, (byte) (CardInformation.CARD_MAG|CardInformation.CARD_INSERT|CardInformation.CARD_CLSS),
                        new CardReaderListener.Stub() {

                            @Override
                            public void onReadCardResult(int i, CardInformation cardInformation) throws RemoteException {
                                msg="";
                                if(OperationResult.SUCCESS == i){
                                    int type = cardInformation.getCardType();
                                    if(CardInformation.CARD_MAG != type) {
                                        showMsg("card type:" + type);
                                    }else{
                                        showMsg("cardno:"+cardInformation.getCardNo()
                                                + ",track1:" + cardInformation.getTrack1()
                                                + ",track2:" + cardInformation.getTrack2()
                                                + ",track3:" + cardInformation.getTrack3()
                                                + ",extDate:" + cardInformation.getExpDate());
                                    }

                                }else {
                                    showMsg("read err :"+i);
                                }
                            }
                        });
                showMsg("readCard result:"+operatorResult);
                callbackContext.success(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
        }
    }
    //打印
    public void print(String str){
        if(mAIDLService != null){
            msg="";
            try {
                printerProvider = mAIDLService.getPrinterProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
            try {
                operatorResult = printerProvider.prepareForPrint();
                showMsg("prepareForPrint result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            printBill(str);
            try {
                operatorResult = printerProvider.endPrint();
                showMsg("endPrint result:"+operatorResult);
                callbackContext.success(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    public void printBill(String str) {
        Gson gson = new Gson();
        Order data=gson.fromJson(str,Order.class);

        PrinterFormat format = new PrinterFormat();
        format.setParameter("size", PrinterFormat.FONT_SIZE_LARGE);
        format.setParameter("align", PrinterFormat.ALIGN_CENTER);
        format.setParameter("bold", PrinterFormat.BOLD_DISABLE);

        String amount = "100.00";

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy");//0226103522
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String dateString = formatter.format(curDate);
            String localTime = "101234";//交易时间 例如103522
            String localDate = "20170403";//交易日期 例如0226
            SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMddHHmmss");
            String s = dateString + localDate + localTime;
            Date now = null;
            try {
                now = formatter2.parse(s);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String dateString_new = formatter1.format(now);
            format.setParameter("double-wh", PrinterFormat.NORMAL_WIDTH_HEIGHT);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(activity.getAssets().open("print.bmp"), null, opts);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //打印顶部logo
            printBitmap(format, bitmap);
            //换两行
            printText(format, "");
            printText(format, "");

            format.setParameter("align", PrinterFormat.ALIGN_LEFT);
            printText(format, "销售单号:"+data.getSaleOrder());
            printText(format, "销售客户:"+data.getCustomeName() );
            printText(format, "定金金额:" + data.getAdvanceMoney());
            printText(format, "订单日期:"+data.getSaleDate());
            printText(format, "订单交期:"+data.getDeliverDate());
            printText(format, "销售数量:"+data.getSaleAmount());
            printText(format, "销售金额:"+data.getSaleMoney());
            printText(format, "经手人:"+data.getSponerName());
            printText(format, " ");
            printText(format, "---------销售货品信息---------");
            for(int i=0;i<data.getGoodses().size();i++){
                Order.Goods good=data.getGoodses().get(i);
                printText(format, "名称:"+good.getName());
                printText(format, "颜色:"+good.getColor()+"  尺码:"+good.getSize());
                printText(format, "批发价:"+good.getZmoney()+"  数量:"+good.getAmount());
                printText(format, "折扣:"+good.getDiscount()+"  总金额:"+good.getWholesalePrice());
                printText(format, "------------------------------");
            }
            printText(format, "打印时间:"+data.getPrintTime());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    //密码键盘
    public void pinpad(){
        if(mAIDLService != null) {
            msg="";
            try {
                pinPadProvider = mAIDLService.getPinPadProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
            String mk ="CE62D1F28DC50DFD98424413D67E4FA3";
            String ck = "74E7E744";		// 若无checkvalue，可传null

            // 灌装主密钥
            KeyInfo tmk = new KeyInfo(PinPadConstant.KEY_TYPE_TMK, false, 0, 0, Convert.str2Bcd(mk), 16, Convert.str2Bcd(ck), 4);
            try {
                operatorResult = pinPadProvider.loadKey(tmk);
                showMsg("灌装主密钥 result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // 灌装工作密钥
            String encrypik ="2D25290B4C47B6EBD247D9C8D8499399";
            String pikck = "0A89AB77";

            String encrymak ="D6DDA3BED199AB7454D34EFADC5B6704";
            String makck = "1EAF523b";

            String encrytdk ="535C26152FE4F52D26B64CE60C3A2EFE";
            String tdkck = "B445E3AA";

            KeyInfo pik = new KeyInfo(PinPadConstant.KEY_TYPE_PIK, true, 0, 0, Convert.str2Bcd(encrypik), 16, Convert.str2Bcd(pikck), 4);
            try {
                operatorResult = pinPadProvider.loadKey(pik);
                showMsg("灌装pik result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            KeyInfo mak = new KeyInfo(PinPadConstant.KEY_TYPE_MAK, true, 0, 0, Convert.str2Bcd(encrymak), 16, Convert.str2Bcd(makck), 4);
            try {
                operatorResult = pinPadProvider.loadKey(mak);
                showMsg("灌装mak result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            KeyInfo tdk = new KeyInfo(PinPadConstant.KEY_TYPE_TDK, true, 0, 0, Convert.str2Bcd(encrytdk), 16, Convert.str2Bcd(tdkck), 4);
            try {
                operatorResult = pinPadProvider.loadKey(tdk);
                showMsg("灌装tdk result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            //计算MAC
            String macdata ="0200702406C020C09A1113622800010000100000000000000000010000343010051000010012346228000100001D301020100000000000003934303137303031333031333238313438313630303032313536ED8677B84EF5C989260000000000000001459F2608F879A548590795E39F2701809F101307000103A00002010A010000050931BEB54E529F3704B9B7D4D79F360202FE950500800460009A031507219C01009F02060000000000015F2A02015682027D009F1A0201569F03060000000000009F330360E1C89F34030203009F3501229F1E0838333230494343008408A0000003330101019F090200209F410400000003001422000026000601";
            byte[] mac = null;
            try
            {
                mac = pinPadProvider.calcMac(0, PinPadConstant.ALG_MAC_METHOD_X919_X00, Convert.str2Bcd(macdata));
                if(mac != null) {
                    showMsg("计算的mac:" + Convert.bcd2Str(mac));
                }else {
                    showMsg("mac计算失败");
                }

            }catch(RemoteException e)
            {
                e.printStackTrace();
                showMsg("服务异常");
            }
            //数据加密
            String datatext = "887633367|20141103898760077000012|8876.23|6225211110578837|6225288880578837=9870120012342020000|||06c5c087668f9978||||||";
            byte[] enrcdata = null;
            try
            {
                enrcdata = pinPadProvider.encrypt(0, Convert.str2Bcd(string2HexString(datatext)));
                if(enrcdata != null) {
                    showMsg("加密结果：" + Convert.bcd2Str(enrcdata));
                }else {
                    showMsg("加密失败");
                }

            }catch(RemoteException e)
            {
                showMsg("服务异常");
            }
            // 输入联机PIN
            PinPadConfig config = new PinPadConfig();
            config.setTimeout(60);
            config.setSupportPinLen("4,6");
            config.setSymmetricAlgorithmType(PinPadConstant.ALG_SYMMETRICAL_DES);
            config.setSupportKeyTone(true);
            config.setSupportEchoPassword(true);
            try {
                operatorResult = pinPadProvider.config(config);
                showMsg("pinpad config result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                operatorResult = pinPadProvider.inputOnlinePin("6225885918926825", 0, PinPadConstant.ALG_PIN_MODE_ONLINE, new PinPadListener.Stub() {
                    @Override
                    public void pressKeyEvent(int len) throws RemoteException {
                        Log.d("abc", "input pin len:" + len);
                    }
                    @Override
                    public void handleResult(PinPadOperationResult pinPadOperationResult) throws RemoteException {
                        if(pinPadOperationResult.getResult() == OperationResult.SUCCESS){
                            showMsg("get online pin ok, PIN:" + Convert.bcd2Str(pinPadOperationResult.getPinblock()));
                        }else{
                            showMsg("err online result code:" + pinPadOperationResult.getResult());
                        }
                    }
                });
                showMsg("inputOnlinePin result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        callbackContext.success(msg);
    }
    //系统接口
    public void systemInterface(){
        if(mAIDLService != null) {
            msg="";
            try {
                systemProvider = mAIDLService.getSystemProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }

            try {
                String sn=systemProvider.getSerialNo();
                showMsg("sn:" + sn);
            } catch (RemoteException e) {
                e.printStackTrace();
                showMsg("get sn err:" + e.toString());
            }


            // 显示信息控制------------------------------------
            try {
                List<DriverInformations> list = systemProvider.getDriverInformation();
                String driverversion = "";
                String securityDriverVersion = "";

                if(list != null) {
                    for (DriverInformations info : list) {
                        if ("middleware".equals(info.getId())) {
                            driverversion = info.getVersion();
                        } else if ("securityfirmware".equals(info.getId())) {
                            securityDriverVersion = info.getVersion();
                        }
                    }
                }

                StringBuilder build = new StringBuilder();
                build.append("AndroidKernelVersion: " + systemProvider.getAndroidKernelVersion() + "\n");
                build.append("AndroidOsVersion: " + systemProvider.getAndroidOsVersion() + "\n");
                build.append("CurSdkVersion: " + systemProvider.getCurSdkVersion() + "\n");
                build.append("DriverVersion: " + driverversion + "\n");
                build.append("HardWireVersion: " + systemProvider.getHardWireVersion() + "\n");
                build.append("IMEI: " + systemProvider.getIMEI() + "\n");
                build.append("Manufacture: " + systemProvider.getManufacture() + "\n");
                build.append("Model: " + systemProvider.getModel() + "\n");
                build.append("RomVersion: " + systemProvider.getRomVersion() + "\n");
                build.append("SecurityDriverVersion: " + securityDriverVersion + "\n");
                build.append("SerialNo: " + systemProvider.getSerialNo() + "\n");
                build.append("StoragePath: " + systemProvider.getStoragePath() + "\n");

                showMsg(build.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // 导航栏控制---------------------------------------
            showMsg("隐藏导航栏");
            try {
                operatorResult = systemProvider.controlNavigationBar(false);
                showMsg("controlNavigationBar false result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                operatorResult = systemProvider.controlNavigationBar(true);
                showMsg("controlNavigationBar true result:"+operatorResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            showMsg("显示导航栏");
            callbackContext.success(msg);
        }
    }
    //EMV
    public void emv(){
        if(mAIDLService != null) {
            msg="";
            try {
                emvProvider = mAIDLService.getEmvProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }

            try {
                cardReaderProvider = mAIDLService.getCardReaderProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }

            // 导入公钥和AID参数——仅在有数据更新时设置一次
            SetAidAndCapk(emvProvider);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        operatorResult = cardReaderProvider.readCard(60, (byte) (CardInformation.CARD_MAG|CardInformation.CARD_INSERT|CardInformation.CARD_CLSS),
                                new CardReaderListener.Stub() {

                                    @Override
                                    public void onReadCardResult(int i, final CardInformation cardInformation) throws RemoteException {
                                        if(OperationResult.SUCCESS == i){
                                            int type = cardInformation.getCardType();
                                            if(CardInformation.CARD_MAG != type) {
                                                int flag = (CardInformation.CARD_CLSS == type) ? 0 : 1;
                                                EmvTransResult res = emvProvider.process(getEmvTransData(flag), emvTransListener);
                                                byte[] check = getfield55Data("9f1095");
                                                Log.i("check", "tlv:"+Convert.bcd2Str(check));
                                                showMsg("ic卡交易结果:"+res);
                                                Log.i("test", "ic卡交易结果:"+res);
                                                switch(res){
                                                    case EMV_L2_OFFLINE_DENY:
                                                        showMsg("交易脱机拒绝:"+res);
                                                        break;
                                                    case EMV_L2_ONLINE_DENY:
                                                        showMsg("交易联机拒绝:"+res);
                                                        break;
                                                    case EMV_L2_OFFLINE_ACCEPT:
                                                        showMsg("交易脱机接受:"+res);
                                                        break;
                                                    case EMV_L2_ONLINE_ACCEPT:
                                                        showMsg("交易联机接受:"+res);
                                                        break;
                                                    case EMV_L2_ONLINE_FAILD:
                                                        showMsg("交易联机失败:"+res);
                                                        break;
                                                    case EMV_L2_FALLBACK:
                                                    case EMV_L2_USE_ANOTHER_INTERFACE:
                                                        showMsg("请使用其他交易方式:"+res);
                                                        break;
                                                    default:
                                                        showMsg("交易终止:"+res);
                                                        break;
                                                }
                                            }else{
                                                showMsg("cardno:"+cardInformation.getCardNo()
                                                        + ",track1:" + cardInformation.getTrack1()
                                                        + ",track2:" + cardInformation.getTrack2()
                                                        + ",track3:" + cardInformation.getTrack3()
                                                        + ",extDate:" + cardInformation.getExpDate());
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            Thread.sleep(300);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        encryDataAndInputPin(cardInformation.getCardNo(), HexUtil.hexString2String(cardInformation.getTrack2()));
                                                    }
                                                }).start();

                                            }

                                        }else {
                                            showMsg("read err :"+i);
                                        }
                                    }
                                });
                        showMsg("readCard result:"+operatorResult);
                        callbackContext.success(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    //PIN
    public void pin(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                encryDataAndInputPin("6225885918926825",
                        "887633367|20141103898760077000012|8876.23|6225211110578837|6225288880578837=9870120012342020000|||06c5c087668f9978||||||");
            }
        }).start();
    }
    //扫码接口
    public void scan(){
        if(mAIDLService != null) {
            msg="";
            try {
                scanCodeProvider = mAIDLService.getScanCodeProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
            try {
                operatorResult = scanCodeProvider.startScan(new ScanCodeListener.Stub() {
                    @Override
                    public void handleResult(final ScanCodeOperationResult scanCodeOperationResult) throws RemoteException {
                        showMsg("res:"+scanCodeOperationResult.getResult()
                                + "barcode:"+scanCodeOperationResult.getCode());
                    }
                }, 5);
                showMsg("startScan result:"+operatorResult);
                callbackContext.success(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    //停止扫码
    public void stopScan(){
        if(mAIDLService != null) {
            msg="";
            try {
                scanCodeProvider = mAIDLService.getScanCodeProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }

            try {
                operatorResult = scanCodeProvider.stopScan();
                showMsg("stopScan result:"+operatorResult);
                callbackContext.success(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    //IC卡APDU
    public void ICApdu(){
        if(mAIDLService != null) {
            msg="";
            CpuCardProvider cpuCardProvider = null;

            try {
                cpuCardProvider = mAIDLService.getCpuCardProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }

            final CpuCardProvider provider = cpuCardProvider;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!provider.connect()){
                            showMsg("卡片未连接!");
                            callbackContext.success(msg);
                            return;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    String cmd = "00A404000E315041592E5359532E444446303100";
                    byte[] recv = null;
                    try {
                        recv = provider.command(Convert.str2Bcd(cmd));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if(recv != null){
                        showMsg("recv1:"+Convert.bcd2Str(recv));
                    }else {
                        showMsg("recv1 is null");
                    }

                    String cmd2 = "00B2010C00";
                    try {
                        recv = provider.command(Convert.str2Bcd(cmd2));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if(recv != null){
                        showMsg("recv2:"+Convert.bcd2Str(recv));
                    }else {
                        showMsg("recv2 is null");
                    }
                    try {
                        provider.disconnect();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    callbackContext.success(msg);
                }
            }).start();
        }
    }
    //RF卡APDU
    public void RFApdu(){
        if(mAIDLService != null) {
            msg="";
            RFCardProvider rfCardProvider = null;

            try {
                rfCardProvider = mAIDLService.getRFCardProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }

            final RFCardProvider provider = rfCardProvider;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!provider.connect()){
                            showMsg("卡片未连接!");
                            callbackContext.success(msg);
                            return;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    String cmd = "00A404000E325041592E5359532E444446303100";
                    byte[] recv = null;
                    try {
                        recv = provider.command(Convert.str2Bcd(cmd));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if(recv != null){
                        showMsg("recv1:"+Convert.bcd2Str(recv));
                    }else {
                        showMsg("recv1 is null");
                    }

                    String cmd2 = "00A4040008A00000033301010100";
                    try {
                        recv = provider.command(Convert.str2Bcd(cmd2));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if(recv != null){
                        showMsg("recv2:"+Convert.bcd2Str(recv));
                    }else {
                        showMsg("recv2 is null");
                    }
                    try {
                        provider.disconnect();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    callbackContext.success(msg);
                }
            }).start();
        }
    }
    //PSAM卡APDU
    public void PsamApdu(){
        if(mAIDLService != null) {
            msg="";
            PSAMProvider psamProvider = null;
            try {
                psamProvider = mAIDLService.getPSAMProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
            final PSAMProvider provider = psamProvider;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    comWithPsam(provider, 0);
                    comWithPsam(provider, 1);
                    callbackContext.success(msg);
                }
            }).start();
        }
    }

    //绑定服务
    public void bindService() {
        getConn();
        Intent intent = new Intent();
        intent.setAction("com.start.smartpos.AIDL_SERVICE").setPackage("com.start.smartpos");
        boolean isSuccess = activity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if(isSuccess){
            callbackContext.success("连接服务成功！");
        }else{
            callbackContext.error("连接服务失败");
            Toast.makeText(activity.getApplicationContext(),"连接服务失败！",Toast.LENGTH_SHORT).show();
        }
    }

    //得到服务连接
    public void getConn(){
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                callbackContext.success("AIDL服务绑定成功！");
                Toast.makeText(activity.getApplicationContext(),"AIDL服务绑定成功！",Toast.LENGTH_SHORT).show();;
                mAIDLService = ServiceProvider.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                callbackContext.error("AIDL服务异常断开："+name.getPackageName());
                Log.e("rain", "AIDL服务异常断开："+name.getPackageName());
                mAIDLService = null;
                try {
                    Thread.sleep(5*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bindService();
            }
        };
    }

    //返回的字符串拼接
    private void showMsg(final String tip){
        msg+=tip+"</br>";
    }
    //打印相关
    private boolean printText(PrinterFormat format, String text) throws RemoteException{
        operatorResult = printerProvider.printText(format, text);
        if(OperationResult.SUCCESS == operatorResult){
            return true;
        }
        showMsg("printText err:"+operatorResult+"\r\n");
        return false;
    }
    private boolean printBitmap(PrinterFormat format, Bitmap bitmap) throws RemoteException{
        operatorResult = printerProvider.printBitmap(format, bitmap);
        if(OperationResult.SUCCESS == operatorResult){
            return true;
        }
        showMsg("printBitmap err:"+operatorResult);

        return false;
    }
    private static String string2HexString(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }
    //EMV相关
    // 导入AID和公钥,
    private void SetAidAndCapk(EmvProvider provider) {
        // AID和公钥只需导入一次，不必每次交易都设置。

        List<EmvAidParam> list;
        EmvTermConfig config = new EmvTermConfig();
        config.setBypassPin(0);
        try {
            // 配置终端参数
            provider.clearCapk();
            provider.clearTermAid();
            provider.setTermConfig(config);
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
        try {
            list = provider.getAidParam();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
        if(list.size() > 0){
            return;
        }

        EmvAidParam aidParam1 = new EmvAidParam();
        aidParam1.setAID("A000000333010101");// 9f06
        aidParam1.setSelFlag(0);// df01
        aidParam1.setVersion("0020");// 9f08
        aidParam1.setTacDefualt("FC78F4F8F0");// df11
        aidParam1.setTacOnline("FC78F4F8F0");// df12
        aidParam1.setTacDenial("0010000000");// df13
        aidParam1.setFloorlimitCheck(10000000);// 9f1b
        aidParam1.setThreshold(00000000);// df15
        aidParam1.setMaxTargetPer(20);// df16
        aidParam1.setTargetPer(10);// df17
        aidParam1.setdDOL("9F3704");// df14
        aidParam1.setOnlinePin(01);// df18
        aidParam1.setECTTLVal(10000000);// 9f7b
        aidParam1.setRdClssFLmt(10000000);// df19
        aidParam1.setRdClssTxnLmt(999999999);// df20
        aidParam1.setRdCVMLmt(999999999);// df21
        aidParam1.setECTTLFlg(1);
        aidParam1.setRdCVMLmtFlg(1);
        aidParam1.setRdClssTxnLmtFlg(1);
        aidParam1.setRdClssFLmtFlg(1);
        aidParam1.setFloorlimitCheck(1);
        try {
            operatorResult = emvProvider.setAidParam(aidParam1);
            showMsg("setAidParam1 result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        aidParam1.setAID("A000000333010102");// 9f06
        aidParam1.setSelFlag(01);// df01
        try {
            operatorResult = emvProvider.setAidParam(aidParam1);
            showMsg("setAidParam2 result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        aidParam1.setAID("A0000003330101021122334455667788");// 9f06
        aidParam1.setSelFlag(01);// df01
        try {
            operatorResult = emvProvider.setAidParam(aidParam1);
            showMsg("setAidParam2 result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        aidParam1.setAID("A000000333010103");// 9f06
        aidParam1.setSelFlag(01);// df01
        try {
            operatorResult = emvProvider.setAidParam(aidParam1);
            showMsg("setAidParam3 result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        aidParam1.setAID("A000000333010106");// 9f06
        aidParam1.setSelFlag(01);// df01
        try {
            operatorResult = emvProvider.setAidParam(aidParam1);
            showMsg("setAidParam4 result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        EmvCapk pubKeyParam1 = new EmvCapk();
        pubKeyParam1.setRID("A000000333");// 9F06
        pubKeyParam1.setKeyID(04);// 9F22
        pubKeyParam1.setExpDate("20321230");// DF05
        pubKeyParam1.setHashInd(01);// DF06
        pubKeyParam1.setArithInd(01);// DF07
        pubKeyParam1.setModul("bc853e6b5365e89e7ee9317c94b02d0abb0dbd91c05a224a2554aa29ed9fcb9d86eb9ccbb322a57811f86188aac7351c72bd9ef196c5a01acef7a4eb0d2ad63d9e6ac2e7836547cb1595c68bcbafd0f6728760f3a7ca7b97301b7e0220184efc4f653008d93ce098c0d93b45201096d1adff4cf1f9fc02af759da27cd6dfd6d789b099f16f378b6100334e63f3d35f3251a5ec78693731f5233519cdb380f5ab8c0f02728e91d469abd0eae0d93b1cc66ce127b29c7d77441a49d09fca5d6d9762fc74c31bb506c8bae3c79ad6c2578775b95956b5370d1d0519e37906b384736233251e8f09ad79dfbe2c6abfadac8e4d8624318c27daf1");// DF02
        pubKeyParam1.setExponent("03");// DF04
        pubKeyParam1.setCheckSum("f527081cf371dd7e1fd4fa414a665036e0f5e6e5");// DF03
        try {
            operatorResult = emvProvider.setCapk(pubKeyParam1);
            showMsg("setCapk1 result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        pubKeyParam1.setRID("A000000333");// 9F06
        pubKeyParam1.setKeyID(11);// 9F22
        pubKeyParam1.setExpDate("20321230");// DF05
        pubKeyParam1.setHashInd(01);// DF06
        pubKeyParam1.setArithInd(01);// DF07
        pubKeyParam1.setModul("cf9fdf46b356378e9af311b0f981b21a1f22f250fb11f55c958709e3c7241918293483289eae688a094c02c344e2999f315a72841f489e24b1ba0056cfab3b479d0e826452375dcdbb67e97ec2aa66f4601d774feaef775accc621bfeb65fb0053fc5f392aa5e1d4c41a4de9ffdfdf1327c4bb874f1f63a599ee3902fe95e729fd78d4234dc7e6cf1ababaa3f6db29b7f05d1d901d2e76a606a8cbffffecbd918fa2d278bdb43b0434f5d45134be1c2781d157d501ff43e5f1c470967cd57ce53b64d82974c8275937c5d8502a1252a8a5d6088a259b694f98648d9af2cb0efd9d943c69f896d49fa39702162acb5af29b90bade005bc157");// DF02
        pubKeyParam1.setExponent("03");// DF04
        pubKeyParam1.setCheckSum("bd331f9996a490b33c13441066a09ad3feb5f66c");// DF03
        try {
            operatorResult = emvProvider.setCapk(pubKeyParam1);
            showMsg("setCapk2 result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        pubKeyParam1.setRID("A000000333");// 9F06
        pubKeyParam1.setKeyID(03);// 9F22
        pubKeyParam1.setExpDate("20321230");// DF05
        pubKeyParam1.setHashInd(01);// DF06
        pubKeyParam1.setArithInd(01);// DF07
        pubKeyParam1.setModul("b0627dee87864f9c18c13b9a1f025448bf13c58380c91f4ceba9f9bcb214ff8414e9b59d6aba10f941c7331768f47b2127907d857fa39aaf8ce02045dd01619d689ee731c551159be7eb2d51a372ff56b556e5cb2fde36e23073a44ca215d6c26ca68847b388e39520e0026e62294b557d6470440ca0aefc9438c923aec9b2098d6d3a1af5e8b1de36f4b53040109d89b77cafaf70c26c601abdf59eec0fdc8a99089140cd2e817e335175b03b7aa33d");// DF02
        pubKeyParam1.setExponent("03");// DF04
        pubKeyParam1.setCheckSum("87f0cd7c0e86f38f89a66f8c47071a8b88586f26");// DF03
        try {
            operatorResult = emvProvider.setCapk(pubKeyParam1);
            showMsg("setCapk3 result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public EmvTransData getEmvTransData(int flag){
        EmvTransData transData = new EmvTransData();

        transData.setAmount("000000000001");
        transData.setTag9Value((byte) 0x00);
        transData.setTransDate("160510");
        transData.setTransTime("123208");
        transData.setTransNo("0001");
        transData.setSupportSM(false);
        transData.setCardAuth(true);
        transData.setForceOnline(false);
        transData.setSupportEC(true);
        transData.setSupportCvm(true);
        if(flag == 0){
            transData.setFlow(EmvConstant.FLOW.QPBOC);
            transData.setChannel(EmvChannel.PICC);
        }
        else
        {
            transData.setFlow(EmvConstant.FLOW.PBOC);
            transData.setChannel(EmvChannel.ICC);
        }
        transData.setKeyType(6);//
        transData.setKeyIndex(0);
        transData.setEncryMode(0);
        transData.setMinLen(0);
        transData.setMaxLen(6);
        transData.setTimeout(30);
        transData.setSupportEchoPassword(true);
        transData.setSupportKeyTone(true);
        return transData;
    }
    private EmvTransListener.Stub emvTransListener = new EmvTransListener.Stub() {
        @Override
        public int onConfirmCardNo(final String s) throws RemoteException {
            // TODO:用户确认卡号,UI线程处理界面交互

            //0：确认，其他取消
            return 0;
        }

        @Override
        public EmvIccOnlineData onOnlineProc(byte[] bytes, byte[] bytes1) throws RemoteException {
            // TODO:联机通讯，收发数据
            byte[] field55 = null;
//            String tags = "9f269f279f109f379f36959a9c9f025f2a829f1a9f1e849f099f419f638a9f74";
            String tags = "9F269F279F109F379F36959A9C9F025F2A829F1A9F039F339F349F359F1E849F099F419F63";

            // pin
            if(bytes != null) {
                Log.i("pin", "pin:" + Convert.bcd2Str(bytes));
            }else{
                Log.i("pin", "pin is null");
            }

            field55 = getfield55Data(tags);

            if(field55 != null) {
                Log.i("test", "55field" + Convert.bcd2Str(field55));
            }else {
                Log.i("test", "55field is null");
            }

            //根据交易类型，组包（8583报文）
            //TODO:联机发送8583包至平台
//            socketConnect();
//TODO:发送接收数据
            // 上送55域数据field55
//            socketSend(field55);
            // field55读取联机响应的55域数据
//            socketRecv(field55);

            //TODO:接收平台应答数据，解析8583报文
            EmvIccOnlineData onOnlineRet = new EmvIccOnlineData();
            //平台有应答则设置为0，无应答设置为 -1；

            onOnlineRet.setOnlineResult(0);
            //应答报文中的55域数据，需要返回给emv内核（这里假设应答报文返回的数据是9f36）

            byte[] field55Recv = new byte[]{(byte)0x9F, 0x36,0x02,0x02,0x5A};

            /*
                EmvIccOnlineData.field55:接收平台返回的IC卡数据（tlv格式），如果没有可设置为null
                EmvIccOnlineData.RespCode 平台返回的交易结果
                EmvIccOnlineData.AIRespcode平台返回的标签为8a的值，没有可设置为null
                EmvIccOnlineData.OnlineResult 联机流程是否成功，能成功收发数据填0，其他值是失败。
             */
            onOnlineRet.setfield55(field55Recv);
            //应答报文中的39域数据
            onOnlineRet.setRespCode("00");
            //应答报文中的38域数据（没有38域填"000000"即可）
            onOnlineRet.setAIRespcode("043820");
            onOnlineRet.setOnlineResult(0);

            // TODO:断开链接
//            socketDisconnect();
            return onOnlineRet;
        }

        @Override
        public int onCardHolderPwd(int i) throws RemoteException {
            return 0;
        }

        @Override
        public int onShowMessage(final EmvMsgDisplay msg) throws RemoteException {
            if(3 == msg.getType()) {
                // TODO:实时反馈密码输入的位数，在UI线程处理
                Log.v("abc", "Psw len:"+msg.getContent());
                return 0;
            }else {
                // TODO:回调显示提示信息，在UI线程处理

                //文本类：返回 0；
                //选择类：0-确定 1-取消；
                //索引框：N：实际索引号；
                return 0;
            }
        }

        @Override
        public void onSendKey(int i) throws RemoteException {

        }
    };
    private byte[] getfield55Data(String tags){
        byte[] field55 = null;

        try {
            field55 = emvProvider.getTlvArray(tags);
            if(null == field55){
                showMsg("getfield55 failed");
            }
        } catch (RemoteException e) {
            Log.e("abc", e.getMessage());
        }
        return field55;
    }
    private void encryDataAndInputPin(String card, String track2){
        if(mAIDLService != null) {
            try {
                pinPadProvider = mAIDLService.getPinPadProvider();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }else {
            return;
        }
//        String datatext = "887633367|20141103898760077000012|8876.23|6225211110578837|6225288880578837=9870120012342020000|||06c5c087668f9978||||||";
        int len = track2.length();
        String defaultString = "FFFFFFFF";
        StringBuilder builder = new StringBuilder();
        builder.append(track2);
        builder.append(defaultString, 0, 8-(len%8));
        String datatext = builder.toString();
        byte[] enrcdata = null;
        byte[] org = Convert.str2Bcd(string2HexString(datatext));
        try
        {
            enrcdata = pinPadProvider.encrypt(0, org);
            showMsg("加密结果："+Convert.bcd2Str(enrcdata));

        }catch(RemoteException e)
        {
            showMsg("加密数据失败:" +e.toString());
        }


        // 输入联机PIN
        PinPadConfig config = new PinPadConfig();
        config.setTimeout(60);
        config.setSupportPinLen("4,6");
        config.setSymmetricAlgorithmType(PinPadConstant.ALG_SYMMETRICAL_DES);
        config.setSupportKeyTone(true);
        config.setSupportEchoPassword(true);
        try {
            operatorResult = pinPadProvider.config(config);
            showMsg("pinpad config result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            operatorResult = pinPadProvider.inputOnlinePin(card, 0, PinPadConstant.ALG_PIN_MODE_ONLINE, new PinPadListener.Stub() {

                @Override
                public void pressKeyEvent(int len) throws RemoteException {
                    Log.d("abc", "input pin len:" + len);
                }

                @Override
                public void handleResult(PinPadOperationResult pinPadOperationResult) throws RemoteException {
                    if(pinPadOperationResult.getResult() == OperationResult.SUCCESS){
//                            Log.d("abc", "get pin ok, PIN:" + Convert.bcd2Str(pinPadOperationResult.getPinblock()));
                        showMsg("get online pin ok, PIN:" + Convert.bcd2Str(pinPadOperationResult.getPinblock()));
                    }else{
//                            Log.d("abc", "err result code:" + pinPadOperationResult.getResult());
                        showMsg("err online result code:" + pinPadOperationResult.getResult());
                    }
                }
            });
            showMsg("inputOnlinePin result:"+operatorResult);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    //PsamApdu相关
    private void comWithPsam(PSAMProvider provider, int slot){
        showMsg("卡槽"+slot+"测试");
        try {
            if(!provider.connect(slot)){
                showMsg("卡片"+slot+"未连接");
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        String cmd = "00A40000023F00";
        byte[] recv = null;
        try {
            recv = provider.command(slot, Convert.str2Bcd(cmd));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(recv != null){
            showMsg("recv1:"+Convert.bcd2Str(recv));
        }else {
            showMsg("recv1 is null");
        }

        String cmd2 = "00B0950408";
        try {
            recv = provider.command(slot, Convert.str2Bcd(cmd2));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(recv != null){
            showMsg("recv2:"+Convert.bcd2Str(recv));
        }else {
            showMsg("recv2 is null");
        }
        try {
            provider.disconnect(slot);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
