## process
1. ActivityManagerService.startProcessLocked : 调用了Process.start函数开始为应用程序创建新的进程

2. Process.startViaZygote : 
这个函数将创建进程的参数放到argsForZygote列表中去，如参数"--runtime-init"表示要为新创建的进程初始化运行时库，然后调用zygoteSendAndGetPid函数进一步操作

3. Process.zygoteSendAndGetPid : 
openZygoteSocketIfNeeded初始化sZygoteWriter的一个Socket写入流

4. ZygoteInit.runSelectLoop : 
得到的是一个ZygoteConnection对象

5. ZygoteConnection.runOnce : 
pid = Zygote.forkAndSpecialize 创建一个进程

6. ZygoteConnection.handleChildProc : 

7. RuntimeInit.zygoteInit : 
zygoteInitNative() : 执行Binder驱动程序初始化的相关工作
invokeStaticMain() : 就是执行进程的入口函数，这里就是执行startClass类的main函数. ActivityThread.main()

8.  ActivityThread.main : 

创建一个ActivityThread对象; 
然后进入消息循环中;