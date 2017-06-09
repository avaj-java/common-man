package jaemisseo.man

import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: sujkim
 * Date: 10/28/16
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
class SocketManTest {

    SocketMan socketMan
    String ip
    int port

    @Before
    void init(){
        socketMan = new SocketMan()
        ip = "127.0.0.1"
//        ip = "192.168.0.55"
        port = 5000
    }



    @Test
    @Ignore
    void startServer(){
        new SocketMan()
                .setCharset('euc-kr')
                .server(port){ socketMan ->
                    int recevableBytes = 3072
                    Socket socket = socketMan.socket
                    String charset = socketMan.charset
                    InputStream is = socket.getInputStream()
                    int len = -1
                    int data_size = 0
                    byte[] data = new byte[recevableBytes]
                    byte[] realBytes

                    // Receive
                    try{
                        while ((len = is.read()) > 0) {
                            data[data_size] = (byte)len
                            data_size++
                            // 개행이면 while문을 빠져나감.
                            if (len == 0x0a) {
                                break
                            }
                        }
                    }catch(Exception e){
//                        e.printStackTrace()
                    }finally{
                        if (data_size){
                            realBytes = new byte[data_size]
                            (0..data_size -1).each{
                                realBytes[it] = data[it]
                            }
                        }
                    }

                    if (realBytes){
                        String msg = (charset) ? new String(realBytes, charset) : new String(realBytes)
                        println "//////////////////////////////////////////////////"
                        println "- SIZE:  ${realBytes.length} byte"
                        println "- MESSAGE:"
                        println msg
                    }

                }
    }



    @Test
    @Ignore
    void startEchoServer(){
        new SocketMan()
            .setCharset('euc-kr')
            .echoServer(port){
                String msg = it.receivedMsg
                String charset = it.charset
                println "LENGTH:${msg.length()}\nLENGTH(byte):${msg.getBytes(charset).length}\n${msg}"
            }
    }



    @Test
    @Ignore
    void "스레드 따로 서버 가동하기"(){
        new SocketMan()
                .setModeIndependent(true)
                .echoServer(port)
        while(true){}
    }



    @Test
    @Ignore
    void "간단한 메세지 전송"(){
        if (true) return

        String codeRule = '${A(1)}호이${B(10).right()}${C(5).left()}dddddddjohnubnjn하하하 ijd하하           '
        Map codeMap = [A: 'aaa', B: 'bbb', C: 'ccc' ]
        String msg = new VariableMan().parse(codeRule, codeMap)

        new SocketMan()
                .setCharset('euc-kr')
                .connect(ip, port)
                .send(msg)
    }

}
