package org.doubango.ngn.utils;

import android.util.Log;

public class NgnNetworkConnection  extends NgnObservableObject implements Comparable<NgnNetworkConnection> {
    private static final String TAG = NgnNetworkConnection.class.getCanonicalName();
    private boolean mUp;
    private boolean mIPv6;
    private String mLocalIP;
    private int mLocalPort;
    private String mProxyHost;
    private int mProxyPort;
    private String mDescription;
    private String mName;
    private String mTransport;
    private String mIPversion;
    private String mAoRIP;
    private int mAoRPort;
    private final int mId;
    private static int gId = 0;

    public NgnNetworkConnection(String name, String transport, String ipversion)
    {
        mName = name;
        mTransport = transport;
        mIPversion = ipversion;
        mDescription = name;
        synchronized (NgnNetworkConnection.class) {
            mId = ++gId;
        }
    }

    @Override
    public String toString() {
        return String.format("id = %d, name = %s, transport = %s, IPversion = %s, Up = %s, useIPv6 = %s, localIP = %s, localPort = %d, ProxyHost = %s, ProxyPort = %d",
                getId(), mName, mTransport, mIPversion, mUp ? "true" : "false", mIPv6 ? "true" : "false", mLocalIP, mLocalPort, mProxyHost, mProxyPort);
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getLocalIP() { return mLocalIP; }

    public String getProxyHost() { return mProxyHost; }

    public boolean isUp() {
        return mUp;
    }

    public boolean isIPv6() {
        return mIPv6;
    }

    public void setUp(boolean bUp) {
        boolean changed = (mUp != bUp);
        mUp = bUp;
        if (changed) {
            super.setChangedAndNotifyObservers(bUp);
        }
    }

    public void setIPv6(boolean bIPv6) {
        boolean changed = (mIPv6 != bIPv6);
        if (changed || mIPversion == null) {
            mIPv6 = bIPv6;
            mIPversion = bIPv6 ? "ipv6" : "ipv4";
            super.setChangedAndNotifyObservers(bIPv6);
        }
    }

    public boolean setTransport(String transport, String IPversion)
    {
        if (mTransport != transport || mIPversion != IPversion) {
            mTransport = transport;
            mIPversion = IPversion;
            super.setChangedAndNotifyObservers(this);
        }
        return true;
    }

    public boolean setDescription(String description)
    {
        mDescription = description;
        return true;
    }

    public boolean setLocalAddress(String local_ip, int local_port)
    {
        if (mLocalIP != local_ip) {
            mLocalIP = local_ip;
            mLocalPort = local_port;
            super.setChangedAndNotifyObservers(this);
        }
        return true;
    }

    public boolean setLocalIP(String local_ip)
    {
        if (mLocalIP != local_ip) {
            mLocalIP = local_ip;
            super.setChangedAndNotifyObservers(local_ip);
        }
        return true;
    }

    public boolean setLocalPort(int local_port)
    {
        if (mLocalPort != local_port) {
            mLocalPort = local_port;
            super.setChangedAndNotifyObservers(local_port);
        }
        return true;
    }

    public boolean setProxyCSCF(String proxy_cscf_ip, int proxy_cscf_port)
    {
        if (mProxyHost != proxy_cscf_ip || mProxyPort != proxy_cscf_port) {
            mProxyHost = proxy_cscf_ip;
            mProxyPort = proxy_cscf_port;
            super.setChangedAndNotifyObservers(this);
        }
        return true;
    }

    public boolean setAoR(String aor_ip, int aor_port)
    {
        if (mAoRIP != aor_ip || mAoRPort != aor_port) {
            mAoRIP = aor_ip;
            mAoRPort = aor_port;
            super.setChangedAndNotifyObservers(this);
        }
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize()");
        delete();
        super.finalize();
    }

    public void delete(){

    }

    @Override
    public int compareTo(NgnNetworkConnection arg0) {
        return (int)(getId() - arg0.getId());
    }

    public static class NgnNetworkConnectionFilterByID implements NgnPredicate<NgnNetworkConnection> {
        private final int mId;
        public NgnNetworkConnectionFilterByID(int id){
            mId = id;
        }
        @Override
        public boolean apply(NgnNetworkConnection connection) {
            return (connection.mId == mId);
        }
    }

    public static class NgnNetworkConnectionFilterByLocalIP implements NgnPredicate<NgnNetworkConnection> {
        private final String mLocalIP;
        public NgnNetworkConnectionFilterByLocalIP(String localIP){
            mLocalIP = localIP;
        }
        @Override
        public boolean apply(NgnNetworkConnection connection) {
            return (connection.mLocalIP == mLocalIP);
        }
    }
    public static class NgnNetworkConnectionFilterByName implements NgnPredicate<NgnNetworkConnection> {
        private final String mName;
        public NgnNetworkConnectionFilterByName(String name){
            mName = name;
        }
        @Override
        public boolean apply(NgnNetworkConnection connection) {
            return mName.equals(connection.mName);
        }
    }

    public static class NgnNetworkConnectionFilterByNameStartsWith implements NgnPredicate<NgnNetworkConnection> {
        private final String mNameStart;
        public NgnNetworkConnectionFilterByNameStartsWith(String nameStart){
            mNameStart = nameStart;
        }
        @Override
        public boolean apply(NgnNetworkConnection connection) { return connection.mName.startsWith(mNameStart); }
    }

    public static class NgnNetworkConnectionFilterByUp implements NgnPredicate<NgnNetworkConnection> {
        private final boolean mUp;
        public NgnNetworkConnectionFilterByUp(boolean up){
            mUp = up;
        }
        @Override
        public boolean apply(NgnNetworkConnection connection) {
            return (connection.mUp == mUp);
        }
    }

    public static class NgnNetworkConnectionFilterByUpAndIPv6 implements NgnPredicate<NgnNetworkConnection> {
        private final boolean mUp;
        private final boolean mIPv6;
        public NgnNetworkConnectionFilterByUpAndIPv6(boolean up, boolean ipv6){
            mUp = up;
            mIPv6 = ipv6;
        }
        @Override
        public boolean apply(NgnNetworkConnection connection) {
            return (connection.mUp == mUp && connection.mIPv6 == mIPv6);
        }
    }

    public static class NgnNetworkConnectionFilterByUpAndIPv6AndNameStartsWith implements NgnPredicate<NgnNetworkConnection> {
        private final boolean mUp;
        private final boolean mIPv6;
        private final String mNameStart;
        public NgnNetworkConnectionFilterByUpAndIPv6AndNameStartsWith(boolean up, boolean ipv6, String nameStart){
            mUp = up;
            mIPv6 = ipv6;
            mNameStart = nameStart;
        }
        @Override
        public boolean apply(NgnNetworkConnection connection) {
            return (connection.mUp == mUp && connection.mIPv6 == mIPv6 && connection.mName.startsWith(mNameStart));
        }
    }
}
