package shatarupa.cuidate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;
import java.util.List;

public class Contact_Picker
  extends AppCompatActivity
{
  private static final int CONTACT_PICKER_REQUEST = 991;
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Context context = this;
    if (ContextCompat.checkSelfPermission(Contact_Picker.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
      new MultiContactPicker.Builder(Contact_Picker.this) //Activity/fragment context
              .theme(R.style.MyCustomPickerTheme) //Optional - default: MultiContactPicker.Azure
              .hideScrollbar(false) //Optional - default: false
              .showTrack(true) //Optional - default: true
              .searchIconColor(Color.WHITE) //Optional - default: White
              .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
              .handleColor(ContextCompat.getColor(Contact_Picker.this, R.color.colorPrimary)) //Optional - default: Azure Blue
              .bubbleColor(ContextCompat.getColor(Contact_Picker.this, R.color.colorPrimary)) //Optional - default: Azure Blue
              .bubbleTextColor(Color.WHITE) //Optional - default: White
              .showPickerForResult(CONTACT_PICKER_REQUEST);
    }
    else
    {
      Toast.makeText(this, "Permission to read contacts not granted!! Please provide the required permissions and try again!!", 1).show();
      startActivity(new Intent(this, Home.class));
    }
  }
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CONTACT_PICKER_REQUEST) {
      SharedPreferences settings = getSharedPreferences("pref_settings", 0);
      SharedPreferences.Editor editor = settings.edit();
      if (resultCode == RESULT_OK) {
        List<ContactResult> results = MultiContactPicker.obtainResult(data);
        int numberOfContacts = results.size();
        String[] emergencyContactName = new String[numberOfContacts];
        String[] emergencyContactNumber = new String[numberOfContacts];
        editor.putInt("numberOfEmergencyContacts", numberOfContacts);
        for (int x = 0; x < numberOfContacts; x++)
        {
          emergencyContactName[x] = ((ContactResult)results.get(x)).getDisplayName();
          emergencyContactNumber[x] = ((ContactResult)results.get(x)).getPhoneNumbers().toString();
          Log.i("Contact Name" + x, emergencyContactName[x]);
          Log.i("Contact Number" + x, emergencyContactNumber[x]);
          editor.putString("emConName" + x, emergencyContactName[x]);
          editor.putString("emConNum" + x, emergencyContactNumber[x]);
        }
        editor.putBoolean("Contacts_Added_Status", true);
      } else if (resultCode == RESULT_CANCELED) {
        System.out.println("User closed the picker without selecting items.");
      }
      editor.apply();
      startActivity(new Intent(this, Home.class));
    }
    finish();
  }
}