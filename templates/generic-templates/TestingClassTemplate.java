package {{projectpackage}}.test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

{{otherimports}}

public class {{testingclassname}}Test {

    @SuppressWarnings("unchecked")
    public {{testingclassname}}Test() {
        super({{activity}}.class);
    }
    
{{testingmethodtemplate}}

}
