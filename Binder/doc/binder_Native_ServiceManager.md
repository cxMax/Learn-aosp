## ServiceManager启动流程

## 原理
* ServiceManager是Binder IPC通信过程中的守护进程，本身也是一个Binder服务
* 没有采用libbinder中的多线程模型来与Binder驱动通信，而是自行编写了binder.c直接和Binder驱动来通信，并且只有一个循环binder_loop来进行读取和处理事务，这样的好处是简单而高效
* 其功能：查询和注册服务
* 对于Binder IPC通信过程中，其实更多的情形是BpBinder和BBinder之间的通信，比如ActivityManagerProxy和ActivityManagerService之间的通信等

## 启动过程
1. 打开binder驱动：binder_open
2. 注册成为binder服务的大管家：binder_become_context_manager
3. 进入无限循环，处理client端发来的请求：binder_loop

### main() in service_manager.c
1. binder_open
打开binder驱动，申请128k字节大小的内存空间
    * 通过系统调用陷入内核，打开Binder设备驱动
    * 通过系统调用，ioctl获取binder版本信息
    * 通过系统调用，mmap内存映射，mmap必须是page的整数倍
```
struct binder_state
{
    int fd; // dev/binder的文件描述符
    void *mapped; //指向mmap的内存地址
    size_t mapsize; //分配的内存大小，默认为128KB
};
```


2. binder_become_context_manager
成为上下文管理者

(1) binder_ioctl in kernel/drivers/android/binder.c

(2) binder_ioctl_set_ctx_mgr
    * 保证只创建一次mgr_node对象
    * 设置当前线程euid作为Service Manager的uid
    * 创建ServiceManager实体
    
(3) binder_new_node in kernel/drivers/android/binder.c
    * 首次进来为空
    * 给新创建的binder_node 分配内核空间
    * 将新创建的node对象添加到proc红黑树
    * 设置binder_work的type
在Binder驱动层创建binder_node结构体对象，并将当前binder_proc加入到binder_node的node->proc。并创建binder_node的async_todo和binder_work两个队列

3. binder_loop in servicemanager/binder.c
进入无限循环，处理client端发来的请求

(1) binder_write
    * ioctl(bs->fd, BINDER_WRITE_READ, &bwr) ; 
    其内容为BC_ENTER_LOOPER请求协议号。通过ioctl将bwr数据发送给binder驱动

(2) binder_ioctl in kernel/drivers/android/binder.c
    * 获取binder_thread
    * 进行binder的读写操作

(3) binder_ioctl_write_read in kernel/drivers/android/binder.c
    * 把用户空间数据ubuf拷贝到bwr
    * 将内核数据bwr拷贝到用户空间ubuf
    
(4) binder_thread_write in kernel/drivers/android/binder.c
    * 写缓存有数据
    
(5) binder_parse 解析binder信息
    * bio_init_from_txn ; 从txn解析出binder_io信息
    * res = func(bs, txn, &msg, &reply)
    func指向svcmgr_handler
    * binder_send_reply
    
(6) svcmgr_handler in  service_manager.c
    * 该方法的功能：查询服务，注册服务，以及列举所有服务
```
struct svcinfo
{
    struct svcinfo *next;
    uint32_t handle; //服务的handle值
    struct binder_death death;
    int allow_isolated;
    size_t len; //名字长度
    uint16_t name[0]; //服务名
};
```
    
### 核心工作

1. do_find_service
查询到目标服务，并返回该服务所对应的handle

2. do_add_service
    * svc_can_register：检查权限，检查selinux权限是否满足；
    * find_svc：服务检索，根据服务名来查询匹配的服务；
    * svcinfo_death：释放服务，当查询到已存在同名的服务，则先清理该服务信息，再将当前的服务加入到服务列表svclist；
    
    
### 总结
启动流程 : 
1. 打开binder驱动，并调用mmap()方法分配128k的内存映射空间 : binder_open();
2. 通知binder驱动使其成为守护进程：binder_become_context_manager()；
3. 验证selinux权限，判断进程是否有权注册或查看指定服务；
4. 进入循环状态，等待Client端的请求：binder_loop()
5. 注册服务的过程，根据服务名称，但同一个服务已注册，重新注册前会先移除之前的注册信息
6. 死亡通知: 当binder所在进程死亡后,会调用binder_release方法,然后调用binder_node_release.这个过程便会发出死亡通知的回调

ServiceManager最核心的两个功能 : 
1. 注册服务：记录服务名和handle信息，保存到svclist列表
2. 查询服务：根据服务名查询相应的的handle信息

### refer
http://gityuan.com/2015/11/07/binder-start-sm/