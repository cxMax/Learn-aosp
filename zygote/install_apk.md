## install_apk
1. SystemServer.init1(args)

2. com_android_server_SystemServer.system_init
初始化几个服务

3. runtime->callStatic("com/android/server/SystemServer", "init2")调用SystemServer的init2

4. SystemServer.init2(args) : 
创建了一个ServerThread线程，PackageManagerService服务就是这个线程中启动的了

5. ServerThread.run : 
启动PackageManagerService, ActivityManagerService .

6. PackageManagerService.main : 
这里会调用scanDirLI函数来扫描移动设备上的下面这五个目录中的Apk文件
        /system/framework

        /system/app

        /vendor/app

        /data/app

        /data/app-private
        
7. 创建一个PackageParser实例，接着调用这个实例的parsePackage函数来对这个Apk文件进行解析
解析AndroidManifest.xml
从Apk归档文件中得到这个配置文件后，就调用另一外版本的parsePackage函数对这个应用程序进行解析了

8. PackageManagerService.scanPackageLI
这个函数主要就是把前面解析应用程序得到的package、provider、service、receiver和activity等信息保存在PackageManagerService服务中了
