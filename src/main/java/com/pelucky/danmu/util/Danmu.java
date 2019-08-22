package com.pelucky.danmu.util;

import java.security.MessageDigest;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pelucky.danmu.thread.KeepaliveSender;
import com.pelucky.danmu.thread.ReceiveData;

public class Danmu {
    TcpSocketClient tcpSocketClient;
    private TcpSocketClient tcpSocketClientAuth;
    private Logger logger = LoggerFactory.getLogger(Danmu.class);
    private KeepaliveSender keepaliveSender;
    private ReceiveData receiveData;
    private ReceiveData receiveDataAuth;
    private String roomID;
    private String username;
    private String ltkid;
    private String stk;

    public Danmu(String danmu_server, int danmu_port, String auth_server, int auth_port, String roomID, String username, String ltkid, String stk) {
        tcpSocketClient = new TcpSocketClient(danmu_server, danmu_port);
        keepaliveSender = new KeepaliveSender(tcpSocketClient);
        receiveData = new ReceiveData(tcpSocketClient);

        tcpSocketClientAuth = new TcpSocketClient(auth_server, auth_port);
        receiveDataAuth = new ReceiveData(tcpSocketClientAuth);
        this.roomID = roomID;
        this.username = username;
        this.ltkid = ltkid;
        this.stk = stk;
    }

    public void start() {
        receiveData();
        tcpSocketClient.sendData("type@=loginreq/roomid@=" + roomID + "/");
        tcpSocketClient.sendData("type@=joingroup/rid@=" + roomID + "/gid@=-9999/");
        sendKeepalive();
        logger.info("Danmu start succefully!");
    }

    private void sendKeepalive() {
        Thread thread = new Thread(keepaliveSender);
        thread.setName("DanmuServerKeepaliveThread");
        thread.start();
    }

    private void receiveData() {
        Thread thread = new Thread(receiveData);
        thread.setName("DanmuServerReceiveThread");
        thread.start();
    }

    /**
     *
     * Auth server, The
     */
    public void authDanmu() {
        // Auth server's receive data
        /*Thread thread = new Thread(receiveDataAuth);
        thread.setName("AuthServerReceiveThread");
        thread.start();*/

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String vk = MD5Util.MD5(timestamp + "7oE9nPEG9xXV69phU31FYCLUagKeYtsF" + uuid);
        // String vk = MD5Util.MD5(timestamp +
        // "r5*^5;}2#\\${XF[h+;'./.Q'1;,-]f'p[" + uuid);// vk参数

        String loginreqInfo1 = "type@=loginreq/username@=" + username + "/ct@=0/password@=/roomid@=" + roomID
                + "/devid@=" + uuid + "/rt@=" + timestamp + "/vk@=" + vk + "/ver@=20150929/aver@=2017073111/ltkid@="
                + ltkid + "/biz@=1/stk@=" + stk + "/";
        String loginreqInfo = "type@=loginreq/roomid@=" + roomID + "/dfl@=sn@AA=105@ASss@AA=1/username@=" + username
                + "/password@=/ltkid@=/biz@=1/stk@=/devid@=b95c14c6362cc27a7188576d00021501/ct@=0/pt@=2/rt@=/vk@=" + vk
                + "/ver@=20180222/aver@=218101901/";
        //"type@=loginreq/roomid@=2561707/dfl@=sn@AA=105@ASss@AA=1/username@=sdadsda/password@=/ltkid@=/biz@=1/stk@=/devid@=b95c14c6362cc27a7188576d00021501/ct@=0/pt@=2/rt@=/vk@=1989377f39fa69918f6f7c52ba23107a/ver@=20180222/aver@=218101901/"

        tcpSocketClientAuth.sendData(loginreqInfo);

        // Auth server's keepalive
        /*Thread keepAliveThread = new Thread(new KeepaliveSender(tcpSocketClientAuth));
        keepAliveThread.setName("AuthServerReceiveThread");
        keepAliveThread.start();*/
    }

    public void sendDanmu(String message) {
        message = DouyuProtocolMessage.encodeMessage(message);
        logger.info("Send message: {}", message);
        String data = "content@=" + message+ "/col@=0/type@=chatmessage/dy@=/sender@=376763/ifs@=0/nc@=0/rev@=0/admzq@=0/cst@=1547137729931/";
        tcpSocketClientAuth.sendData(data);
    }
    //"content@=111111111/col@=0/type@=chatmessage/dy@=/sender@=376763/ifs@=0/nc@=0/rev@=0/admzq@=0/cst@=1547137729931/"
}

/**
 * https://github.com/brucezz/DouyuCrawler
 */
class MD5Util {
    public static String MD5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            byte[] md = mdInst.digest(s.getBytes());
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte b : md) {
                str[k++] = hexDigits[b >>> 4 & 0xf];
                str[k++] = hexDigits[b & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
