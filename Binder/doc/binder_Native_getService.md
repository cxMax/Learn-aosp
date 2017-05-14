## 获取Media服务

### getMediaPlayerService in IMediaDeathNotifier.cpp

* defaultServiceManager(); //获取ServiceManager  
在请求获取名为”media.player”的服务过程中，采用不断循环获取的方法

* defaultServiceManager -> BpServiceManager.getService()

* checkService() in getService()
remote()->transact , 其中remote为BpBinder.

* BpBinder::transact  ->  remote()->transact
Binder代理类调用transact()方法，真正工作还是交给IPCThreadState来进行transact工作

* IPCThreadState::self()->transact() in BpBinder::transact
self() :  
//初始IPCThreadState  
//创建线程的TLS  

transact():  
writeTransactionData // 传输数据
binder驱动通信的数据结构，该过程最终是把Binder请求码BC_TRANSACTION和binder_transaction_data结构体写入

waitForResponse  //等待响应

* talkWithDriver() in waitForResponse
```
//通过ioctl不停的读写操作，跟Binder Driver进行通信【2.8.1】
        if (ioctl(mProcess->mDriverFD, BINDER_WRITE_READ, &bwr) >= 0)
```
通过ioctl与mDriverFD通信，是真正与Binder驱动进行数据读写交互的过程。
service manager -> do_find_service() 查询服务所对应的handle -> binder_send_reply()应答发送BC_REPLY协议 -> binder_transaction()，再向服务请求者的Todo队列 插入事务

* binder_transaction
1. 当请求服务的进程与服务属于不同进程，则为请求服务所在进程创建binder_ref对象，指向服务进程中的binder_node;
2. 当请求服务的进程与服务属于同一进程，则不再创建新对象，只是引用计数加1，并且修改type为BINDER_TYPE_BINDER或BINDER_TYPE_WEAK_BINDER。

### 死亡通知
为了让Bp端能知道Bn端的生死情况
* 定义：DeathNotifier是继承IBinder::DeathRecipient类，主要需要实现其binderDied()来进行死亡通告
* 注册：binder->linkToDeath(sDeathNotifier)是为了将sDeathNotifier死亡通知注册到Binder上

1. linkToDeath in BpBinder.cpp
-> requestDeathNotification()

2. requestDeathNotification
向binder driver发送BC_REQUEST_DEATH_NOTIFICATION命令

3. binderDied
客户端进程通过Binder驱动获得Binder的代理（BpBinder），死亡通知注册的过程就是客户端进程向Binder驱动注册一个死亡通知，该死亡通知关联BBinder，即与BpBinder所对应的服务端

4. unlinkToDeath
当Bp在收到服务端的死亡通知之前先挂了，那么需要在对象的销毁方法内，调用unlinkToDeath()来取消死亡通知

5. 触发时机
Service Manager 检查BBinder是否有注册死亡通知，当发现存在死亡通知时，那么就向其对应的BpBinder端发送死亡通知消息。

## refer :
http://gityuan.com/2015/11/15/binder-get-service/
