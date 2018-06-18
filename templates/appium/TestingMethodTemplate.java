@Test
	public void test{{testingmethodname}}() {
	    String ces = "{{ces}}";
	    
	    new EventRunner().executeCompleteEventSequence(new {{testingclassname}}Adapter(drive), ces);
	}