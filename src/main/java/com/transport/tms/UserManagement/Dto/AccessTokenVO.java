package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccessTokenVO {
    public String accessToken;
    public List<String> permissions;
    public UserVO userVO;
}
