package com.achobeta.themis.domain.user.service;

import com.achobeta.themis.common.vo.AuthResponseVO;
import com.achobeta.themis.common.vo.ForgetPasswdRequestVO;
import com.achobeta.themis.common.vo.LoginRequestVO;
import com.achobeta.themis.domain.user.model.UserModel;

/**
 * @author Aseubel
 * @date 2025/10/27 上午1:42
 */
public interface IUserService {

    UserModel getUserByUserId(UserModel userModel);

    UserModel getUserInfo(Long userId);

     AuthResponseVO login(LoginRequestVO request);

    AuthResponseVO refreshToken(String refreshToken);

    void forgetPassword(ForgetPasswdRequestVO request);

    void sendVerifyCode(String phone);
}
