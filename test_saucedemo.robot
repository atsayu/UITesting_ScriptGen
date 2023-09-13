*** Setting ***
Library	SeleniumLibrary

*** Test Cases ***
Normal Login 1
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	standard_user
	Input Text	//*[@id="password"]	secret_sauce
	Click Element	//*[@id="login-button"]
	Location should be	https://www.saucedemo.com/inventory.html
Normal Login 2
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	problem_user
	Input Text	//*[@id="password"]	secret_sauce
	Click Element	//*[@id="login-button"]
	Location should be	https://www.saucedemo.com/inventory.html
Locked 1
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	locked_out_user
	Input Text	//*[@id="password"]	secret_sauce
	Click Element	//*[@id="login-button"]
WrongPassword 1
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	standard_user
	Input Text	//*[@id="password"]	invalid_pass1
	Click Element	//*[@id="login-button"]
WrongPassword 2
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	standard_user
	Input Text	//*[@id="password"]	invalid_pass2
	Click Element	//*[@id="login-button"]
WrongPassword 3
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	standard_user
	Input Text	//*[@id="password"]	invalid_pass3
	Click Element	//*[@id="login-button"]
WrongPassword 4
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	problem_user
	Input Text	//*[@id="password"]	invalid_pass1
	Click Element	//*[@id="login-button"]
WrongPassword 5
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	problem_user
	Input Text	//*[@id="password"]	invalid_pass2
	Click Element	//*[@id="login-button"]
WrongPassword 6
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	problem_user
	Input Text	//*[@id="password"]	invalid_pass3
	Click Element	//*[@id="login-button"]
EmptyPassword 1
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	standard_user
	Input Text	//*[@id="password"]	${EMPTY}
	Click Element	//*[@id="login-button"]
EmptyPassword 2
	Open Browser	https://www.saucedemo.com/	Chrome
	Input Text	//*[@id="user-name"]	problem_user
	Input Text	//*[@id="password"]	${EMPTY}
	Click Element	//*[@id="login-button"]
