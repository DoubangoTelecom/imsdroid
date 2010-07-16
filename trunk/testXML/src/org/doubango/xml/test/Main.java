package org.doubango.xml.test;


/*
 * @XmlValue ==> @Text
 * @XmlAttribute ==> @Attribute
 * @XmlElement ===> @Element
 * namespace=... ==> @Namespace(reference="....")
 */

import oma.xml.xdm.xcap_directory.TestXcapDirectory;
import ietf.params.xml.ns.pidf.TestPidf;
import ietf.params.xml.ns.rls_services.TestRLS;
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
        //TestXcapCaps.run();
        
        /* OMA xcap-directory */
        //TestXcapDirectory.run();
        
        /* rls-services */
        //TestRLS.run();
        /* resoource-lists */
        //TestResourceLists.run();
        
        /* reginfo */
        //TestReginfo.run();
        
        /* contacts */
        //TestContacts.run();
        
        /* Pidf */
        TestPidf.run();
    }
}