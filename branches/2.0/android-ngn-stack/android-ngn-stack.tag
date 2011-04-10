<?xml version='1.0' encoding='ISO-8859-1' standalone='yes' ?>
<tagfile>
  <compound kind="page">
    <name>index</name>
    <title>Foreword</title>
    <filename>index</filename>
  </compound>
  <compound kind="page">
    <name>Introduction</name>
    <title></title>
    <filename>_introduction</filename>
  </compound>
  <compound kind="page">
    <name>page__Setting_Up_NGN_project</name>
    <title>Setting up NGN project</title>
    <filename>page___setting__up__n_g_n_project</filename>
    <docanchor file="page___setting__up__n_g_n_project">anchor_Setting_Up_NGN_project</docanchor>
  </compound>
  <compound kind="page">
    <name>Architecture</name>
    <title></title>
    <filename>_architecture</filename>
  </compound>
  <compound kind="page">
    <name>NgnBaseService_page</name>
    <title>Base Service</title>
    <filename>_ngn_base_service_page</filename>
  </compound>
  <compound kind="page">
    <name>NgnContactService_page</name>
    <title>Contact Service</title>
    <filename>_ngn_contact_service_page</filename>
  </compound>
  <compound kind="page">
    <name>NgnHttpClientService_page</name>
    <title>HTTP/HTTPS Service</title>
    <filename>_ngn_http_client_service_page</filename>
  </compound>
  <compound kind="page">
    <name>NgnNetworkService_page</name>
    <title>Network Service</title>
    <filename>_ngn_network_service_page</filename>
  </compound>
  <compound kind="page">
    <name>NgnSoundService_page</name>
    <title>Sound Service</title>
    <filename>_ngn_sound_service_page</filename>
  </compound>
  <compound kind="page">
    <name>NgnStorageService_page</name>
    <title>Storage Service</title>
    <filename>_ngn_storage_service_page</filename>
  </compound>
  <compound kind="page">
    <name>NgnConfigurationService_page</name>
    <title>Configuration Service</title>
    <filename>_ngn_configuration_service_page</filename>
  </compound>
  <compound kind="page">
    <name>NgnHistoryService_page</name>
    <title>History Service</title>
    <filename>_ngn_history_service_page</filename>
  </compound>
  <compound kind="page">
    <name>NgnSipService_page</name>
    <title>SIP/IMS Service</title>
    <filename>_ngn_sip_service_page</filename>
    <docanchor file="_ngn_sip_service_page">anchor_Listening_for_audio_video_call_state</docanchor>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::NgnApplication</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1_ngn_application.html</filename>
    <member kind="function" static="yes">
      <type>static Context</type>
      <name>getContext</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_application.html</anchorfile>
      <anchor>a09e86a7f24b136873a02f73614b31fc5</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static int</type>
      <name>getSDKVersion</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_application.html</anchorfile>
      <anchor>a6019a9b1600b1979bda5d41babcb1d6b</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static boolean</type>
      <name>useSetModeToHackSpeaker</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_application.html</anchorfile>
      <anchor>ae6071d946afb072b9a820419ee729bab</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static boolean</type>
      <name>isSamsung</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_application.html</anchorfile>
      <anchor>abf3148388fef3be8a33875de8f369753</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static boolean</type>
      <name>isHTC</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_application.html</anchorfile>
      <anchor>ae24fbbbe2ea3b71951012bb34e236a6b</anchor>
      <arglist>()</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::NgnEngine</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</filename>
    <member kind="function">
      <type>synchronized boolean</type>
      <name>start</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>aeffba43e02b7ed5176f4c94d64c9be1c</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>synchronized boolean</type>
      <name>stop</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a1cfd5ad6fa6290f25a243349c9dfb16b</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>synchronized boolean</type>
      <name>isStarted</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a36fe1062c5169305358fb7e86ae7463f</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setMainActivity</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a4f79b2f5a3c863d7ae86c3039354a952</anchor>
      <arglist>(Activity mainActivity)</arglist>
    </member>
    <member kind="function">
      <type>Activity</type>
      <name>getMainActivity</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a2b74d2ea285ccbd4b4bf61bed65381eb</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>INgnConfigurationService</type>
      <name>getConfigurationService</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>ae7852f0cea9f8453f0de0eda9472e8de</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>INgnStorageService</type>
      <name>getStorageService</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>aa514a0ff965c81cc2c28ae10de03124f</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>INgnNetworkService</type>
      <name>getNetworkService</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>ad583127962e81c35b258614dc7c6b3bc</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>INgnHttpClientService</type>
      <name>getHttpClientService</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a03059fb870cfff0c4191b0754081746a</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>INgnContactService</type>
      <name>getContactService</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a9208e8d48f638dc31073f133ac782d9e</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>INgnHistoryService</type>
      <name>getHistoryService</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a2a2cddc84ad30c5048585484f7b1ec0c</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>INgnSipService</type>
      <name>getSipService</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a35fe3391b613e63147bd8688c2df4c26</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>INgnSoundService</type>
      <name>getSoundService</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>afab10caf3158171731e3144eee2eda97</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>Class&lt;?extends NgnNativeService &gt;</type>
      <name>getNativeServiceClass</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>ae016a84bd969a618d4a8784c0a8cf99e</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static NgnEngine</type>
      <name>getInstance</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>aecd6dde60106e3cc2d80f4a8c94bdeac</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" protection="protected">
      <type></type>
      <name>NgnEngine</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1_ngn_engine.html</anchorfile>
      <anchor>a05a0e598efe88666dff053ff5240cab1</anchor>
      <arglist>()</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::NgnNativeService</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1_ngn_native_service.html</filename>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::events::NgnEventArgs</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1events_1_1_ngn_event_args.html</filename>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::events::NgnInviteEventArgs</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1events_1_1_ngn_invite_event_args.html</filename>
    <base>org::doubango::ngn::events::NgnEventArgs</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::events::NgnStackEventArgs</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1events_1_1_ngn_stack_event_args.html</filename>
    <base>org::doubango::ngn::events::NgnEventArgs</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::events::NgnStringEventArgs</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1events_1_1_ngn_string_event_args.html</filename>
    <base>org::doubango::ngn::events::NgnEventArgs</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::media::NgnProxyAudioConsumer</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1media_1_1_ngn_proxy_audio_consumer.html</filename>
    <base>org::doubango::ngn::media::NgnProxyPlugin</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::media::NgnProxyAudioProducer</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1media_1_1_ngn_proxy_audio_producer.html</filename>
    <base>org::doubango::ngn::media::NgnProxyPlugin</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::media::NgnProxyPlugin</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1media_1_1_ngn_proxy_plugin.html</filename>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::media::NgnProxyVideoProducer</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1media_1_1_ngn_proxy_video_producer.html</filename>
    <base>org::doubango::ngn::media::NgnProxyPlugin</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::model::NgnContact</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</filename>
    <member kind="function">
      <type></type>
      <name>NgnContact</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</anchorfile>
      <anchor>a4dc0646d6ed7aca479ca72d9c50ec2e2</anchor>
      <arglist>(int id, String displayName)</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>getId</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</anchorfile>
      <anchor>a7feb28bd158a62cedb9a2573bea84598</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>List&lt; NgnPhoneNumber &gt;</type>
      <name>getPhoneNumbers</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</anchorfile>
      <anchor>a4a0a773a36190d27f7912be3b6039596</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>String</type>
      <name>getPrimaryNumber</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</anchorfile>
      <anchor>a88922408710a5df8c5ffb3a7384ff4bb</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>addPhoneNumber</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</anchorfile>
      <anchor>a19af42e492f421e19238c6e77af86750</anchor>
      <arglist>(PhoneType type, String number, String description)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setDisplayName</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</anchorfile>
      <anchor>a2145fcf4c948e7eb82452fa434732306</anchor>
      <arglist>(String displayName)</arglist>
    </member>
    <member kind="function">
      <type>String</type>
      <name>getDisplayName</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</anchorfile>
      <anchor>a15abcf877eccbdbb3e66b00b4b43bf69</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>Bitmap</type>
      <name>getPhoto</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1model_1_1_ngn_contact.html</anchorfile>
      <anchor>ae034fd36a9b0e0256c0651b11c2a248f</anchor>
      <arglist>()</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::services::impl::NgnBaseService</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1services_1_1impl_1_1_ngn_base_service.html</filename>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::services::impl::NgnContactService</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1services_1_1impl_1_1_ngn_contact_service.html</filename>
    <base>org::doubango::ngn::services::impl::NgnBaseService</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::services::impl::NgnHttpClientService</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1services_1_1impl_1_1_ngn_http_client_service.html</filename>
    <base>org::doubango::ngn::services::impl::NgnBaseService</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::services::impl::NgnNetworkService</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1services_1_1impl_1_1_ngn_network_service.html</filename>
    <base>org::doubango::ngn::services::impl::NgnBaseService</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::services::impl::NgnSoundService</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1services_1_1impl_1_1_ngn_sound_service.html</filename>
    <base>org::doubango::ngn::services::impl::NgnBaseService</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::services::impl::NgnStorageService</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1services_1_1impl_1_1_ngn_storage_service.html</filename>
    <base>org::doubango::ngn::services::impl::NgnBaseService</base>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::sip::NgnAVSession</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</filename>
    <base>org::doubango::ngn::sip::NgnInviteSession</base>
    <member kind="function">
      <type>boolean</type>
      <name>makeCall</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a163fec0e7dc72e5c21916c5a58ed75f6</anchor>
      <arglist>(String remoteUri)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>makeVideoSharingCall</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>aeb159b6609a67ff284214d22539962f0</anchor>
      <arglist>(String remoteUri)</arglist>
    </member>
    <member kind="function">
      <type>Context</type>
      <name>getContext</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a5d9e247dec38e0833f823e9ed1e28a2e</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setContext</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>adc1fdbe70ddfa9e848c235aba8e00e57</anchor>
      <arglist>(Context context)</arglist>
    </member>
    <member kind="function">
      <type>final View</type>
      <name>startVideoConsumerPreview</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a66c9ef6f9c085f7464e1ed98824e3714</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>final View</type>
      <name>startVideoProducerPreview</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>ac4ebc7c1515e4497cb1b86606a07039c</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>isSendingVideo</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a05557c303fe7c1596dad5019032d861e</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>toggleCamera</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>aa85aadb8bcf27343214e1c49a8d78a3c</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setRotation</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a8f68c124aa0a8b5ac9b7d14e6148e3ad</anchor>
      <arglist>(int rot)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setSpeakerphoneOn</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a900b48afb9d994bf6311f610a37d5838</anchor>
      <arglist>(boolean speakerOn)</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>toggleSpeakerphone</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>ae7a00f1360e525e682241ae978ffc3e7</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setState</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>abe0b50ecfca4bcf4ccd5768ae2670e64</anchor>
      <arglist>(InviteState state)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>acceptCall</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>aed6d8a982a10151b824650695ee2c8e2</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>hangUpCall</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>ab47eaebcd62e2b53cb67a3d6a4726f64</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>holdCall</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a742a7d41a71a6c79664819270b29cb2d</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>resumeCall</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>ae184e653a90a872aafdb7f8fc8c7856f</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>isLocalHeld</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>aaf60faf805b163b06365fe137010b560</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>isRemoteHeld</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>af8289eb703453826eda637b39f12ccff</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>sendDTMF</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a3c70ab840a54b9d127588fa723bc06f5</anchor>
      <arglist>(int digit)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static NgnAVSession</type>
      <name>createOutgoingSession</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>afc875ec5c10ac78c91a866f3c79ebb3e</anchor>
      <arglist>(NgnSipStack sipStack, NgnMediaType mediaType)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static NgnAVSession</type>
      <name>getSession</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>af90d8b1a50decc51d45d149ce2193801</anchor>
      <arglist>(long id)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static int</type>
      <name>getSize</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>ab81c9c8cb2973cac7d971600df196e0c</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static boolean</type>
      <name>hasSession</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a5d45179cb8602dbd514f4746501646dc</anchor>
      <arglist>(long id)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static boolean</type>
      <name>hasActiveSession</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a78c91b9ca55c8dfbdab104f9a4a8c2ed</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static NgnAVSession</type>
      <name>getFirstActiveCallAndNot</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a1e87ddd0ace3d35ca5a629af1b871e9f</anchor>
      <arglist>(long id)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static boolean</type>
      <name>makeAudioCall</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a3ea12cbb012c4f07c7de931ccfcefe1b</anchor>
      <arglist>(String remoteUri, NgnSipStack sipStack)</arglist>
    </member>
    <member kind="function" static="yes">
      <type>static boolean</type>
      <name>makeAudioVideoCall</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_a_v_session.html</anchorfile>
      <anchor>a0d02b86a241b29c126cfe8f8dc8aeddc</anchor>
      <arglist>(String remoteUri, NgnSipStack sipStack)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::sip::NgnInviteSession</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_invite_session.html</filename>
    <base>org::doubango::ngn::sip::NgnSipSession</base>
    <member kind="function">
      <type></type>
      <name>NgnInviteSession</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_invite_session.html</anchorfile>
      <anchor>adf180a5fb645e6f8f26066b17d236abd</anchor>
      <arglist>(NgnSipStack sipStack)</arglist>
    </member>
    <member kind="function">
      <type>NgnMediaType</type>
      <name>getMediaType</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_invite_session.html</anchorfile>
      <anchor>a04223f3aa0951d1bbc5458393a7be070</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>InviteState</type>
      <name>getState</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_invite_session.html</anchorfile>
      <anchor>a402d242a552e6b50a31f581e677d4b3f</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setState</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_invite_session.html</anchorfile>
      <anchor>abe0e01a08421a9770947f8c7511895ee</anchor>
      <arglist>(InviteState state)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>isActive</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_invite_session.html</anchorfile>
      <anchor>a0252be0d24903696e8bc63d99bf8064a</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>MediaSessionMgr</type>
      <name>getMediaSessionMgr</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_invite_session.html</anchorfile>
      <anchor>aa6372f171c51b01f885d0bf9336031fb</anchor>
      <arglist>()</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::sip::NgnMessagingSession</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_messaging_session.html</filename>
    <base>org::doubango::ngn::sip::NgnSipSession</base>
    <member kind="function">
      <type>boolean</type>
      <name>SendBinaryMessage</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_messaging_session.html</anchorfile>
      <anchor>afde135a98a91bc9cb4122d5e7083387d</anchor>
      <arglist>(String text, String SMSC)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>sendTextMessage</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_messaging_session.html</anchorfile>
      <anchor>ae906198963a3bfd30dda4e8c84e4a3b9</anchor>
      <arglist>(String text)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>accept</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_messaging_session.html</anchorfile>
      <anchor>abaae30a16b0487fe8397f3971989be66</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>reject</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_messaging_session.html</anchorfile>
      <anchor>a733172b5de1c90845f919750c6a0c765</anchor>
      <arglist>()</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::sip::NgnRegistrationSession</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_registration_session.html</filename>
    <base>org::doubango::ngn::sip::NgnSipSession</base>
    <member kind="function">
      <type></type>
      <name>NgnRegistrationSession</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_registration_session.html</anchorfile>
      <anchor>a341995f23a88dd8b77b932b5d6b151f3</anchor>
      <arglist>(NgnSipStack sipStack)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>register</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_registration_session.html</anchorfile>
      <anchor>a153cbb9cb31b81c36aa23a1134fe2235</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>unregister</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_registration_session.html</anchorfile>
      <anchor>aaa90551271171f377ada50d4f53f8a3c</anchor>
      <arglist>()</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::sip::NgnSipSession</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</filename>
    <member kind="enumeration">
      <name>ConnectionState</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a544e0cc851be4cf264f763a2e42bed29</anchor>
      <arglist></arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>incRef</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a7f923a9fc91ce3548ee59c50cd8cf3cc</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>int</type>
      <name>decRef</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>ada9c616d6dcda7ef7d834f2344f6e1e1</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>long</type>
      <name>getId</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a45d11507138ebe0c7c7666447548f5d9</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>NgnSipStack</type>
      <name>getStack</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>ae97b707766002faaa1db3a67198c581f</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>addHeader</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>ad580c16020f2903f35358aa02c86b5f1</anchor>
      <arglist>(String name, String value)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>removeHeader</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>ae862aff8e978eac813fa91a08ddafecf</anchor>
      <arglist>(String name)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>addCaps</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a7641e1f8b7ae88a596ba5d38a2abac96</anchor>
      <arglist>(String name)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>addCaps</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>adc691b9f960b04f634657327b84b9cba</anchor>
      <arglist>(String name, String value)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>removeCaps</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a4ea07fe092c5852815e632808abfb3bf</anchor>
      <arglist>(String name)</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>isConnected</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a01414595cc66083877c589c647f6b3cf</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>void</type>
      <name>setConnectionState</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a9c24d916441ba483c1fd2ca9a990b600</anchor>
      <arglist>(ConnectionState state)</arglist>
    </member>
    <member kind="function">
      <type>ConnectionState</type>
      <name>getConnectionState</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a09103935272ae59c02884d3099a4196f</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>String</type>
      <name>getFromUri</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a212aa71a69464dd7b904ffd10a639453</anchor>
      <arglist>()</arglist>
    </member>
    <member kind="function">
      <type>boolean</type>
      <name>setFromUri</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>a9ee0fb16ba42489cf68e7b0609d04816</anchor>
      <arglist>(String uri)</arglist>
    </member>
    <member kind="function" protection="protected">
      <type></type>
      <name>NgnSipSession</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_session.html</anchorfile>
      <anchor>adb655805312e4a16ecaf0f476d867689</anchor>
      <arglist>(NgnSipStack sipStack)</arglist>
    </member>
  </compound>
  <compound kind="class">
    <name>org::doubango::ngn::sip::NgnSipStack</name>
    <filename>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_stack.html</filename>
    <member kind="function">
      <type></type>
      <name>NgnSipStack</name>
      <anchorfile>classorg_1_1doubango_1_1ngn_1_1sip_1_1_ngn_sip_stack.html</anchorfile>
      <anchor>ac75d9eeb6f53cd03ad3df8028270d5c7</anchor>
      <arglist>(SipCallback callback, String realmUri, String impiUri, String impuUri)</arglist>
    </member>
  </compound>
</tagfile>
