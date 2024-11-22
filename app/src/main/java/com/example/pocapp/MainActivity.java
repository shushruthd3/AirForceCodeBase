package com.example.pocapp;

import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    EditText mEditValidate,mEditReg;
    final int REQUEST_CODE = 101;
    final int REQUEST_CODE_BT = 102;
    String imei;
    APIInterface apiInterface;
    Button mValidateBtn,mRegBtn;
    TextView textView_id;
    TextView textView_id_cot;
    TextView sap1,sap2;
    static TextView mandroid_id_crypt;
    String serial;
    String finger;
    String BuildVersion;
    String radioVersion;
    LinearLayout validateLayout, mRegisterLayout;
    TelephonyManager telephonyManager;
    CardView mcardview;
public static boolean isAlreadyRegistered = false;
    String hashedAfterConcat;
    private static final int PERMISSION_REQUEST_CODE = 200;
    RegisterUser userReg = new RegisterUser();
    String newConcatValue;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditValidate = (EditText) findViewById(R.id.url_edit1);
        mEditReg =(EditText)findViewById(R.id.url_edit);
        sap1 =(TextView)findViewById(R.id.sap1);
        sap2 =(TextView)findViewById(R.id.sap2);
        mcardview = (CardView)findViewById(R.id.cardview);
        validateLayout = (LinearLayout)findViewById(R.id.validate_layout);
        mRegisterLayout = (LinearLayout)findViewById(R.id.register_layout);
        mandroid_id_crypt = (TextView)findViewById(R.id.android_id_crypt);
        mEditValidate.requestFocus();
        mValidateBtn = (Button)findViewById(R.id.click);
        mRegBtn = (Button)findViewById(R.id.click1);
textView_id = (TextView)findViewById(R.id.android_id);
textView_id_cot = (TextView)findViewById(R.id.android_id_cat);
         apiInterface = APIClient.getClient().create(APIInterface.class);
         telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        DevicePolicyManager dpm = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(getApplicationContext(), MyDeviceAdminReceiver.class);

        if (!dpm.isAdminActive(compName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please activate device admin to protect the app.");
            startActivity(intent);
        }
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonObject object = new JsonObject();
                String valueHashed;
                    valueHashed = hashedAfterConcat;
                object.addProperty("hash", valueHashed);
                Log.e("Register","Register called 1 ");
                textView_id_cot.setText("");
                textView_id.setText("");
                queryDetails(mEditReg);
                Log.e("Register","Register EXITED  ");

            }
        });
        mValidateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView_id_cot.setText("");
                textView_id.setText("");
                sap1.setVisibility(View.VISIBLE);
                sap2.setVisibility(View.VISIBLE);
                Log.e("Register INV","Register INV called 2 ");
                queryDetails(mEditValidate);
                Log.e("Register INV","Register INV EXITED ");
               // getIMSI(MainActivity.this);
              //  getIMSI(MainActivity.this);
               // getICCDData();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                } /*else {
                    textView.setText(""+telephonyManager.getSimSerialNumber());
                }*/
            }
        });
        if (ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE}, 101);
        }
        if (ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // if permissions are not provided we are requesting for permissions.
            ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE}, REQUEST_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // if permissions are not provided we are requesting for permissions.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE_BT);
        }

        // in the below line, we are setting our imei to our text view.
//Commented out the code as these are NAtive or Previalaged permission calls.
      /*  getUniqueIMEIId(this);
        getIMSI(this);
        getWifiMacAddress(this);
        getSimSerailNumber(this);*/


    }
/* Once Register of Device is success store the status Local in PReference.*/
    private void storeRegisterStatus(boolean value){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
// Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
// Storing the key and its value as the data fetched from edittext
        myEdit.putBoolean("reg_status", value);
        // Once the changes have been made, we need to commit to apply those changes made,
// otherwise, it will throw an error
        myEdit.commit();

    }

    /*Query of the Device Register state during every launch or restart of the app ( swipe kill,add push to background)*/
    private boolean getRegisterStatus(){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
// Creating an Editor object to edit(write to the file)
        boolean s1 = sharedPreferences.getBoolean("reg_status", false);
        return s1;

    }

