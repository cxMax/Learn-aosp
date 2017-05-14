# Binder Framework

## 概述
1. Binder 架构
[!img] 图

2. Binder 类图

* ServiceManager
(1) 通过getIServiceManager方法获取的是ServiceManagerProxy对象
(2) ServiceManager的addService, getService实际工作都交由ServiceManagerProxy的相应方法来处理

* ServiceManagerProxy
(1) 其成员变量mRemote指向BinderProxy对象，ServiceManagerProxy的addService, getService方法最终是交由mRemote来完成

* ServiceManagerNative
(1) asInterface()返回的是ServiceManagerProxy对象
(2) ServiceManager便是借助ServiceManagerNative类来找到ServiceManagerProxy

* Binder
(1) 其成员变量mObject和方法execTransact()用于native方法

* BinderInternal
(1) 内部有一个GcWatcher类，用于处理和调试与Binder相关的垃圾回收

* IBinder
(1) 一个内部接口DeathDecipient


## 初始化
在Android系统开机过程中，Zygote启动时会有一个虚拟机注册过程，该过程调用AndroidRuntime::startReg方法来完成jni方法的注册

1. startReg in AndroidRuntime.cpp
register_android_os_Binder

2. register_android_os_Binder in android_util_Binder.cpp
* int_register_android_os_Binder 
    注册Binder类的jni方法
* int_register_android_os_BinderInternal
    注册BinderInternal类的jni方法
* int_register_android_os_BinderProxy
    注册BinderProxy类的jni方法

3. int_register_android_os_Binder in android_util_Binder.cpp
注册 Binder类的jni方法
* 通过gBinderOffsets，保存Java层Binder类的信息，为JNI层访问Java层提供通道
* 通过RegisterMethodsOrDie，将gBinderMethods数组完成映射关系，从而为Java层访问JNI层提供通道

4. int_register_android_os_BinderInternal in android_util_Binder.cpp
与上个方法类似,建立了是BinderInternal类在Native层与framework层之间的相互调用的桥梁

5. int_register_android_os_BinderProxy in android_util_Binder.cpp
与上个方法类似,建立了是BinderProxy类在Native层与framework层之间的相互调用的桥梁

## 注册服务
这是一个java层 -> c++层 -> kernel的过程

1. ServiceManager.addService()

2. getIServiceManager()
ServiceManagerNative.asInterface(BinderInternal.getContextObject())

3. BinderInternal.getContextObject()
调用 android_os_BinderInternal_getContextObject() in android_util_binder.cpp
返回 ProcessState::self()->getContextObject()等价于 new BpBinder(0)

4. javaObjectForIBinder() in android_util_binder.cpp
根据BpBinder(C++)生成BinderProxy(Java)对象. 主要工作是创建BinderProxy对象,并把BpBinder对象地址保存到BinderProxy.mObject成员变量

5. ServiceManagerNative.asInterface(BinderInternal.getContextObject())
等价于 ServiceManagerNative.asInterface(new BinderProxy())

6. ServiceManagerProxy初始化
mRemote为BinderProxy对象，该BinderProxy对象对应于BpBinder(0)，其作为binder代理端，指向native层大管家service Manage

7. ServiceManagerProxy.addService()
framework层的ServiceManager的调用实际的工作确实交给SMP的成员变量BinderProxy；而BinderProxy通过jni方式，最终会调用BpBinder对象

* writeStrongBinder
* android_os_Parcel_writeStrongBinder 
将java层Parcel转换为native层Parcel
* ibinderForJavaObject
根据Binde(Java)生成JavaBBinderHolder(C++)对象. 主要工作是创建JavaBBinderHolder对象,并把JavaBBinderHolder对象地址保存到Binder.mObject成员变量.
* JavaBBinder in android_util_Binder.cpp
继承于BBinder对象
* flatten_binder in parcel.cpp
将Binder对象扁平化，转换成flat_binder_object对象
(1) 对于Binder实体，则cookie记录Binder实体的指针
(2) 对于Binder代理，则用handle记录Binder代理的句柄
* BinderProxy.transact()
transactNative()
* android_os_BinderProxy_transact in android_util_Binder.cpp
BpBinder::transact()

### 小结
注册服务过程就是通过BpBinder来发送ADD_SERVICE_TRANSACTION命令，与实现与binder驱动进行数据交互

## 获取服务
1. ServiceManager.getService()
.....
ServiceManagerNative -> ServiceManagerProxy初始化.getService()
2. BinderProxy.transact
3. transactNative
4. android_os_BinderProxy_transact
* java Parcel转为native Parcel
* gBinderProxyOffsets.mObject中保存的是new BpBinder(0)对象
* BpBinder->transact(code, *data, reply, flags)
 也就是说通过getService()最终获取了指向目标Binder服务端的代理对象BinderProxy
 
## refer
http://gityuan.com/2015/11/21/binder-framework/