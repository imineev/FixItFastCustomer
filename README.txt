Fix-It-Fast Customer Quick-Introduction
=========================================

FixItFastCustomer-MAF-2.2.1-MCS-15.3.5 : Tested  on MCS v1.0 (15.3.5)  with MAF extension 2.2.1

The FiF Customer application requires the FIF_CUSTOMER 1.0 Mobile Backend (MBE) in Oracle MCS. 
The MBE settings panel lists the values to copy&paste into the FiF Customer application preferences that  
are accessible from the maf-application.xml in Oracle JDeveloper or in the iOS or Android preference panel
on the device. 

Note that the FiF Customer application is built against a MCS instance that uses Basic HTTP  for authentication. 

MCS instances used are Internet facing (OPC). No need of using Oracle Cisco VPN on device.

Take care to enable Location service on the device before demoing

Preferences:
============
fifMobileBackendURL 			==> MBE Base URL
fifMobileBackendName			==> FIF Mobile Backend Name : FIF_CUSTOMER
fifMobileBackendId			==> Mobile Backend ID
fifMobileBackendApplicationKeyAndroid	==> Application Key of the Android client Application in the MBE
fifMobileBackendApplicationKeyiOS 		==> Application Key of the iOS client Application in the MBE
fifMBEAnonymousKey			==> Anonymous Key of the MBE
gcmSenderId				==> Google GMC Sender ID for the Android registered client application in the MBE
appleBundleId				==> Apple Application Id for the iOS client Application in the MBE
googleApplicationPackage			==> Google Application Id for the Android client Application in the MBE
enablePush				==> Enable Receiving Push Notifications
pushMessagesForDebug			==> Shows Push Raw Messages in a debug panel. Can be enable at runtime

How-to demo
============

1. Authenticate as lynn/Welcome2!* or larry/Welcome2!*
   -> After authentication, the application registers with Apple APNS or Android GMC to receive push notifications from MCS
   -> Also after authentication, the application starts collecting Analytic events
2. In the initial Main Menu  , click on My Reports
3. In the  SR List view, select an incident to navigate to the detail page
   -> The list shows data queried from a MCS custom API   
4. Add a note to the incident. Press the Update button to submit the note
   -> Submitting the note sends an update push message to the technican and returns back to the list page
   -> on navigating back, the collected analytic events are flushed to the MCS server
5. Tab on the "Back" button in the upper left corner to navigate back to the Main Menu
6. Click on Create New Report icon
7. Create a New Incident entering Title, Problem Description and taking a photo
8. Click Submit button to create the incident  
-> Submitting the incident sends an new push message to the technican and returns back to the Main Menu
9. You can show in My Reports the newly created incident
10. In MCS, select FIF_CUSTOMER 1.0 MBE and tap onto the "Open" button. Switch to the Notifications panel using the 
   left side menu
11. In the notification dialog, add a message with the following structure
      {id:"113",title: "UPDATE:House Destroyed After Earthquake",Status:"In Progress"}
    -> ensure the id value (113 in the example) exists in MCS as otherwise navigation will fail
12. Press the send button
13. In the device, look at the notification center alert that shows when the message is received
13. Switch to MCS MBE and show Analytics. You should see analytic events like "DataControlEvent", "incident", "view-navigation" etc. 
	-> Ensure FIF_CUSTOMER is selected in the Analytic panel (otherwise you may not see information)
14. Tab the "Back" button in the upper left corner to de-register from push and to show the login screen again


Known issues: 
===============

1- The Oracle Cisco VPN access may not pass notifications through. This is especially the case for Android devices. Here you may want to 
   disconnect from VPN while showing the SR List view to see notifications 
   ==> You have no need to be connected VPN as MCS instances used are Internet facing (OPC)
   
  
What's provided
==================

1. ipa file		=> you will have to go through the  Preferences page in the appp on the device before to be able to use it.
2. apk file	=> you will have to go through the  Preferences page in the appp on the device before to be able to use it
3. JDeveloper workspace

What is not provided
====================

For legal and licensing reason, the following information is not shipped with the sample

1. certificate to compile and sign application 
2. p12 certificate to setup iOS client application in MCS
3. Google GCM sender Id and project key

If you need one of these, please get in touch with chris.muir@oracle.com to help you configuring your MBE instance. This however is only possible
for Oracle employees. 