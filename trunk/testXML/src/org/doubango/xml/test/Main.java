package org.doubango.xml.test;


/*
 * @XmlValue ==> @Text
 * @XmlAttribute ==> @Attribute
 * @XmlElement ===> @Element
 * namespace=... ==> @Namespace(reference="....")
 */

import ietf.params.xml.ns.xcap_caps.TestXcapCaps;
import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* xcap-caps */
        TestXcapCaps.run();
        
        /* resoource-lists */
        //TestResourceLists.run();
        
        /* reginfo */
        //TestReginfo.run();
        
        /* contacts */
        //TestContacts.run();
    }
}