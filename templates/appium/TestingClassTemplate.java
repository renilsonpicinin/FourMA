package {{projectpackage}}.test;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

{{otherimports}}

public class {{testingclassname}}Test extends ActivityInstrumentationTestCase2 {

	private static final String ACTIVITY = "";
    private static final String DEVICE = "";
    private static final String PLATFORM = "";
    
    protected AndroidDriver driver;

    @Before
    public void before() throws MalformedURLException {
    	
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, DEVICE);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, PLATFORM);
        capabilities.setCapability(MobileCapabilityType.APP, ACTIVITY);

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