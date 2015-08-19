

# SIP Account #
If you are a newbie and don't have a SIP account I recommend using sip2sip.info.<br />
To create an account: https://mdns.sipthor.net/register_sip_account.phtml.<br />

# SIP Configuration #

Before starting to use the client you should configure your identity and the network settings. This short guide explain how to do this. <br />
If you don't have enough time and:
  * is a **Freephonie** user: [http://code.google.com/p/imsdroid/wiki/Freephonie](http://code.google.com/p/imsdroid/wiki/Freephonie)<br />
  * is a **iptel.org** user: [http://code.google.com/p/imsdroid/wiki/iptel\_org](http://code.google.com/p/imsdroid/wiki/iptel_org)<br />
  * is a **pbxes.org** user: [http://code.google.com/p/imsdroid/wiki/pbxes\_org](http://code.google.com/p/imsdroid/wiki/pbxes_org)<br />
  * is a **sip2sip.info** user: [http://code.google.com/p/imsdroid/wiki/sip2sip\_info](http://code.google.com/p/imsdroid/wiki/sip2sip_info)<br />

## Identity ##
At the home screen, go to Options (![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/options_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/options_48.png)) -> Identity (![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/identity_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/identity_48.png)) to open the **Identity screen**.<br />

<table>
<tr><td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_identity.png' /></td></tr>
<tr align='center'><td><b>Identity Screen</b></td></tr>
</table>

**Display Name:** Your nickname. Useless in this beta version.<br />

**IMS Public Identity (IMPU):** As its name says, it’s your public visible identifier where you are willing to receive calls or any demands. An IMPU could be either a SIP or tel URI (e.g. tel:+33100000 or sip:bob@open-ims.test).<br />
For those using IMSDroid as a basic SIP client, the IMPU should coincide with their SIP URI (a.k.a SIP address).<br />

**IMS Private Identity (IMPI):** The IMPI is a unique identifier assigned to a user (or UE) by the home network. It could be either a SIP URI (e.g. sip:bob@open-ims.test), a tel URI (e.g. tel:+33100000) or any alphanumeric string (e.g. bob@open-ims.test or bob). <br />
For those using IMSDroid as a basic SIP client, the IMPI should coincide with their authentication name. If you don't know what is your IMPI, then fill the field with your SIP address as above.<br />

**Password:** Your password.<br />

**Realm:** The realm is the name of the domain to authenticate to. It should be a valid SIP URI (e.g. sip:open-ims.test). <br />

**3GPP Early IMS Security:** If you are not using an IMS server you should use this option to disable some heavy IMS authentication procedures.<br />

## Network Settings ##
At the home screen, go to Options (![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/options_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/options_48.png)) -> Network(![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/network_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/network_48.png)) to open the **Network screen**. <br />

<table>
<tr><td><a href='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_network.png'><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_network.png' /></a></td></tr>
<tr align='center'><td><b>Network Screen</b></td></tr>
</table>

**Enable WiFi:** To enable WiFi.<br />

**Enable 4G/3G/2.G:** To enable 4G (e.g. LTE), 3G (e.g. UMTS) and 2.5G (e.g. EDGE) networks. You should enable this option when testing on the emulator.<br />

**IPv4** or **IPv6:** Define the IP version of the proxy-cscf host. If you don't know what is the right value, then keep **IPv4** checked.<br />

**Proxy-CSCF Host:** This is the IPv4/IPv6 address or FQDN (Fully-Qualified Domain Name) of your SIP server or outbound proxy (e.g. 88.89.125.125 or example.com or 2a01:e35:8b32:7050:212:f0ff:fe99:c9fc). <br />

**Proxy-CSCF Port:** The port associated to the proxy host. Should be **5060**.<br />

**Transport:** The transport type (TCP or UDP) to use.<br />

**Proxy-CSCF Discovery:** Should be None. <br />

**Enable SigComp:** Will enable Signaling Compression. Only check if your server support this feature. Should not be checked. <br />

## Connecting ##
Return to the home screen and click on **Sign In** item to connect to your SIP server. <br />
If the connection succeed, you will have new items in the home screen (see below) and a green notification icon will be added in the status bar. <br />

![![](http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/home.png)](http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/home.png)

# Voice/Video Call #
This beta version (v1.0.0) already supports both audio and video calls.<br />
  * Supported Audio Codecs: AMR-NB, GSM, PCMA, PCMU, Speex-NB <br />
  * Supported Video codecs: H264, Theora, H.263, H.263-1998, H.261 <br />

## Codecs ##
To choose which audio/video codecs to enable/disable you should go to Options(![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/options_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/options_48.png)) -> Codecs(![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/codecs_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/codecs_48.png)) screen page as shown below. <br />

<table>
<tr><td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_codecs.png' /></td></tr>
<tr align='center'><td><b>Codecs Screen</b></td></tr>
</table>

To have decent video quality, you should only check Theora or H.264 codecs. However, these codecs require at least a 600MHz processor. <br /> If you are using an old Android device (e.g. Android G1), then you should only select H.263 and H.263+. <br /> Of course the remote party should also support the same codecs. <br />
If you are using one-way video services (e.g. VoD, IPTV) you can use any device because the video decoding process require less CPU resources. <br />

## Audio/Video call ##
You can place a call from the dialer, address book or the history screen. <br />

### From the Dialer ###
The dialer is accessible from the home screen (![![](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/dialer_48.png)](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/dialer_48.png)). <br /> The dialer screen is shown below. <br />
You can enter any phone number (e.g. '+33100000000' or '0600000000'), SIP URI (e.g. 'sip:bob@open-ims.test'). If the SIP Uri is incomplete (e.g. 'bob') the application will automatically append the scheme ('sip:') and domain name('@open-ims.test') before placing the call. <br /> If you put a telephone number with 'tel:' prefix, the client will try to map it to a SIP URI using ENUM protocol. <br />

<table>
<tr><td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_dialer.png' /></td></tr>
<tr align='center'><td><b>Dialer Screen</b></td></tr>
</table>

From the Dialer screen you can click on:<br />
![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/avatar_24.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/avatar_24.png) to select the phone number from the native address book. <br />
![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/voice_call_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/voice_call_48.png)  to make audio call. <br />
![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/visio_call_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/visio_call_48.png)  to make audio/video call. <br />
![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/message_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/message_48.png)  to send a SIP MESSAGE (Short IM). <br />

### From the History ###
All incoming, outgoing and missed calls and message will be logged in the history screen.<br /> To redial a number from the history screen, click on the history icon (![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/history_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/history_48.png)) from the home screen. <br />

<table>
<tr>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_history.png' /></td>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_history_ctx.png' /></td>
</tr>
<tr align='center'>
<td><b>History Screen</b></td>
<td><b>History Screen with context menu</b></td>
</tr>
</table>

The context menu is opened when you select an entry and make a long click. <br />

### From the Address Book ###
The entries in the address book come from the phone unless you have enabled XCAP. <br />

<table>
<tr>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_contacts.png' /></td>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_contacts_ctx.png' /></td>
</tr>
<tr align='center'>
<td><b>Contacts Screen</b></td>
<td><b>Contacts Screen with context menu</b></td>
</tr>
</table>


### In Call Screen ###
Once the call is placed a new screen (In Call Screen) will be automatically opened and a notification icon will be added in the status bar (![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/phone_call_25.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/phone_call_25.png)). As long as you are in call this icon will remain in the status bar. This icon will allow you to reopen the 'In Call Screen'. The Audio stream will continue even if you leave this screen or the application but the video stream will be stopped (restarted when you come back). <br />

<table>
<tr><td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_av.png' /></td></tr>
<tr align='center'><td><b>In Call Screen</b></td></tr>
</table>
As you can remark, your preview image only contains a white box. It's because you have to explicitly start the outgoing video stream.<br /> This is done by clicking on the Android menu button and selecting ![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/video_start_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/video_start_48.png) item. <br /> ![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/video_stop_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/video_stop_48.png) item will be used to stop the video. <br />
Select ![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/image_gallery_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/image_gallery_48.png) to share content (image, video, vcard, ...) with the remote party.<br />

<table>
<tr>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_av_ctx.png' /></td>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_av_fd.png' /></td>
</tr>
<tr align='center'>
<td><b>In Call Screen with context menu</b></td>
<td><b>In Call Screen with full-duplex video</b></td>
</tr>
</table>
### Video Quality ###
The application can decode any video size (SQCIF, QCIF, CIF, 4CIF, ...). <br />
By default, it will encode/send QCIF video for performance reasons. To allow high quality video (CIF and above), go to Options (![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/options_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/options_48.png)) -> QoS/QoE (![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/qos_qoe_48.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/qos_qoe_48.png)) and change **Bandwidth** value from **Low** to **Medium** or **High**. <br /> CIF video encoding requires at least a 600MHz processor.<br />
  * SQCIF=128x96 (Bandwidth value should be equal to **Low** or higher)
  * QCIF=176x144 (Bandwidth value should be equal to **Low** or higher)
  * CIF=352x288 (Bandwidth value should be equal to **Medium** or higher)

# Content Sharing #
This feature allow you to share any content. A content could be a picture, video, vcard, ...<br /> You can share a content from these screens: Dialer, Contacts, History and In Call. <br />
At any time you can see the pending transfers by selecting ![http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/image_gallery_25.png](http://imsdroid.googlecode.com/svn/branches/1.0/imsdroid/res/drawable/image_gallery_25.png) from the home screen or status bar. <br /> You can select an item from the list to get more information about the transfer. <br />

<table>
<tr>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_content_select.png' /></td>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_content_pending.png' /></td>
</tr>
<tr align='center'>
<td><b>Select Content to share</b></td>
<td><b>Pending Transfers</b></td>
</tr>
</table>


# Calling iPhone/iPod Touch devices #
If you are an iOS developer there is the aplha version of idoubs (http://code.google.com/p/idoubs/) which allows face to face chat with iPhone/iPod Touch devices. <br />