UiScrollable spinner = new UiScrollable(new UiSelector().resourceId(""));
spinner.click();
spinner.scrollTextIntoView("{{text}}");
spinner.waitForExists(5000);

spinner.getChildByText(new UiSelector()
        .className(android.widget.TextView.class.getName()), "{{text}}").click();