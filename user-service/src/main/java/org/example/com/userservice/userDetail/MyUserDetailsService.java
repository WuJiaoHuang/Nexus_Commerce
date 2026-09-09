package org.example.com.userservice.userDetail;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.com.userservice.mapper.UserMapper;
import org.example.com.userservice.pojo.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public MyUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        UserPrincipal userPrincipal = new UserPrincipal(user);
        return userPrincipal;
    }

}
