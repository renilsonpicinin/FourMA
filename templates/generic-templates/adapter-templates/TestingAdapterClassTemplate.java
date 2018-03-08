package {{projectpackage}}.test.adapters;

import com.general.mbts4ma.erunner.*;
import com.robotium.solo.Solo;

import com.general.mbts4ma.event.*;

public class {{testingclassname}}Adapter {

    private Solo solo = null;

    public {{testingclassname}}Adapter(Solo solo) {
        this.solo = solo;
    }

{{testingmethodtemplate}}

}
