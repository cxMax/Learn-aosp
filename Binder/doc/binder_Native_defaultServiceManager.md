## 获取ServiceManager
当进程注册服务(addService)或 获取服务(getService)的过程之前，都需要先调用defaultServiceManager()方法

### defaultServiceManager in IServiceManager.cpp
当尝试创建或获取ServiceManager时，ServiceManager可能尚未准备就绪，这时通过sleep 1秒后，循环尝试获取直到成功

1. ProcessState::self()
每个进程有且只有一个ProcessState对象
(1) 初始化ProcessState
    * 打开Binder驱动
    * 采用内存映射函数mmap，给binder分配一块虚拟地址空间,用来接收事务
    * binder默认的最大可并发访问的线程数为16
    
2. getContextObject()
获取BpBiner对象
(1) getStrongProxyForHandle
    * 查找handle对应的资源项
    * 通过ping操作测试binder是否准备就绪
    * 当handle值所对应的IBinder不存在或弱引用无效时，则创建BpBinder对

(2) lookupHandleLocked() in  ProcessState.cpp
返回handle向对应位置的handle_entry结构体指针

(3) 创建BpBinder
 IPCThreadState::self()->incWeakHandle(handle); //handle所对应的bindle弱引用 + 1

3. interface_cast<IServiceManager>()
用于获取BpServiceManager对象

(1) interface_cast
interface_cast<IServiceManager>() 等价于 IServiceManager::asInterface()  
asInterface()通过模板函数来定义的  
IServiceManager::asInterface() 等价于 new BpServiceManager()。在这里，更确切地说应该是new BpServiceManager(BpBinder)

(2) BpServiceManager实例化 in IServiceManager.cpp
    * BpInterface初始化 in IInterface.h
    * BpRefBase初始化 in Binder.cpp
        mRemote指向new BpBinder(0)，从而BpServiceManager能够利用Binder进行通过通信


### 总结
defaultServiceManager 等价于 new BpServiceManager(new BpBinder(0))

### refer
http://gityuan.com/2015/11/08/binder-get-sm/