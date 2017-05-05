## register broadcast
过程也是通过Binder去调用C层
最终回调在ActivityManagerService.registerReceiver()

这个广播的intent-action,保存在当前activity的宿主进程中. 缓存在一个HashMap

## send broadcast
最终调用还是最终回调在ActivityManagerService.finishReceiver()
