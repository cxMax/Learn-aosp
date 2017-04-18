# Binder 

## relationship
### Server :
Server向ServiceManager注册了Binder实体及其名字
### Client :
Server向ServiceManager注册了Binder实体及其名字后，Client就可以通过名字获得该Binder的引用了
### ServiceManager :
ServiceManager的作用是将字符形式的Binder名字转化成Client中对该Binder的引用，使得Client能够通过Binder名字获得对Server中Binder实体的引用
ServiceManager是一个进程，Server是另一个进程，Server向SMgr注册Binder必然会涉及进程间通信.
当一个进程使用BINDER_SET_CONTEXT_MGR命令将自己注册成SMgr时Binder驱动会自动为它创建Binder实体

### Binder Drive :
驱动负责进程之间Binder通信的建立，Binder在进程之间的传递.
Binder驱动的代码位于linux目录的drivers/misc/binder.c中

## realize binder
### Binder in Server
做为Proxy设计模式的基础，首先定义一个抽象接口类封装Server所有功能，其中包含一系列纯虚函数留待Server和Proxy各自实现
接下来采用继承方式以接口类和Binder抽象类为基类构建Binder在Server中的实体，实现基类里所有的虚函数，包括公共接口函数以及数据包处理函数：onTransact()。

### Binder in Client
做为Proxy设计模式的一部分，Client端的Binder同样要继承Server提供的公共接口类并实现公共函数。但这不是真正的实现，而是对远程函数调用的包装：将函数参数打包，通过Binder向Server发送申请并等待返回值
由于继承了同样的公共接口类，Client Binder提供了与Server Binder一样的函数原型，使用户感觉不出Server是运行在本地还是远端。

### Binder in transaction
Binder可以塞在数据包的有效数据中越进程边界从一个进程传递给另一个进程.
无论是Binder实体还是对实体的引用都从属与某个进程，所以该结构不能透明地在进程之间传输，必须经过驱动翻译.

### Binder in document
将文件看成Binder实体，进程打开的文件号看成Binder的引用。一个进程可以将它打开文件的文件号传递给另一个进程，从而另一个进程也打开了同一个文件，就象Binder的引用在进程之间传递一样。

### Binder in drive
系统中所有的Binder实体以及每个实体在各个进程中的引用都登记在驱动中；驱动需要记录Binder引用->实体之间多对一的关系

### Binder entity in drive 
每个进程都有一棵红黑树用于存放创建好的节点，以Binder在用户空间的指针作为索引。每当在传输数据中侦测到一个代表Binder实体的flat_binder_object，先以该结构的binder指针为索引搜索红黑树；如果没找到就创建一个新节点添加到树中

### Binder reference in drive
就象一个对象有很多指针一样，同一个Binder实体可能有很多引用，不同的是这些引用可能分布在不同的进程中。和实体一样，每个进程使用红黑树存放所有正在使用的引用

### Binder memory manage
由Binder驱动负责管理数据接收缓存.Binder驱动实现了mmap()系统调用

### Binder thread manage
Binder通信实际上是位于不同进程中的线程之间的通信。

## Binder principle
### Binder manager
1. server manager 成为守护进程
至此，我们就从源代码一步一步地分析完Service Manager是如何成为Android进程间通信（IPC）机制Binder守护进程的了。总结一下，Service Manager是成为Android进程间通信（IPC）机制Binder守护进程的过程是这样的：

        1. 打开/dev/binder文件：open("/dev/binder", O_RDWR);

        2. 建立128K内存映射：mmap(NULL, mapsize, PROT_READ, MAP_PRIVATE, bs->fd, 0);

        3. 通知Binder驱动程序它是守护进程：binder_become_context_manager(bs);

        4. 进入循环等待请求的到来：binder_loop(bs, svcmgr_handler);

        在这个过程中，在Binder驱动程序中建立了一个struct binder_proc结构、一个struct  binder_thread结构和一个struct binder_node结构，这样，Service Manager就在Android系统的进程间通信机制Binder担负起守护进程的职责了。

2. server manager 即defaultServiceManager接口是如何实现的。
Service Manager远程接口就创建完成了，它本质上是一个BpServiceManager，包含了一个句柄值为0的Binder引用。
        在Android系统的Binder机制中，Server和Client拿到这个Service Manager远程接口之后怎么用呢？
        对Server来说，就是调用IServiceManager::addService这个接口来和Binder驱动程序交互了，即调用BpServiceManager::addService 。而BpServiceManager::addService又会调用通过其基类BpRefBase的成员函数remote获得原先创建的BpBinder实例，接着调用BpBinder::transact成员函数。在BpBinder::transact函数中，又会调用IPCThreadState::transact成员函数，这里就是最终与Binder驱动程序交互的地方了。
        对Client来说，就是调用IServiceManager::getService这个接口来和Binder驱动程序交互了。
        
## Binder realize in Java Layer

### 获取Service Manager的Java远程接口的过程；
总结一下，就是在Java层，我们拥有了一个Service Manager远程接口ServiceManagerProxy，而这个ServiceManagerProxy对象在JNI层有一个句柄值为0的BpBinder对象与之通过gBinderProxyOffsets关联起来。

### HelloService接口的定义；
这里我们可以看到IHelloService.aidl这个文件编译后的真面目，原来就是根据IHelloService接口的定义生成相应的Stub和Proxy类，这个就是我们熟悉的Binder机制的内容了，即实现这个HelloService的Server必须继续于这里的IHelloService.Stub类，而这个HelloService的远程接口就是这里的IHelloService.Stub.Proxy对象获得的IHelloService接口

### HelloService的启动过程；


### Client获取HelloService的Java远程接口的过程；
### Client通过HelloService的Java远程接口来使用HelloService提供的服务的过程。

## Terminology
纯虚函数 : 在基类中不能对虚函数给出有意义的实现，而把它声明为纯虚函数，它的实现留给该基类的派生类去做。这就是纯虚函数的作用。
红黑树 :　红黑树（Red Black Tree） 是一种自平衡二叉查找树

## References
Binder学习指南 : http://weishu.me/2016/01/12/binder-index-for-newer/
Android Bander设计与实现 - 设计篇 : http://blog.csdn.net/universus/article/details/6211589
Android进程间通信（IPC）机制Binder简要介绍和学习计划 : http://blog.csdn.net/luoshengyang/article/details/6618363
Android深入浅出之Binder机制 : http://www.cnblogs.com/innost/archive/2011/01/09/1931456.html

