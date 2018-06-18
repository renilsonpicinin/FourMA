package {{projectpackage}}.test;

import android.test.ActivityInstrumentationTestCase2;
import {{projectpackage}}.R;
import {{projectpackage}}.{{activity}};
import {{projectpackage}}.test.util.*;
import {{projectpackage}}.test.adapters.*;
import com.general.mbts4ma.erunner.*;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

{{otherimports}}

public class {{testingclassname}}Test extends ActivityInstrumentationTestCase2 {

	private static final String BASIC_SAMPLE_PACKAGE = "";
    private static final int LAUNCH_TIMEOUT = 5000;
    private UiDevice device;

    @Before
    public void startMainActivityFromHomeScreen() {

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        device.pressHome();

        final String launcherPackage = device.getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)),
                LAUNCH_TIMEOUT);
        
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }

        device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT);
    }
    
    {{testingmethodtemplate}}

}
