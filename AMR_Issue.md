The AMR codec plugin is based on [opencore-amr](http://sourceforge.net/projects/opencore-amr/) which is licensed under **The Apache License V2.0**. However, AFAIK AMR itself is a patented audio data compression (by VoiceAge Corporation) and you should pay a fee in order to use the encoder (not the case for the decoder). Most Android devices natively include AMR Narrow band encoder/decoder and the question is: **Is it possible to use AMR codec in a free project running on Android device?**<br /><br />
As I'm not a lawyer, I decided to deactivated the AMR encoder/decoder in the application. <br />
If you want to use AMR, it's up to you to activate the plugin as follow:<br />
For information, codecs are declared like this: <br />
```
typedef enum tdav_codec_id_e
{
	tdav_codec_id_amr_nb_oa = 0x00000001<<0,
	tdav_codec_id_amr_nb_be = 0x00000001<<1,
	tdav_codec_id_amr_wb_oa = 0x00000001<<2,
	tdav_codec_id_amr_wb_be = 0x00000001<<3,
	tdav_codec_id_gsm = 0x00000001<<4,
	tdav_codec_id_pcma = 0x00000001<<5,
	tdav_codec_id_pcmu = 0x00000001<<6,
	tdav_codec_id_ilbc = 0x00000001<<7,
	tdav_codec_id_speex_nb = 0x00000001<<8,
	tdav_codec_id_speex_wb = 0x00000001<<9,
	tdav_codec_id_speex_uwb = 0x00000001<<10,
	tdav_codec_id_bv16 = 0x00000001<<11,
	tdav_codec_id_bv32 = 0x00000001<<12,
	tdav_codec_id_evrc = 0x00000001<<13,
	
	/* room for new Audio codecs */
	
	tdav_codec_id_h261 = 0x00010000<<0,
	tdav_codec_id_h263 = 0x00010000<<1,
	tdav_codec_id_h263p = 0x00010000<<2,
	tdav_codec_id_h263pp = 0x00010000<<3,
	tdav_codec_id_h264_bp10 = 0x00010000<<4,
	tdav_codec_id_h264_bp20 = 0x00010000<<5,
	tdav_codec_id_h264_bp30 = 0x00010000<<6,
	tdav_codec_id_theora = 0x00010000<<7,

}
tdav_codec_id_t;
```
1- Open the configuration file (under **/data/data/org.doubango.imsdroid/configuration.xml**)<br />
2- Add (if missing) a **MEDIA** section with a **CODECS** entry like this: <br />
```
   <section name="MEDIA">
      <entry key="CODECS" value="32"/>
   </section>
```
**32** is just an example and you can have any intefger (from 0 to 0xFFFFFFFF). As mentioned above, AMR-NB-OA codec value is equal to **1**(0x00000001<<0) and AMR-NB-BE to **2** (0x00000001<<1). To activate both AMR-NB-OA and AMR-NB-BE you should do a binary **OR** on the **CODECS**'s **value** which give you **35** (32 | 1 | 2).