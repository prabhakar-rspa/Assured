# AssuredApp
Repository for RSPCA Assured Auditing Android App

## Org Connection
The Assured app is connected to salesforce using the 'RSPCA_Assured_Auditing' connected app that was created in production.
The the connection to the org can be setup from the bootconfig filte which can be found at 'app\res\values\bootconfig.xml'.
1. Replace the remoteAccessConsumerKey value with the consumer key from your connected app.
2. Replace the oauthRedirectURI value with the callback URL from your connected app. for connecting to the sandbox, use 'https://test.salesforce.com/services/oauth2/success' and to connect to the production org, use 'https://login.salesforce.com/services/oauth2/success'.

## Dark Mode 
On the settings page, dark mode is switched on by clicking the dark mode switch. 

Dark colour values are made in a colors.xml(night) file.

The ```SharedPreferences``` API is used to store the last chosen theme so that the app launches with the correct theme when launching after closing. 

```darkSwitch.setOnCheckedChangeListener()``` is used to check if the dark mode switched is off or on. 
When it's set to off then the light mode theme is applied and when on, the dark mode theme is applied. 

```saveNightModeState``` is used to edit and save the SharedPreferences value to reapply the correct theme when launching. 

```checkNightModeActivated()``` is the method used to check the SharedPreferece value and flips the dark mode switch to match the saved SharedPreferences.
