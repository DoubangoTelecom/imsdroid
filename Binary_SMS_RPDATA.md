

# Presentation #
SMS stands for Short Message Service or Silent Messaging Service and is a communication service standardized in the GSM mobile communication system, using standardized communications protocols allowing the interchange of short text messages between mobile telephone devices.<br />
<br />
SMS technology has been specified by the ETSI in GSM 03.40 and 03.38 documents (3GPP TS 23.040 and 3GPP TS 23.038 respectively). These documents only describe how to use SMS over mobile networks (nothing for IP networks).<br />
In real world there are two way to receive SMS messages over mobile networks: Binary (PDU) and Text mode. <br />

## Text mode ##
The message payload is transferred « as is » into a SIP MESSAGE request (text/plain). By default this mode is disabled. To enable this mode, you should go to **Options->Messaging** and uncheck **Enable Binary SMS (RP-DATA)**<br />

## Binary mode ##
Also know as PDU (Protocol Data Unit) mode, it is use to encode the payload of the SMS sent over IMS networks. In this case the payload only contains a sequence of hexa-decimal octets or decimal semi-octets strings. The overall PDU string contains some useful information (SMSC address, Service center time stamp, sender number, message reference ...) plus the actual message.<br />
<br />
The message length can be up to 160 characters where each character represent 7bits [160/7bits], [140/8bits] or [70/16bits]. By default, each character represent 7bits encoded as per **GSM 03.38**.<br />
For IMS/LTE Networks, SMS message shall be encapsulated in RPDUs (Relay Protocol Data Unit) data string as defined in **3GPP TS 24.011**, section 7.3. The RPDU data is transferred from SM entity to SM entity using SIP MESSAGE requests. These SIP requests shall use the MIME type **"application/vnd.3gpp.sms"** for this purpose. <br />

### API Reference ###
The implementation is based on [tinySMS](http://www.doubango.org/API/tinySMS/) project.

### Technical Reference ###
  * 3GPP TS 23.038 - Alphabets and language-specific information
  * 3GPP TS 23.040 - Technical realization of Short Message Service (SMS)
  * 3GPP TS 24.011 - Point-to-Point (PP) Short Message Service (SMS) support on mobile radio interface.
  * 3GPP TS 24.341 - Support of SMS over IP networks; Stage 3
  * 3GPP TS 24.451 - Support of SMS and MMS over NGN IMS subsystem; Stage 3 [of 3GPP TS 24.341 Release 7](Endorsement.md)

### Implementation Reference ###
  * GSMA RCS release 3 - [10.2.2 SMS Service Over Broadband Access](http://www.gsmworld.com/documents/Service_Realization_v1.0%281%29.pdf)
  * GSMA VoLTE V1.0.0 - [5.5 SMS over IP](http://news.vzw.com/OneVoiceProfile.pdf)

### Configuration ###
Before you start sending any binary SMS you should set your IP-SM-GW (IP-Short-Message-Gateway) using the Messaging screen (Options->Messaging). The default value is **sip:+331000000000@open-ims.test** as shown below.<br /><br />
![![](http://imsdroid.googlecode.com/svn/trunk/screenshots/screen_messaging.png)](http://imsdroid.googlecode.com/svn/trunk/screenshots/screen_messaging.png)
<br /> <br />
The Uri of the IP-SM-GW must be a valid SIP URI with a telephone number as user info or a telephone number. Example of valid Uris: <br />
  * sip:+331000000000@open-ims.test, sip:331000000000@open-ims.test
  * tel:+331000000000
If you provide an invalid Uri the client will switch to text mode.<br />

### Sending SMS ###
You can send a SMS (both text and binary) from the dialer, the address book or the history. <br />

### Receiving SMS ###
Incoming SMSs will be signaled by a sound and new icon will be added in the notification status bar. <br /> To read the SMS you can either click on the notification icon or go to the history screen. <br /> If the received message is a binary SMS, then the acknowledgment (RP-ACK) or error (RP-ERROR) message will be sent to the SIP URI present in the P-Asserted-Identity header as per 3GPP TS 24.341 section 5.3.2.4. The originator address will come from the TPDU data encapsulated in the RP-DATA message.
<br />
For more technical information: [http://betelco.blogspot.com/2009/10/sms-over-3gpp-ims-network.html](http://betelco.blogspot.com/2009/10/sms-over-3gpp-ims-network.html)