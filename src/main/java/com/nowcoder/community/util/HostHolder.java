package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

// 持有用户的信息，用来代替Session对象，（Session对象可以直接持有用户信息并线程隔离）
@Component
public class HostHolder {

    private ThreadLocal<User> users =  new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}
