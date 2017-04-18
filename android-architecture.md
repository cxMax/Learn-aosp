# android-architecture

## relationship
硬件驱动层 : C语言编写驱动程序,生成.o文件  
硬件抽象层 :   
作用 :   
### 为Android增加硬件抽象层（HAL）模块访问Linux内核驱动程序
步骤 : 
1. 编译成功后，就可以在out/target/product/generic/system/lib/hw目录下看到xx.default.so文件了
2. 重新打包后，system.img就包含我们定义的硬件抽象层模块xx.default了

### 为Android硬件抽象层（HAL）模块编写JNI方法提供Java访问硬件服务接口
步骤 : 
1. 新建xx.cpp
2. 定义被java层调用的JNI方法
3. JNI方法调用表增加函数,注册该JNI方法

### 为Android系统的Application Frameworks层增加硬件访问服务
步骤 :
1. 为硬件抽象层模块准备好JNI方法调用层
2. 调用这些硬件服务的应用程序与这些硬件服务之间的通信需要通过代理来进行.定义AIDL接口
3. 返回到frameworks/base目录，打开Android.mk文件，修改LOCAL_SRC_FILES变量的值，增加IHelloService.aidl源文件
4. 编译IHelloService.aidl接口,这样，就会根据IHelloService.aidl生成相应的IHelloService.Stub接口
5. 增加HelloService.java,   HelloService主要是通过调用JNI方法来提供硬件服务。
6. 修改同目录的SystemServer.java文件，在ServerThread::run函数中增加加载HelloService的代码

### 为Android系统内置Java应用程序测试Application Frameworks层的硬件服务
步骤: 
1. 通过AIDL直接调用JNI的service方法.client的调用是通过IHelloService.Stub.asInterface调用的

## android架构
它从下到上涉及到了Android系统的硬件驱动层、硬件抽象层、运行时库和应用程序框架层

## reference 
1. Android硬件抽象层（HAL）概要介绍和学习计划 : 
http://blog.csdn.net/luoshengyang/article/details/6567257
2. 在Ubuntu上为Android系统编写Linux内核驱动程序 : 
http://blog.csdn.net/luoshengyang/article/details/6568411
3. 在Ubuntu上为Android系统内置C可执行程序测试Linux内核驱动程序 : 
http://blog.csdn.net/luoshengyang/article/details/6571210
4. 在Ubuntu为Android硬件抽象层（HAL）模块编写JNI方法提供Java访问硬件服务接口 : 
http://blog.csdn.net/luoshengyang/article/details/6575988
5. 为Android系统的Application Frameworks层增加硬件访问服务
http://blog.csdn.net/luoshengyang/article/details/6578352