/*Method to Query or GET ICCD data*/
    private void getICCDData() {

        SubscriptionManager subsManager = (SubscriptionManager) getApplicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<SubscriptionInfo> subsList = subsManager.getActiveSubscriptionInfoList();
        if (subsList!=null) {
            for (SubscriptionInfo subsInfo : subsList) {
                if (subsInfo != null) {
                  String  simSerialNo  = subsInfo.getIccId();
                  Log.e("SERAIL NUMBER","SERIAL NUMBER "+simSerialNo);
                }
            }
        }
    }

    /* fetch SIM SERIAL NUMBER FROM DEVICE*/
    private void getSimSerailNumber(MainActivity mainActivity) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.getSimSerialNumber();
            mEditValidate.setText(mEditValidate.getText() + "\n" + "get Sim Serial no:." + telephonyManager.getSimSerialNumber() + "\n" + "-------------");
        } catch (Exception e) {
            mEditValidate.setText(mEditValidate.getText() + "\n" + "get Sim Serial no:." + e.getMessage() + "\n" + "-------------");
        }
    }
    /* fetch WIFI MAC ADDRESS FROM DEVICE*/
    private void getWifiMacAddress(MainActivity context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String macAddress = wifiInfo.getMacAddress();

            // Use the MAC address as needed
            System.out.println("Wi-Fi MAC Address: " + macAddress);
            Toast.makeText(context, "getWifiMacAddress:." + macAddress, Toast.LENGTH_SHORT).show();
            mEditValidate.setText(mEditValidate.getText() + "\n" + "getWifiMacAddress:." + macAddress + "\n" + "-------------");

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            boolean isEnabled = mBluetoothAdapter.isEnabled();
            if (!isEnabled) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    mBluetoothAdapter.enable();
                }
                mBluetoothAdapter.enable();
            }
            String btAddress = mBluetoothAdapter.getAddress();
            mEditValidate.setText(mEditValidate.getText() + "\n" + "get bt MacAddress:." + btAddress + "\n" + "-------------");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "getWifiMacAddress:." + e.getMessage(), Toast.LENGTH_SHORT).show();
            mEditValidate.setText(mEditValidate.getText() + "\n" + "getWifiMacAddress:." + e.getMessage() + "\n" + "-------------");
        }
    }
    /* fetch IMSI FROM DEVICE*/
    private  String getIMSI(MainActivity context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                Log.e("QUERY IMSI","QUERY IMSI::"+telephonyManager.getSubscriberId());
                return telephonyManager.getSubscriberId();  // IMSI
            }
            return null;
        }
         catch (Exception e) {
            e.printStackTrace();
             Log.e("QUERY IMSI","QUERY IMSI:EXCEPTION:"+e.getMessage().toString());
            Toast.makeText(context, "getIMSI:." + e.getMessage(), Toast.LENGTH_SHORT).show();
           // textView.setText(textView.getText() + "\n" + "getIMSI:." + e.getMessage() + "\n" + "-------------");
        }
        return null;
    }

    /* fetch FINGER PRINT and RADIO VERSION FROM DEVICE*/
    private void getInfo() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
          //  serial = Build.getSerial();
            finger = Build.FINGERPRINT;
        }
        else
        {
           // serial = Build.SERIAL;
            finger = Build.FINGERPRINT;
        }
       BuildVersion =  Build.VERSION.INCREMENTAL;
        radioVersion = Build.getRadioVersion();

        try {
            textView_id_cot.setText(textView_id_cot.getText().toString()+ " \n:$ :"+"SERAIL :" +"" + "\n$ :"+"FINGER"+finger +"\n$ BuildVersion :"+BuildVersion +"\n $radioVersion:"+radioVersion);
            textView_id.setText(textView_id.getText().toString()+ " \n:$ :"+"SERAIL :" +"" + "\n$ :"+"FINGER"+toHexString(getSHA(finger)) +"\n$ BuildVersion :"+toHexString(getSHA(BuildVersion)) +"\n $radioVersion:"+toHexString(getSHA(radioVersion)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    /* fetch SIM SERIAL NUMBER FROM DEVICE*/
    public String getUniqueIMEIId(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                return "";
            }
            String imei = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                imei = telephonyManager.getImei();
            }
            Log.e("imei", "=" + imei);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "getUniqueIMEIId:." + e.getMessage(), Toast.LENGTH_SHORT).show();
            mEditValidate.setText(mEditValidate.getText() + "\n" + "getUniqueIMEIId:." + e.getMessage() + "\n" + "-------------");
        }
        return "not_found";
    }

    //Generic method to call Register / Validate the device with the API input details passed over.
    private void queryDetails(EditText regEdit) {
        Log.e("Register","Register called 2 ");
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(regEdit.getWindowToken(), 0);
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        getInfo();
        try {
            textView_id_cot.setText(textView_id_cot.getText().toString()+"\n $+Android ID:"+androidId);
            textView_id.setText(textView_id.getText().toString()+"\n $:"+"AndroidID :" +toHexString(getSHA(androidId)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        String code = null;  String b_version = null;  String fingerprint = null;
        User user;
        JsonObject object = new JsonObject();
        object.addProperty("androidId",androidId);
        try {
             newConcatValue = androidId+"$"+finger+"$"+Integer.toString(Build.VERSION.SDK_INT);

            Log.e("Register/Validate","Register called 2:"+ androidId +"finger :"+finger +"SOFT :"+Integer.toString(Build.VERSION.SDK_INT));
           code =   toHexString(getSHA(androidId));
           b_version = toHexString(getSHA(BuildVersion));
           fingerprint = toHexString(getSHA(finger));
           hashedAfterConcat = toHexString(getSHA(newConcatValue));
            Log.e("HASHED DATA","register data hashed:"+ hashedAfterConcat);
            userReg.mAndroidId = androidId;
            userReg.mFingerPrint = finger;
            userReg.mSoftwareVersion = Build.VERSION.SDK_INT;

            user = new User(androidId);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

       /* Log.e("HASHED DATA","register data :"+newConcatValue);
        Log.e("HASHED DATA","register data hashed:"+ hashedAfterConcat);*/
        // makeAPICall(textView.getText().toString().trim(),object);
if(!getRegisterStatus()) {
    Log.e("Register","Register called 3 ");
    makeRegisterCall(regEdit.getText().toString(), userReg);
    mSaveUrls();
}else{
    Log.e("Register INV","Register INV called Validate ");
    JsonObject object1 = new JsonObject();
    object1.addProperty("hash", hashedAfterConcat);
    makeValidateAPICall(mEditValidate.getText().toString(),object1);
    mSaveUrls();

}
    }




    private static String SECRET_KEY = "aesEncryptionKey";
    private static String INIT_VECTOR = "encryptionIntVec";

    public static String encrypt(String value) {
        try {
          String beforeEncrpty = mandroid_id_crypt.getText().toString()+value;
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            Log.e("HASHED DATA","HASHED DATA after sri Encrypted :"+Base64.encodeToString(encrypted, Base64.DEFAULT));
            mandroid_id_crypt.setText("beforeEncrpty :"+ beforeEncrpty+"\n"+"Encrypted :"+mandroid_id_crypt.getText().toString()+Base64.encodeToString(encrypted, Base64.DEFAULT));
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //Method to get SHA
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    //Conver hash data to HEXA.
    public static String toHexString(byte[] hash)
    {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
       Toast.makeText(getApplicationContext(), "OnResume called... ", Toast.LENGTH_SHORT).show();
        mEditReg.setText(getRegUrlData());
        mEditValidate.setText(getValidateUrlData());
        if(getRegisterStatus()){
            validateLayout.setVisibility(View.VISIBLE);
            mRegisterLayout.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(),"Register is done",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(),"Register is not done",Toast.LENGTH_SHORT).show();
            /*validateLayout.setVisibility(View.GONE);
            mRegisterLayout.setVisibility(View.VISIBLE);*/
        }


    }

//Method to save the URLS locally into Preference for Validate and Register API used.
    private void mSaveUrls(){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
// Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        Log.e("MADE API CALL","MADE API CALL FOR mSaveUrls :"+mEditReg.getText().toString() +" VALIDATE:"+mEditValidate.getText().toString());
// Storing the key and its value as the data fetched from edittext
        myEdit.putString("url_reg", mEditReg.getText().toString());
        myEdit.putString("url_validate", mEditValidate.getText().toString());
        // Once the changes have been made, we need to commit to apply those changes made,
// otherwise, it will throw an error
        myEdit.commit();
    }

    //Method to get REGISTER URL DATA which is stored.
    private String getRegUrlData(){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
// Creating an Editor object to edit(write to the file)
        String s1 = sharedPreferences.getString("url_reg", "http://192.168.29.70:8000/v1/devicevalidate");
        Log.e("MADE API CALL","MADE API CALL FOR getRegUrlData :"+s1);
return s1;
    }

    //Method to get VALIDATE URL DATA which is stored.
    private String getValidateUrlData(){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
// Creating an Editor object to edit(write to the file)
        String s1 = sharedPreferences.getString("url_validate", "http://192.168.29.70:8000/v1/devicevalidate");
        Log.e("MADE API CALL","MADE API CALL FOR getValidateUrlData :"+s1);
        return s1;
    }

    //validate API Method
    private void makeValidateAPICall(String url , JsonObject user) {
        mcardview.setVisibility(View.VISIBLE);
        //storeRegisterStatus(false);
        Log.e("Register INV","Register INV called Validate 2:"+user);
Toast.makeText(getApplicationContext(),"Validate API called",Toast.LENGTH_SHORT).show();
        Log.e("MADE API CALL","MADE API CALL FOR VALIDATE :"+user);
       // User mUser = new User(user);

        ;
        Call<User> call1 = apiInterface.createUser(url , user);
        call1.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user1 = response.body();
                mcardview.setVisibility(View.INVISIBLE);
                if(response.code() == 200){
                    validateLayout.setVisibility(View.VISIBLE);
                    mRegisterLayout.setVisibility(View.GONE);
                    //sendRegisterData();
                    Toast.makeText(getApplicationContext(),"SUCCESS ",Toast.LENGTH_SHORT).show();
                }else if(response.code() ==103){
                    validateLayout.setVisibility(View.GONE);
                    mRegisterLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(),"Register the Device",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"FAILURE",Toast.LENGTH_SHORT).show();
                }
               // textView.setText("");
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {  Toast.makeText(getApplicationContext(), "  Failed to send"+t.getMessage().toString() , Toast.LENGTH_SHORT).show();
                Log.e("HASHED DATA","HASHED DATA ERROR:"+call);
                Log.e("Register INV","Register INV called Validate ERROR ");
                mcardview.setVisibility(View.INVISIBLE);
              /*  validateLayout.setVisibility(View.GONE);
                mRegisterLayout.setVisibility(View.VISIBLE);*/
                Toast.makeText(getApplicationContext(),"Validated Device failed"+user.toString(),Toast.LENGTH_SHORT).show();
          // sendRegisterData();
                call.cancel();
            }
        });


    }

//Register API call Method.
    private void makeRegisterCall(String url , RegisterUser user) {
        //
        Log.e("Register","Register called 4 ");

        mcardview.setVisibility(View.VISIBLE);
        JsonObject object = new JsonObject();
Toast.makeText(getApplicationContext(),"Called Register",Toast.LENGTH_SHORT).show();
        object.addProperty("androidid",user.mAndroidId);
        object.addProperty("fingerprint",user.mFingerPrint);
        object.addProperty("software",user.mSoftwareVersion);
        Log.e("HASHED DATA","RegisterUser after conc :"+object.toString());
        Log.e("Register","Register called 5 ");

        Log.e("MADE API CALL","MADE API CALL FOR REGISTER :"+object);
        Call<RegisterUser> call1 = apiInterface.register(url , object);
        call1.enqueue(new Callback<RegisterUser>() {
            @Override
            public void onResponse(Call<RegisterUser> call, Response<RegisterUser> response) {
                RegisterUser user1 = response.body();
                Log.e("Register","Register called 6 ");
                if(response.code() == 200) {
                    Toast.makeText(getApplicationContext(), "Register Successfully ", Toast.LENGTH_SHORT).show();
                    storeRegisterStatus(true);
                    validateLayout.setVisibility(View.VISIBLE);
                    mRegisterLayout.setVisibility(View.GONE);

                }else if(response.code()!=200){
                    storeRegisterStatus(false);
                    Toast.makeText(getApplicationContext(),"Registration got failed",Toast.LENGTH_SHORT).show();
                }
                /*}else if(response.code() == 103){
                    Toast.makeText(getApplicationContext(),"Register Device failed",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Register Device failed",Toast.LENGTH_SHORT).show();
                }*/
               // textView.setText("");
                mcardview.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<RegisterUser> call, Throwable t) {  Toast.makeText(getApplicationContext(), "  Failed to send"+t.getMessage().toString() , Toast.LENGTH_SHORT).show();
                Log.e("HASHED DATA","HASHED DATA ERROR:"+call);
                mcardview.setVisibility(View.INVISIBLE);
                storeRegisterStatus(false);
               /* validateLayout.setVisibility(View.VISIBLE);
                mRegisterLayout.setVisibility(View.GONE);*/
                Log.e("Register","Register called 7 just making it as try ");
                call.cancel();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

            } else{
                //not granted
            }
        }
        if(requestCode == PERMISSION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
                            PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                mEditValidate.setText("" + telephonyManager.getSimSerialNumber());
            }
        }
        else if (requestCode == REQUEST_CODE) {
            // in the below line, we are checking if permission is granted.
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // if permissions are granted we are displaying below toast message.
               // getUniqueIMEIId(this);
                Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // in the below line, we are displaying toast message
                // if permissions are not granted.
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_BT) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission BT granted.", Toast.LENGTH_SHORT).show();
            } else {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                boolean isEnabled = mBluetoothAdapter.isEnabled();
                if (!isEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        mBluetoothAdapter.enable();
                    }

                }
            }
        }
    }
}