*** Setting ***
Library	SeleniumLibrary

*** Test Cases ***
Test 1
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	test2_locator	test2_1
	Input Text	test3_locator	test3_2
	Click Element	//*[@id="login-button"]
Test 2
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	test1_locator	test1_1
	Input Text	test3_locator	test3_1
	Click Element	//*[@id="login-button"]
Test 3
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	test2_locator	test2_2
	Input Text	test3_locator	test3_3
	Input Text	test1_locator	test1_1
	Input Text	test3_locator	test3_3
	Click Element	//*[@id="login-button"]
