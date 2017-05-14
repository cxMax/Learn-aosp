## Android深入浅出之Binder机制

##代表服务端的MediaService诞生过程
1. Main_mediaserver.cpp的main函数

第一步 : 
* ProcessState::self()
(1) 打开/dev/binder设备，这样的话就相当于和内核binder机制有了交互的通道
(2) 映射fd到内存，设备的fd传进去后，估计这块内存是和binder设备共享的

* defaultServiceManager() in ServiceManager.cpp
 IServiceManager::asInterface(BpBinder);
asInterface(new BpBinder(0))实际返回的是BpServiceManager(new BpBinder(0))；  
 返回的实际是BpServiceManager，它的remote对象是BpBinder

* BpBinder in BpBinder.cpp
(1) ProcessState有了
(2) IPCThreadState有了，而且是主线程的。
(3) BpBinder有了，内部handle值为0

* IServiceManager IInterface
IServiceManager.cpp
addService()

* BpServiceManager

第二步 : MediaPlayerService
* instantiate() in MediaPlayerService.cpp 该函数内部调用addService，把MediaPlayerService信息 add到ServiceManager中
MediaPlayerService从BnMediaPlayerService派生
BnMediaPlayerService 目的是 ServiceManager.addService
和ServiceManager通信是利用BpServiceManager
BpServiceManager发送了一个addService命令到BnServiceManager

第三步 : BnServiceManager
servicemanger.c
ServiceManager把信息加入到自己维护的一个服务列表中了

2. ServiceManager存在的意义
* MediaPlayerService向SM注册
* MediaPlayerClient查询当前注册在SM中的MediaPlayerService的信息
* 根据这个信息，MediaPlayerClient和MediaPlayerService交互

3. MediaService的运行
*  defaultServiceManager得到了BpServiceManager，然后MediaPlayerService 实例化后，调用BpServiceManager的addService函数
*  这个过程中，是service_manager收到addService的请求，然后把对应信息放到自己保存的一个服务list中
*  到这儿，我们可看到，service_manager有一个binder_looper函数，专门等着从binder中接收请求。虽然service_manager没有从BnServiceManager中派生，但是它肯定完成了BnServiceManager的功能。
* 同样，我们创建了MediaPlayerService即BnMediaPlayerService，那它也应该：  打开binder设备 ; 也搞一个looper循环，然后坐等请求 ;

第四步: MediaPlayerService打开binder
在ProcessState中打开binder

第五步 : looper  
BnMediaPlayerService从BBinder派生，所以会调用到它的onTransact函数
BnXXX的onTransact函数收取命令，然后派发到派生类的函数，由他们完成实际的工作

## MediaPlayerClient怎么和MediaPlayerService交互
1. 使用MediaPlayerService的时候，先要创建它的BpMediaPlayerService
2. BpMediaPlayerService用这个binder和BnMediaPlayerService通讯

## 实现自己的Service
1. 我们需要一个Bn，需要一个Bp，而且Bp不用暴露出来。那么就在BnXXX.cpp中一起实现好了。
2. XXXService提供自己的功能，例如getXXX调用

### 定义XXX接口
XXX接口是和XXX服务相关的，例如提供getXXX，setXXX函数，和应用逻辑相关。
需要从IInterface派生

### 定义BnXXX和BpXXX
BnXXX只不过是把IXXX接口加入到Binder架构中来，而不参与实际的getXXX和setXXX应用层逻辑
主要工作, 就是 onTransact 接收消息

### BpXXX
BpXXX可以在BnXXX中实现

主要干两件事 : getXXX 实现具体getXXX, 调用transact 发送出去


## refer
Android深入浅出之Binder机制
http://www.cnblogs.com/innost/archive/2011/01/09/1931456.html
