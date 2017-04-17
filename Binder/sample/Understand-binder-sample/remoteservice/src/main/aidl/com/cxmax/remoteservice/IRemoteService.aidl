// IRemoteService.aidl
package com.cxmax.remoteservice;
import com.cxmax.remoteservice.User;
// Declare any non-default types here with import statements

interface IRemoteService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    // in 表示传入数据， out 表示传出数据， inout 表示双向传递。注意含有 out 时 User 类需要实现 readFromParcel() 方法
    void addUser(in User user);
}
