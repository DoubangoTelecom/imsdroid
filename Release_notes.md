

## 2.548.870 ##
  * Align with Doubango 870
  * Adds support for ZeroVideoArtifacts (Options -> QoS/QoE)
  * Improve interop with other WebRTC endpoints (mainly chrome and Firefox)
  * Improve interop with other h264-rtp implementations (e.g. gstreamer, bria, cisco, polycom, lync...)
  * Improve video perfs
  * Re-design the video jitter buffer
  * List of devices with opensl-es enabled: "galaxy nexus", "gt-i9100", "gt-s5570i", "xt890", "lg-p970"
  * Many other bug fixes

## 2.544.836 ##
  * Align with Doubango 836 (DTLS-SRTP)

## 2.532.758 ##
  * Align with Doubango 758
  * Update media engine to allow interop with Chrome stable "23.0.1271.64 m" using sipml5
  * Adds support for realtime text (T.140)
  * Fix [issue 391](https://code.google.com/p/imsdroid/issues/detail?id=391), [issue 277](https://code.google.com/p/imsdroid/issues/detail?id=277), [issue 296](https://code.google.com/p/imsdroid/issues/detail?id=296), [issue 313](https://code.google.com/p/imsdroid/issues/detail?id=313), [issue 383](https://code.google.com/p/imsdroid/issues/detail?id=383), [issue 381](https://code.google.com/p/imsdroid/issues/detail?id=381) and [issue 375](https://code.google.com/p/imsdroid/issues/detail?id=375)

## 2.0.509 ##
  * Align with Doubango 738
  * Direct redering of YUV420P frame to opengl-es surface using shaders for GPU chroma conversion (yuv420p -> rgba)
  * Use libyuv (http://code.google.com/p/libyuv/) NEON optimized functions for choma conversion and image scaling, rotation, cropping...
  * Video jitter buffer enhancements
  * Fix [issue 228](https://code.google.com/p/imsdroid/issues/detail?id=228)

## 2.0.504 ##
  * Fix on-way video when calling Chrome 21.0.1180.60-m

## 2.0.499 ##
  * Fix [issue 197](https://code.google.com/p/imsdroid/issues/detail?id=197), [issue 188](https://code.google.com/p/imsdroid/issues/detail?id=188) and [issue 82](https://code.google.com/p/imsdroid/issues/detail?id=82)
  * Align with Doubango 715
  * Improve AEC

## 2.0.491 ##
First version fully implementing RTCWeb standards (ICE, SRTP/SRTCP, RTCP-MUX...).<br />
This version can be used to call [sipML5](http://code.google.com/p/sipml5/) webApp<br />
To enable the features: Options -> General -> Media Profile -> RTCWeb

## 2.0.487 ##
  * ICE (Interactive Connectivity Establishment): Full implementation of RFC 5245 for NAT Traversal
  * 1080p (Full HD): all platforms supports full HD video negotiation. Off course it depends on your CPU and network bandwidth. The preferred video size could be changed from the QoS/QoS screen.
  * Adaptive video jitter buffer: A video jitter buffer with advanced features like error correction, packet loss retransmission, delay recovery...
  * RTP/AVPF profile as per RFC 4585
  * RTCP: Full support for RTCP (3550) and many extensions such as: PLI (RFC 4585), SLI (RFC 4585), RPSI (RFC 4585), FIR (RFC 5104), NACK (4585), TMMBN (RFC 5104)...
  * rtcp-mux as per 5761
  * Negotiation of Generic Image Attributes in the SDP as per RFC 6236
  * Source-Specific Media Attributes in SDP as per draft-lennox-mmusic-sdp-source-attributes-01
  * Explicit Call Transfer as per 3GPP TS 24.629
  * Fix [issue 339](https://code.google.com/p/imsdroid/issues/detail?id=339) and [issue 167](https://code.google.com/p/imsdroid/issues/detail?id=167)

## 2.0.484 ##
  * Adds support for SRTP
  * Enable TLS
  * Fix crashes when ending video calls on some devices
  * Adds support for more front facing cameras
  * Fix [issue 331](https://code.google.com/p/imsdroid/issues/detail?id=331)

## 2.0.481 ##
  * Add support for Android ICS (4.0)
  * Align with latest Doubango binaries ([r685](https://code.google.com/p/imsdroid/source/detail?r=685))
  * Fix [issue 278](https://code.google.com/p/imsdroid/issues/detail?id=278), [issue 280](https://code.google.com/p/imsdroid/issues/detail?id=280), [issue 298](https://code.google.com/p/imsdroid/issues/detail?id=298), [issue 290](https://code.google.com/p/imsdroid/issues/detail?id=290), [issue 206](https://code.google.com/p/imsdroid/issues/detail?id=206), [issue 262](https://code.google.com/p/imsdroid/issues/detail?id=262), [issue 312](https://code.google.com/p/imsdroid/issues/detail?id=312), [issue 322](https://code.google.com/p/imsdroid/issues/detail?id=322) and [issue 325](https://code.google.com/p/imsdroid/issues/detail?id=325).

## 2.0.453-vp8 ##
  * First beta version with support for VP8 video codec (AMRv7-a only)

## 2.0.448-webrtc ##
  * Fix [issue 145](https://code.google.com/p/imsdroid/issues/detail?id=145), [issue 184](https://code.google.com/p/imsdroid/issues/detail?id=184), [issue 237](https://code.google.com/p/imsdroid/issues/detail?id=237), [issue 261](https://code.google.com/p/imsdroid/issues/detail?id=261), [issue 268](https://code.google.com/p/imsdroid/issues/detail?id=268), [issue 267](https://code.google.com/p/imsdroid/issues/detail?id=267) and [issue 263](https://code.google.com/p/imsdroid/issues/detail?id=263)

## 2.0.431-preview ##
  * Enable echo cancellation (Thanks to Philippe Verney)
  * Fix issues: [issue 256](https://code.google.com/p/imsdroid/issues/detail?id=256), [issue 203](https://code.google.com/p/imsdroid/issues/detail?id=203), [issue 134](https://code.google.com/p/imsdroid/issues/detail?id=134), [issue 119](https://code.google.com/p/imsdroid/issues/detail?id=119), [issue 213](https://code.google.com/p/imsdroid/issues/detail?id=213) and [issue 250](https://code.google.com/p/imsdroid/issues/detail?id=250)

## 2.0.419-preview ##
  * Improve video quality by adding the notion of "unrestricted" bandwidth
  * Add support for noise suppression (thanks to **speexdsp**)
  * Fix issues: [issue 232](https://code.google.com/p/imsdroid/issues/detail?id=232), [issue 233](https://code.google.com/p/imsdroid/issues/detail?id=233), [issue 230](https://code.google.com/p/imsdroid/issues/detail?id=230), [issue 111](https://code.google.com/p/imsdroid/issues/detail?id=111), [issue 84](https://code.google.com/p/imsdroid/issues/detail?id=84), [issue 135](https://code.google.com/p/imsdroid/issues/detail?id=135), [issue 157](https://code.google.com/p/imsdroid/issues/detail?id=157) and [issue 192](https://code.google.com/p/imsdroid/issues/detail?id=192).

## 2.0.395-preview ##
Based on **Doubango v2.x**.
  * The SIP/IMS Stack (version 2) is 7 times faster
  * NGN (Next Generation Network) stack for developers (android-ngn-stack)
  * Better audio quality (Adaptive jitter buffer, automatic resampling, gain control, ...)
  * Better video quality (low latency, low cpu usage, ...)
  * Multi-line
  * MSRP chat
  * Fix many issues (see logs)

## 1.2.366 ##
  * Fix [issue 158](https://code.google.com/p/imsdroid/issues/detail?id=158), [issue 165](https://code.google.com/p/imsdroid/issues/detail?id=165)
  * Add support for 4-way conference call using [OpenVCS](http://code.google.com/p/openvcs/)

## 1.2.355 ##
  * Fix  [issue 138](https://code.google.com/p/imsdroid/issues/detail?id=138) , [issue 141](https://code.google.com/p/imsdroid/issues/detail?id=141),  [issue 147](https://code.google.com/p/imsdroid/issues/detail?id=147)  and  [issue 148](https://code.google.com/p/imsdroid/issues/detail?id=148)

## 1.2.348 ##
  * Add support for multiple concurrent calls (thanks to http://www.theipcompany.nl/)
  * Allow intercepting outgoing calls
  * Add support for MP4V-ES codec
  * Fix issues

## 1.1.327 ##
  * Fix [issue 21](https://code.google.com/p/imsdroid/issues/detail?id=21), [issue 56](https://code.google.com/p/imsdroid/issues/detail?id=56), [issue 84](https://code.google.com/p/imsdroid/issues/detail?id=84), [issue 96](https://code.google.com/p/imsdroid/issues/detail?id=96), [issue 100](https://code.google.com/p/imsdroid/issues/detail?id=100), [issue 86](https://code.google.com/p/imsdroid/issues/detail?id=86) and [issue 108](https://code.google.com/p/imsdroid/issues/detail?id=108)
  * Add support for iLBC
  * Add support for up to VGA video sizeo
  * Bundle apk (Both ARMv5te and ARMv7-a with cortex-a8)

## 1.0.320 ##
  * Fix [issue 8](https://code.google.com/p/imsdroid/issues/detail?id=8), [issue 81](https://code.google.com/p/imsdroid/issues/detail?id=81), [issue 84](https://code.google.com/p/imsdroid/issues/detail?id=84), [issue 89](https://code.google.com/p/imsdroid/issues/detail?id=89) and [issue 91](https://code.google.com/p/imsdroid/issues/detail?id=91)
  * Add support for G.729
  * Redesign video producer for better performances

## 1.0.302 ##
  * Fix [issue 75](https://code.google.com/p/imsdroid/issues/detail?id=75) (Add support for IPv6)
  * Add support for Front Facing Camera on Dell Streak

## 1.0.298 ##
  * Fix [issue 53](https://code.google.com/p/imsdroid/issues/detail?id=53) and [issue 70](https://code.google.com/p/imsdroid/issues/detail?id=70)

## 1.0.292 ##
  * Fix [issue 55](https://code.google.com/p/imsdroid/issues/detail?id=55), [issue 63](https://code.google.com/p/imsdroid/issues/detail?id=63), [issue 64](https://code.google.com/p/imsdroid/issues/detail?id=64), [issue 61](https://code.google.com/p/imsdroid/issues/detail?id=61) and [issue 65](https://code.google.com/p/imsdroid/issues/detail?id=65)
  * Improve video performance by using API Level 8 functions

## 1.0.285 ##
  * Add support for Front Facing Camera for Huawei u8230, HTC EVO 4G, Samsung Epic 4G and Samsung Galaxy S.
  * Suppress H264 delay ([issue 60](https://code.google.com/p/imsdroid/issues/detail?id=60))
  * Force IDR frames before PPS and SPS ([issue 48](https://code.google.com/p/imsdroid/issues/detail?id=48) and [issue 58](https://code.google.com/p/imsdroid/issues/detail?id=58))
  * Fix [issue 59](https://code.google.com/p/imsdroid/issues/detail?id=59)
  * Decrease Video CPU usage for both Theora and H264

## 1.0.279 ##
  * Fix [issue 43](https://code.google.com/p/imsdroid/issues/detail?id=43) and [issue 52](https://code.google.com/p/imsdroid/issues/detail?id=52).
  * Change H.264 packetization as per RFC 3984

## 1.0.271 ##
  * Fix issues
  * Improve video quality and decrease CPU usage for H263-1998
  * Load contacts from Android native address book
  * Allow calling native contact from dialer screen
  * Build native libraries using latest NDK (version r4b)

## 1.0.263 ##
  * Add support for GSMA Content Sharing (PRD IR. 79)
  * Rebuild FFmpeg with all ARM optimizations
  * Rebuild Speex with ARM5E optimizations
  * Resolve [issue 31](https://code.google.com/p/imsdroid/issues/detail?id=31)
  * Fix bugs

## 1.0.234 ##
  * Allow decoding any video size
  * Add support for high (CIF) and low (SQCIF) video encoding. QCIF was already supported in previous versions.
  * Resolve [issue 12](https://code.google.com/p/imsdroid/issues/detail?id=12), [issue 17](https://code.google.com/p/imsdroid/issues/detail?id=17), [issue 18](https://code.google.com/p/imsdroid/issues/detail?id=18), [issue 19](https://code.google.com/p/imsdroid/issues/detail?id=19), [issue 26](https://code.google.com/p/imsdroid/issues/detail?id=26) and [issue 27](https://code.google.com/p/imsdroid/issues/detail?id=27).

## 1.0.214 (First Beta version) ##