UiScrollable listView = new UiScrollable(new UiSelector().{{selector}}(""));
listView.setMaxSearchSwipes(100);
listView.scrollTextIntoView("{{text}}");
listView.waitForExists(5000);

listView.getChildByText(new UiSelector()
        .className(android.widget.TextView.class.getName()), "{{text}}").click();