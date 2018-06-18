package {{projectpackage}}.test;

import {{projectpackage}}.R;
import {{projectpackage}}.{{activity}};
import {{projectpackage}}.test.util.*;
import {{projectpackage}}.test.adapters.*;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.tools.ant.taskdefs.optional.jsp.JspC.WebAppParameter;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.general.mbts4ma.erunner.*;

{{otherimports}}

public class {{testingclassname}}Test extends ActivityInstrumentationTestCase2 {

	private static final String PACKAGE = "";
    private static final String ACTIVITY = "";
    private static final String DEVICE = "";
    private static final String PLATFORM = "";
    //private static String APKFILELOCATION = ".apk da aplicação"; //Descomente essa linha caso utilize o .apk da aplicação
    protected AndroidDriver driver;

    @Before
    public void before() throws MalformedURLException {
    	
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, DEVICE);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, PLATFORM);
        capacidade.setCapability(MobileCapabilityType.APP_ACTIVITY, ACTIVITY);

        driver = new AndroidDriver(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);
    }

    @After
    public void after() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    {{testingmethodtemplate}}

}
