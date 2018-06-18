package {{projectpackage}}.test.adapters;

import com.general.mbts4ma.erunner.*;

public class {{testingclassname}}Adapter {

    private UiDevice device = null;

    public {{testingclassname}}Adapter(UiDevice device) {
        this.device = device;
    }

	{{testingmethodtemplate}}

}
