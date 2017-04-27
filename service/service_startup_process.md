# Service 启动过程分析

## context.StartService()
<img src="http://img.my.csdn.net/uploads/201108/10/0_1312984689zu5P.gif" alt="uml类图">

Activity extends ContextThemeWrapper extends ContextWrapper extends Context  
ContextImpl extends Context (realize abstract methods )

##  ActivityManagerService的startService函数的处理流程 (通过Binder调用的ActivityMangerService)
流程 :  
ActivityManagerService.startService()

ActiveServices.startServiceLocked()

ActiveServices.retrieveServiceLocked() :
解析service这个Intent，就是解析前面我们在AndroidManifest.xml定义的Service标签的intent-filter相关内容，然后将解析结果放在res.record中

ActiveServices.startServiceInnerLocked()

ActiveServices.bringUpServiceLocked() : 
这个函数前面做了一系列的工作, 比如 destroy , finish 等,直接略过至下面关键一步 : 

ActivityManagerService.startProcessLocked() :  
这里调用Process.start函数创建了一个新的进程

ActivityThread.attach() :   
在Android应用程序中，每一个进程对应一个ActivityThread实例，所以，这个函数会创建一个thread实例

ActivityManagerProxy.attachApplication() :  
通过Binder驱动程序传递给ActivityManagerService

ActivityManagerService.attachApplicationLocked() :  
这里通过进程uid和进程名称将它找出来，然后通过realStartServiceLocked函数来进一步处理

ActiveServices.realStartServiceLocked() :  
调用这个远程接口的scheduleCreateService函数回到原来的ActivityThread对象中执行启动服务的操作

ApplicationThreadProxy.scheduleCreateService() : 
这里通过Binder驱动程序回到新进程的ApplicationThread对象中去执行scheduleCreateService函数

ActivityThread.scheduleCreateService() :  android 7.1最新的源码把这里的实现改了, 跟blog里面的ApplicationThread.scheduleCreateService()有出入
一个CreateServiceData数据放到消息队列中去，并且分发这个消息

H.sendMessage() :  
处理的消息是CREATE_SERVICE，它调用ActivityThread类的handleCreateService成员函数进一步处理。

ActivityThread.handleCreateService() : 
```
service.attach(context, this, data.info.name, data.token, app,
                    ActivityManagerNative.getDefault());
            service.onCreate();
```
到这一步, 就说service创建成功了

## 总结 : 
1. 它通过三次Binder进程间通信完成了服务的启动过程
2. 从主进程调用到ActivityManagerService进程中，完成新进程的创建.
3. 从新进程调用到ActivityManagerService进程中，获取要在新进程启动的服务的相关信息
4. 从ActivityManagerService进程又回到新进程中，最终将服务启动起来




## references 
Android系统在新进程中启动自定义服务过程（startService）的原理分析 :   
http://blog.csdn.net/luoshengyang/article/details/6677029