package com.ssafy.api.response;

import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.db.entity.User;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원 본인 정보 조회 API ([GET] /api/v1/users/me) 요청에 대한 응답값 정의.
 */
@Getter
@Setter
@ApiModel("UserResponse")
public class UserRes{
	@ApiModelProperty(name="User ID")
	String userId;
	
	@ApiModelProperty(name="User name")
	String name;
	
	@ApiModelProperty(name="User position")
	String position;
	
	@ApiModelProperty(name="User deparment")
	String deparment;
	
	public static UserRes of(User user) {
		UserRes res = new UserRes();
		res.setUserId(user.getUserId());
		res.setName(user.getName());
		res.setPosition(user.getPosition());
		res.setDeparment(user.getDepartment());
	
		return res;
	}
}
