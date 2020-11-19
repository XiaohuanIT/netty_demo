intellij中编译运行时候注意，intellij -- preferences -- Build, Execution, Deployment -- Compile -- Annotation Precossor

Enable annotation processing



一、server
netty_demo_2项目中server_client包
启动EchoServer，可以用postman打请求，也可以用netty_demo_2项目中server_client包的EchoClient打请求。但是如果用postman打请求，那么postman将不会收到正常的response。

TODO 疑问：
（1）这里的协议，是什么协议，为什么使用postman就无法收到正常的response呢？
（2）是不是可以用类似的方式进行tomcat"仿写"？
（3）NIO ？？ 多线程？如何验证




二、http_server
可以接收http请求，启动运行HttpServer之后，可以用浏览器或者postman进行模拟测试。




三、http_server_gzip
netty_demo_2项目的http_server_gzip包
HttpGzipServer启动之后，可以用postman请求，但是postman无法发送gzip压缩数据的请求。可以使用netty_demo_2项目的http_server_gzip包下面的MessageSender.java进行请求。

TODO 疑问：
此server启动后，用netty_demo_2项目中server_client包的EchoClient打请求，server端收不到任何请求，而且client没有任何反应？




四、time_server
netty_demo_2项目中time_client发送请求
TCP粘包/拆包的演示



五、time_server_1
netty_demo_2项目中time_client_1发送请求
LineBasedFrameDecoder + StringDecoder，被设计用来支持TCP的粘包和拆包。
LineBasedFrameDecoder的工作原理是：它依次遍历ByteBuf中的可读字节，判断看是否有"\r", "\r\n"，如果有，就以此位置为结束位置，从可读索引到结束位置区间的字节就组成了一行。它是以换行符为结束标志的解码器，支持
携带结束符和不携带结束符两种解码方式，同时支持配置单行的最大长度。如果连续读取到最大长度后仍然没有出现换行符，就会抛异常，同时忽略掉之前读到的异常码流。

StringDecoder的功能非常简单，就是将收到的对象转换成字符串，然后继续调用后面的Handler。

如果发送的消息不是以换行符结束的，该怎么办？或者没有回车换行符，靠消息头中的长度字段来分包怎么办？是不是需要自己写半包解码器？
答案是否定的，Netty提供了多种支持TCP粘包/拆包的解码器，用来满足用户的不同诉求。



六、time_server_2
netty_demo_2项目中time_client_2发送请求
自定义的分隔符形式。DelimiterBasedFrameDecoder



七、time_server_3
固定长度的消息进行自动解码。FixedLengthFrameDecoder。
通过命令行执行命令 `telnet localhost 9000` ，然后输入任何文字即可。



八、server_1
netty_demo_2项目中server_1_client发送请求

对于粘包/拆包的支持，使用

```
ch.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(65535, 0, 2,0,2));
ch.pipeline().addLast("msgpack decoder", new MsgPackDecoder());
ch.pipeline().addLast("frameEncoder",new LengthFieldPrepender(2));
ch.pipeline().addLast("msgpack encoder", new MsgPackEncoder());
```

来解决。原理是：增加了2个字节的消息长度字段。在MessagePack解码器之前增加LengthFieldBasedFrameDecoder，用于处理半包消息，这样后面的 MsgPackDecoder 接收到的永远是整包消息。



九、10.2 http文件服务器，做实验



10.3  没有看懂



十、11.3 websocket方式开发，做实验




java序列化的目的主要有两个：
1. 网络从传输
2. 对象持久化。
当进行远程跨进程服务调用时，需要把被传输的java对象编码为字节数组或者ByteBuffer对象。而当远程服务读取到字节数组或者ByteBuffer对象时，需要将其解码为发送时的java对象。这被称为java对象编解码技术。



评判编解码框架的优劣时，往往会考虑以下几个因素：
（1）是否支持跨语言，支持的语言种类是否丰富；
（2）编码后的码流大小；
（3）编解码的性能；
（4）类库是否小巧，API使用是否方便；
（5）使用者需要使用手工开发的工作量和难度。



java序列化仅仅是java编解码技术的一种。由于它的种种缺陷，衍生出了多种编解码技术和框架。
（1）无法跨语言。java序列化技术是java语言内部的私有协议，其他语言并不支持。
（2）序列化后的码流太大。编码后的字节数组越大，存储的时候就越占空间，存储的硬件成本就越高，并且在网络传输的就越占宽带，导致系统的吞吐量降低。
（3）序列化性能太低。


业界主流的编解码框架：
（1）google protobuf
xml的可读性和可扩展性非常好，也非常适合描述数据结构，但是xml的解析时间开销和xml为了可读性而牺牲的空间开销都非常大，因此不适合做高性能的通信协议。

google protobuf使用二进制编码，在空间和性能上具有更大优势。

（2）facebook的thrift





服务端给客户端主动推送消息该怎么办呢？这时候传统的HTTP协议，显然已经不能满足我们的需求。缺点太多了。于是有一个新的技术随之而生，叫做websocket




TODO 笔记
4.1.3

netty权威指南 10.1 http协议介绍、11.1部分

长连接、短连接？？？


HTTP协议是建立在TCP传输协议之上的应用层协议。HTTP是一个属于应用层的面向对象的协议，由于其简捷、快速的方式，适用于分布式超媒体信息系统。

Netty的HTTP协议栈是基于Netty的NIO通信框架开发的，因此Netty的HTTP协议也是异步非阻塞的。








Response Headers
content-encoding: gzip

Request Headers
accept-encoding: gzip, deflate


问题：
1. 对于前端请求的压缩的数据，在后端写接口时候并没有对数据进行解压，然后压缩返回，这些解压/压缩的操作是在哪里做的呢？



十一、web_socket
NioWebSocketServer
netty搭建的服务器基本上都是差不多的写法：
- 绑定主线程组和工作线程组，这部分对应架构图中的事件循环组
- 只有服务器才需要绑定端口，客户端是绑定一个地址
- 配置channel（数据通道）参数，重点就是ChannelInitializer的配置
- 以异步的方式启动，最后是结束关闭两个线程组

自定义的处理器NioWebSocketHandler，执行流程是：
- web发起一次类似是http的请求，并在channelRead0方法中进行处理，并通过instanceof去判断帧对象是FullHttpRequest还是WebSocketFrame，建立连接是时候会是FullHttpRequest
- 在handleHttpRequest方法中去创建websocket，首先是判断Upgrade是不是websocket协议，若不是则通过sendHttpResponse将错误信息返回给客户端，紧接着通过WebSocketServerHandshakerFactory创建socket对象并通过handshaker握手创建连接
- 在连接创建好后的所以消息流动都是以WebSocketFrame来体现
- 在handlerWebSocketFrame去处理消息，也可能是客户端发起的关闭指令，ping指令等等

保存客户端的信息
当有客户端连接时候会被channelActive监听到，当断开时会被channelInactive监听到，一般在这两个方法中去保存/移除客户端的通道信息，而通道信息保存在ChannelSupervise中：

