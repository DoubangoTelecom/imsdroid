This application uses [Doubango Framework](http://www.doubango.org/).
<br />

<font color='green' size='2'>
<strong>IMSDroid v2.x preview is now available for developers</strong><br />
The source code is under <strong>branches/2.0</strong> and depends and <strong>doubango v2.x</strong><br />
<br />
New features: <br />
- The SIP/IMS Stack is 7 times faster<br />
- Full HD (1080p) video<br />
- NAT Traversal using ICE<br />
- Adds support for TLS, SRTP and RTCP<br />
- NGN (Next Generation Network) stack for developers (<strong>android-ngn-stack</strong>)<br />
- Better audio quality (Adaptive jitter buffer, noise suppression, automatic resampling, gain control, ...)<br />
- Better video quality (low latency, low cpu usage, ...)<br />
- VP8 video codec<br />
- Multi-line<br />
- MSRP chat<br />
- Fix many issues<br />
</font>


[3GPP IMS](http://en.wikipedia.org/wiki/IP_Multimedia_Subsystem) (IP Multimedia Subsystem) is the next generation network for delivering IP multimedia services. IMS is standardized by the 3rd Generation Partnership Project (3GPP).
IMS services could be used over any type of network, such as [3GPP LTE](http://en.wikipedia.org/wiki/3GPP_Long_Term_Evolution), GPRS, Wireless LAN, CDMA2000 or fixed line. <br />

[IMSDroid](http://code.google.com/p/imsdroid/) is the first fully featured open source 3GPP IMS Client for Android devices (1.5 and later). The main purpose of the project is to exhibit [doubango](http://doubango.org)'s features and to offer an IMS client to the open source community. [doubango](http://doubango.org) is an experimental, open source, 3GPP IMS/LTE framework for both embedded (Android, Windows Mobile, Symbian, iPhone, iPad, ...) and desktop systems (Windows XP/Vista/7, MAC OS X, Linux, ...) and is written in ANSI-C to ease portability. The framework has been carefully designed to efficiently work on embedded systems with limited memory and low computing power. <br />
As the SIP implementation follows [RFC 3261](http://www.ietf.org/rfc/rfc3261.txt) and [3GPP TS 24.229 Rel-9](http://www.3gpp.org/ftp/Specs/html-info/24229.htm) specifications, this will allow you to connect to any compliant SIP registrar. <br />
<br />

The current version of IMSDroid partially implements [GSMA Rich Communication Suite release 3](http://www.gsmworld.com/our-work/mobile_lifestyle/rcs/index.htm) and [The One Voice profile V1.0.0](http://news.vzw.com/OneVoiceProfile.pdf) (LTE/4G, also known as [GSMA VoLTE](http://www.gsmworld.com/our-work/mobile_broadband/VoLTE.htm)) specifications. Missing features will be implemented in the next releases. **Stay tuned**.<br /><br />
**For newbies, here is a quick start guide:** [http://code.google.com/p/imsdroid/wiki/Quick\_Start](http://code.google.com/p/imsdroid/wiki/Quick_Start)
<br /><br />

<table cellpadding='3'>
<tr>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0//screenshots/visio.png' /></td>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0//screenshots/visio_conf.png' /></td>
</tr>
<tr>
<td align='center'><b>Video Call Screen (H.264 Base Profile 3.0)</b></td>
<td align='center'><b>4-way video conference call using <a href='http://code.google.com/p/openvcs/'>OpenVCS</a></b></td>
</tr>
<tr>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0/screenshots/screen_content_share.png' /></td>
<td><img src='http://imsdroid.googlecode.com/svn/branches/1.0//screenshots/screen_contacts.png' /></td>
</tr>
<tr>
<td align='center'><b>GSMA Content Sharing</b></td>
<td align='center'><b>Enhanced Address Book</b></td>
</tr>
</table>
<br />

## Quick Start ##
  * Getting Started: [http://code.google.com/p/imsdroid/wiki/Quick\_Start](http://code.google.com/p/imsdroid/wiki/Quick_Start)
  * Configure the client for:
    1. [Freephonie](Freephonie.md)
    1. [iptel.org](iptel_org.md)
    1. [pbxes.org](pbxes_org.md)
    1. [sip2sip.info](sip2sip_info.md)
    1. [ekiga.net](ekiga_org.md)
    1. ... To be continued

## Highlights ##
  * SIP(RFC 3261, 3GPP TS 24.229 Rel-9)
  * TCP and UDP over IPv4 or IPv6
  * Signaling Compression, SigComp(RFC 3320, 3485, 4077, 4464, 4465, 4896, 5049, 5112 and 1951)
  * 
  * Enhanced Address Book (XCAP storage, authorizations, presence, ...)
  * Partial supports for [GSMA Rich Communication Suite release 3](http://www.gsmworld.com/our-work/mobile_lifestyle/rcs/index.htm)
  * Partial supports for [One Voice Profile V1.0.0 (GSMA VoLTE)](http://news.vzw.com/OneVoiceProfile.pdf)
  * Partial supports for MMTel UNI (used by GSMA RCS and GSMA VoLTE)
  * 
  * IMS-AKA registration (both AKA-v1 and AKA-v2), Digest MD5, Basic
  * 3GPP Early IMS Security (3GPP TS 33.978)
  * Proxy-CSCF discovery using DNS NAPTR+SRV
  * Private extension headers for 3GPP
  * Service Route discovery
  * Subscription to reg event package (Honoring network initiated (re/de/un)-registration events)
  * 
  * 3GPP SMS Over IP (3GPP TS 23.038, 24.040, 24.011, 24.341 and 24.451)
  * Voice Call (G729AB<sup><a href='http://code.google.com/p/imsdroid/wiki/Building_Source#Building_libtinyWRAP.so_with_G729AB'>1</a></sup>, AMR-NB, iLBC, GSM, PCMA, PCMU, Speex-NB)
  * Video Call (VP8, H264, MP4V-ES, Theora, H.263, H.263-1998, H.261)
  * DTMF (RFC 4733)
  * QoS negotiation using Preconditions (RFC 3312, 4032 and 5027)
  * SIP Session Timers (RFC 4028)
  * Provisional Response Acknowledgments (PRACK)
  * Communication Hold (3GPP TS 24.610)
  * Message Waiting Indication (3GPP TS 24.606)
  * Calling E.164 numbers by using ENUM protocol (RFC 3761)
  * NAT Traversal using STUN2 (RFC 5389) with possibilities to automatically discover the server by using DNS SRV (TURN already implemented and ICE is under tests)

Many other features are supported by the underlying framework but not exposed to the user interface (in progress). For more information please refer to [doubango website](http://doubango.org). <br />These features include: OMA Large IM Message (MSRP), File Transfer (MSRP), Image Sharing (IR.79), Video Sharing (IR.74), TLS and IPSec Security Agreement (RFC 3329), Proxy-CSCF discovery using DHCPv4/v6, TURN, ...

## Request for InterOperability Testing ##
We have started to implement the features listed below and would like to make some IOT. So, if you have a client, IMS Core or Application Server supporting these features, then you are welcome. <br>
<ul><li>Image Sharing (PRD IR.79 Image Share Interoperability Specification 1.0)<br>
</li><li>Video Sharing (PRD IR.74 Video Share Interoperability Specification, 1.0)<br>
</li><li>File Transfer (OMA SIMPLE IM 1.0)<br>
</li><li>Explicit Communication Transfer (ECT) using IP Multimedia (IM) Core Network (CN) subsystem (3GPP TS 24.629)<br>
</li><li>IP Multimedia Subsystem (IMS) emergency sessions (3GPP TS 23.167)</li></ul>

<h2>GSMA RCS</h2>
<a href='http://doubango.org'>doubango</a> partially support <a href='http://www.gsmworld.com/our-work/mobile_lifestyle/rcs/gsma_rcs_project.htm'>GSMA RCS</a> as defined in release 3. The core features will be fully implemented in the next major release.<br>
<br>
<h2>One Voice Profile (GSMA VoLTE)</h2>
Some features of the <a href='http://news.vzw.com/OneVoiceProfile.pdf'>One Voice Profile</a> are implemented in this version (v1.0.0) and the other will be added in the coming releases.<br /><br />
<img src='http://imsdroid.googlecode.com/svn/trunk/screenshots/LTE_Architecture.png' />
<br /><br />
Already implemented: <br />
<ul><li>5.2.1 SIP Registration Procedures<br>
</li><li>5.2.2 Authentication<br>
</li><li>5.2.3 Addressing<br>
</li><li>5.2.4 Call establishment and termination<br>
</li><li>5.2.6 Tracing of Signalling<br>
</li><li>5.2.7 The use of Signalling Compression<br>
</li><li>5.3 Supplementary services (Communication Hold 3GPP TS 24.610, Message Waiting Indication 3GPP TS 24.606, Communication Barring 3GPP TS 24.611)<br>
</li><li>5.4.1 SIP Precondition Considerations<br>
</li><li>5.4.4 Multimedia Considerations<br>
</li><li>5.5 SMS over IP<br>
</li><li>6.2.1 Codecs<br>
</li><li>6.2.5 AMR Payload Format Considerations</li></ul>

<br />
<br />
<b>© 2010-2012 Doubango Telecom</b> <br />
<i>Inspiring the future</i>