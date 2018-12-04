package {{projectpackage}}.test.adapters;

public class {{testingclassname}}Adapter {

    private AndroidDriver driver = null;

    public {{testingclassname}}Adapter(AndroidDriver driver) {
        this.driver = driver;
    }

	{{testingmethodtemplate}}

}
