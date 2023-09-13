*** Setting ***
Library	SeleniumLibrary

*** Test Cases ***
Normal Login 1
	Open Browser	https://demoqa.com/login	Chrome
	Maximize Browser Window
	Input Text	//*[@id="userName"]	quyenhoang03
	Input Text	//*[@id="password"]	Testing@123
	Click Element	//*[@id="login"]
	Sleep    5
	Location should be	https://demoqa.com/profile
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
WrongPassword 1
	Open Browser	https://demoqa.com/login	Chrome
	Input Text	//*[@id="userName"]	quyenhoang03
	Input Text	//*[@id="password"]	invalid_pass1
	Click Element	//*[@id="login"]
WrongPassword 2
	Open Browser	https://demoqa.com/login	Chrome
	Input Text	//*[@id="userName"]	quyenhoang03
	Input Text	//*[@id="password"]	invalid_pass2
	Click Element	//*[@id="login"]
WrongPassword 3
	Open Browser	https://demoqa.com/login	Chrome
	Input Text	//*[@id="userName"]	quyenhoang03
	Input Text	//*[@id="password"]	invalid_pass3
	Click Element	//*[@id="login"]
