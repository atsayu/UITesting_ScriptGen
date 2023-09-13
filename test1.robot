*** Setting ***
Library	SeleniumLibrary

*** Test Cases ***
Normal Login 1
	Open Browser	https://demoqa.com/login	Chrome
	Maximize Browser Window
	Input Text	//*[@id="userName"]	quyenhoang03
	Input Text	//*[@id="password"]	Testing@123
	Click Element	//*[@id="login"]
    Location Should Be    https://demoqa.com/profile
Wrongusername 1
	Open Browser	https://demoqa.com/login	Chrome
	Maximize Browser Window
	Input Text	//*[@id="userName"]	invalid_1
	Input Text	//*[@id="password"]	Testing@123
	Click Element	//*[@id="login"]
Wrongusername 2
	Open Browser	https://demoqa.com/login	Chrome
	Maximize Browser Window
	Input Text	//*[@id="userName"]	invalid_2
	Input Text	//*[@id="password"]	Testing@123
	Click Element	//*[@id="login"]
Wrongusername 3
	Open Browser	https://demoqa.com/login	Chrome
	Maximize Browser Window
	Input Text	//*[@id="userName"]	invalid_3
	Input Text	//*[@id="password"]	Testing@123
	Click Element	//*[@id="login"]
EmptyUsername 1
	Open Browser	https://demoqa.com/login	Chrome
	Maximize Browser Window
	Input Text	//*[@id="userName"]	${EMPTY}
	Input Text	//*[@id="password"]	Testing@123
	Click Element	//*[@id="login"]
