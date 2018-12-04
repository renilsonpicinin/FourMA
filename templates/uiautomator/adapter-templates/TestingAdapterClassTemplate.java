package {{projectpackage}}.test.adapters;

public class {{testingclassname}}Adapter {

    private UiDevice device = null;

    public {{testingclassname}}Adapter(UiDevice device) {
        this.device = device;
    }

	{{testingmethodtemplate}}

}
