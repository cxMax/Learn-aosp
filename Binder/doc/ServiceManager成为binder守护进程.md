## ServiceManager
Service Manager是成为Android进程间通信（IPC）机制Binder守护进程的过程
主要组成 : binder.h、binder.c和service_manager.c

## 过程
1. service_manager.c 入口 main()
作用 : 
* 打开Binder设备文件；
* 告诉Binder驱动程序自己是Binder上下文管理者，即我们前面所说的守护进程；
* 三是进入一个无穷循环，充当Server的角色，等待Client的请求

2. binder_open in binder.c
* 调用Binder驱动程序的binder_open函数
* 创建一个struct binder_proc数据结构来保存打开设备文件/dev/binder的进程的上下文信息
* 这个进程上下文信息同时还会保存在一个全局哈希表binder_procs中，驱动程序内部使用

3. binder_proc
threads、nodes、 refs_by_desc和refs_by_node,binder_proc分别挂会这四个红黑树下

打开设备文件/dev/binder的操作就完成了，接着是对打开的设备文件进行内存映射操作mmap

4. binder_mmap

## 总结

Service Manager是成为Android进程间通信（IPC）机制Binder守护进程的过程
1. 打开/dev/binder文件：open("/dev/binder", O_RDWR);

2. 建立128K内存映射：mmap(NULL, mapsize, PROT_READ, MAP_PRIVATE, bs->fd, 0);

3. 通知Binder驱动程序它是守护进程：binder_become_context_manager(bs);

4. 进入循环等待请求的到来：binder_loop(bs, svcmgr_handler);

	在这个过程中，在Binder驱动程序中建立了一个struct binder_proc结构、一个struct  binder_thread结构和一个struct binder_node结构，这样，Service Manager就在Android系统的进程间通信机制Binder担负起守护进程的职责了。

## refer
浅谈Service Manager成为Android进程间通信（IPC）机制Binder守护进程之路
http://blog.csdn.net/luoshengyang/article/details/6621566

