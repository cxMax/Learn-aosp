## Binder Driver
驱动这块,细节看得云里雾里, 用到了或者面试在回来看
## 概述
* 初始化(binder_init)
* 打开 (binder_open)
* 映射(binder_mmap)
* 数据操作(binder_ioctl)
系统调用(syscall)，比如打开Binder驱动方法的调用链为： open-> __open() -> binder_open()

## 核心方法
1. binder_init
* 创建名为binder的工作队列
* misc_register 注册misc设备
```
static struct miscdevice binder_miscdev = {
    .minor = MISC_DYNAMIC_MINOR, //次设备号 动态分配
    .name = "binder",     //设备名
    .fops = &binder_fops  //设备的文件操作结构，这是file_operations结构
};
```
* 在debugfs文件系统中创建一系列的文件

2. binder_open 过程需要持有binder_main_lock同步锁
* 创建binder_proc对象，并把当前进程等信息保存到binder_proc对象
* 把binder_proc加入到全局链表binder_procs

3. binder_mmap 过程需要持有binder_main_lock同步锁
* 保证一次只有一个进程分配内存，保证多进程间的并发访问
* 分配物理空间，将物理空间映射到内核空间，将物理空间映射到进程空间

4. binder_ioctl 过程需要持有binder_main_lock同步锁
binder_ioctl()函数负责在两个进程间收发IPC数据和IPC reply数据
* 文件描述符，是通过open()方法打开Binder Driver后返回值
* ioctl命令和数据类型是一体的，不同的命令对应不同的数据类型

## refer
http://gityuan.com/2015/11/01/binder-driver/
http://gityuan.com/2015/11/02/binder-driver-2/