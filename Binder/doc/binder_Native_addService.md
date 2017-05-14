## 向ServiceManager注册Native层的服务的过程
* MediaService  

1. ProcessState::self  
* open_driver作用是打开/dev/binder设备  
* mmap分配内存空间  
ProcessState采用单例模式，保证每一个进程都只打开一次Binder Driver  

2. MediaPlayerService::instantiate()  
MediaPlayerService.cpp  
* 由defaultServiceManager()返回的是BpServiceManager，同时会创建ProcessState对象和BpBinder对象。 故此处等价于调用BpServiceManager->addService  

IServiceManager.cpp ::BpServiceManager  
* 向ServiceManager注册服务MediaPlayerService，服务名为”media.player”  
最后调用的BpBinder::transact  

* IPCThreadState::self()->transact  
真正工作还是交给IPCThreadState来进行transact工作 ,   
 //初始IPCThreadState  
 //创建线程的TLS(线程本地储存空间)  
 
* IPCThreadState进行transact事务处理分3部分：  
 errorCheck() //数据错误检查  
 writeTransactionData() // 传输数据  
 waitForResponse() //f等待响应  
 
 * talkWithDriver in waitForResponse()  
 binder_write_read结构体用来与Binder设备交换数据的结构, 通过ioctl与mDriverFD通信，是真正与Binder驱动进行数据读写交互的过程。 主要是操作mOut和mIn变量  
 
3. Binder Driver  内核层了  
ioctl()经过系统调用后进入Binder Driver  

* binder_ioctl_write_read  
//将用户空间bwr结构体拷贝到内核空间  
//将数据放入目标进程  
//读取自己队列的数据   
//将内核空间bwr结构体拷贝到用户空间

* binder_thread_write
//拷贝用户空间的cmd命令，此时为BC_TRANSACTION  
//拷贝用户空间的binder_transaction_data  

* binder_transaction
注册服务的过程，传递的是BBinder对象  
服务注册过程是在服务所在进程创建binder_node  
向servicemanager的binder_proc->todo添加BINDER_WORK_TRANSACTION事务，接下来进入ServiceManager进程

4. ServiceManager
循环在binder_loop()过程， 会调用binder_parse()方法

* binder_parse()
//获取handle  
//注册指定服务  


* do_add_service
svcinfo记录着服务名和handle信息，保存到svclist列表

* binder_send_reply
binder_write进入binder驱动后，将BC_FREE_BUFFER和BC_REPLY命令协议发送给Binder驱动， 向client端发送reply

## 总结 : 
注册过程 : 
1. MediaPlayerService进程调用ioctl()向Binder驱动发送IPC数据
2. Binder驱动收到该Binder请求,并将整个binder_transaction数据(记为T2)插入到目标线程的todo队列
3. Service Manager(c++)的线程thread2收到T2后，调用服务注册函数将服务”media.player”注册到服务目录中。当服务注册完成后，生成IPC应答数据
4. Binder驱动收到该Binder应答请求，生成BR_REPLY命令 , 在MediaPlayerService收到该命令后，知道服务注册完成便可以正常使用.
## refer 
http://gityuan.com/2015/11/14/binder-add-service/

