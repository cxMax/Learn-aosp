# rudiment of activity start-up process

## prepare tech-knowledge
ActivityManagerService : 责管理Activity的生命周期的  
ActivityStack : 来把所有的Activity按照后进先出的顺序放在一个堆栈中  
ActivityThread :  
ApplicationThread : 一个Binder对象，负责和其它进程进行通信
ActivityManagerService和ActivityStack位于同一个进程中  
ApplicationThread和ActivityThread位于另一个进程中.

ps : android 7.1 源码 引入了ActivityStarter
ActivityStarter : 1.activity跳转和关闭的逻辑抽在这个类里面; 2.持有ActivityStack的引用,分配Activity堆栈和任务
## process analysis

### concise process analysis
ActivityManagerService.startActivity()

ActivityStarter.startActivityMayWait() :   
准备要启动的Activity的相关信息
这里跟Service启动类似.

ApplicationThread : 
跨进程向ActivityManagerService发送消息,

ActivityManagerService.activityPaused() :  
看看是否需要创建新的进程来启动Activity

ActivityManagerService.startProcessLocked() :  
创建一个新的进程

ActivityThread.performLaunchActivity() :  
ClassLoader导入相应的Activity类，然后把它启动起来
```
 activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window);
```

### startActivity from Launcher
Lancher.startActivitySafely() :  

Lancher.startActivity() :  
调用Activity.startActivity()函数

Activity.startActivity() :  
调用Activity.startActivityForResult()

Activity.startActivityForResult() :  
mMainThread的类型是ActivityThread，它代表的是应用程序的主线程.
ApplicationThread是一个Binder对象
ActivityManagerService会使用它来和ActivityThread来进行进程间通信

Instrumentation.execStartActivity() :  
调用ActivityManagerProxy.startActivity()

ActivityManagerProxy.startActivity() :  
通过binder传递IActivityManager, 这里会调用ActivityManagerService.startActivity()

ActivityManagerService.startActivity() :  
调用ActivityStarter.startActivityMayWait()

ActivityStarter.startActivityMayWait() :  
1. 对参数intent的内容进行解析，得到MainActivity的相关信息，保存在ActivityInfo变量中
2. 调用ActivityStarter.startActivityLocked()

ActivityStarter.startActivityLocked() :  
1. 创建即将要启动的Activity的相关信息，并保存在ActivityRecord变量中
2. 调用ActivityStarter.startActivityUnchecked()

ActivityStarter.startActivityUnchecked() :  
1. 当前有没有Task可以用来执行这个Activity
2. 创建新的Task里面来启动这个Activity
3. 新建的Task保存在TaskRecord域中，同时调用setTaskFromReuseOrCreateNewTask()，添加到ActivityManagerService中去 . ps:7.1的源码稍稍做了改变  
4. 调用ActivityStack.startActivityLocked()

ActivityStack.startActivityLocked() :  
1. 相对于android7.1的源码,resumeTopActivityLocked()这个方法被废弃掉了,但原有的加载逻辑不变,只是在这个流程中间加了几个方法
2. 调用ActivityStack.ensureActivitiesVisibleLocked()

ActivityStack.ensureActivitiesVisibleLocked() :  
调用ActivityStack.ensureActivityConfigurationLocked()

ActivityStack.ensureActivityConfigurationLocked() :  
调用ActivityStack.relaunchActivityLocked()

ActivityStack.relaunchActivityLocked() :  
调用ActivityStack.resumeTopActivityInnerLocked()

ActivityStack.resumeTopActivityInnerLocked() :  
调用ActivityStack.startPausingLocked()

ActivityStack.startPausingLocked() :  


## reference
Android应用程序的Activity启动过程简要介绍和学习计划 :   
http://blog.csdn.net/luoshengyang/article/details/6685853

Android应用程序启动过程源代码分析 :  
http://blog.csdn.net/luoshengyang/article/details/6689748